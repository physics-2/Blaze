package org.firstinspires.ftc.teamcode.blaze;


import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.blaze.Actuators.Actuator;
import org.firstinspires.ftc.teamcode.blaze.AutoWire.ElectronicsConfig;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.PowerTrainSubsystem.PowerTrain;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.BlazeRuntime;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.ExecutorManager;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.Scheduler;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.SyncTopicBus;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.TopicBus;

import java.util.HashMap;
import java.util.Objects;

/**
 * The central orchestrator and entry point for the Blaze framework.
 * <p>
 * This class manages the lifecycle of hardware configuration, module scanning,
 * telemetry setup, and global framework components (like the Scheduler and TopicBus).
 * It acts as a singleton facade, providing static access to core framework services.
 * </p>
 */
public class BlazeCore {

    // --- State Flags ---
    private static boolean areManagersInitialized = false;
    private static boolean areCacheCleaned = false;

    // --- Registries ---
    private static final HashMap<String, PowerTrain> allPowerTrains = new HashMap<>();
    private static final HashMap<String, String> moduleToPowerTrain = new HashMap<>();
    private static final HashMap<String, Object> allCustom = new HashMap<>();


    // --- Hardware & Context References ---
    private static HardwareMap hardwareMap;
    private static Gamepad gamepad1;
    private static Gamepad gamepad2;

    // --- Configuration ---
    private static ElectronicsConfig electronicsConfig;
    private static final String PACKAGE_NAME = "org.firstinspires.ftc.teamcode";

    // --- Telemetry ---
    private static MultiDashTelemetry dashTelemetry;


    // Private constructor to prevent instantiation of this utility class
    private BlazeCore(){}

    /**
     * Initializes the core framework configuration, hardware map, and runtime managers.
     *
     * @param hardwareMap The FTC HardwareMap provided by the OpMode.
     */
    public static void createConfig(HardwareMap hardwareMap)  {
        BlazeCore.hardwareMap = hardwareMap;

        // Clear cache only on the very first launch to ensure a clean state
        if(!areCacheCleaned){
            BlazeAnnotationScanner.removeCache(hardwareMap);
            BlazeLogger.addDefaultLog("Config","First launch,cleaning cache");
            areCacheCleaned = true;
        }

        BlazeLogger.addDefaultLog("Config","Scanning annotations: ");


        // Initialize runtime managers (Scheduler, TopicBus, etc.) exactly once
        if (!areManagersInitialized) {
            BlazeLogger.addDefaultLog("Config","Creating runtime/managers ");
            BlazeRuntime.create();
            areManagersInitialized = true;
        }

        // Clear previous hardware state to prevent dead hardware pointers between OpMode runs
        BlazeLogger.addDefaultLog("Config","Cleaning hardware");
        destroyHardware();

        long startTime = System.currentTimeMillis();
        // Scan for and instantiate the hardware configuration class
        if (electronicsConfig == null) {

            Class<? extends ElectronicsConfig> configClass = BlazeRegistry.getHardwareConfigClass();
            BlazeLogger.addDefaultLog("Config","Got electronics config");

            if (configClass == null) {
                throw new IllegalStateException("No class annotated with @HardwareConfig found in package: " + PACKAGE_NAME);
            }

            try {
                BlazeCore.electronicsConfig = configClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Failed to instantiate configuration class '" + configClass.getSimpleName() +
                                "'. Ensure it has a public, no-argument constructor.", e
                );
            }
        }

        // Apply the hardware configuration
        electronicsConfig.addElectronicsFull(hardwareMap);
        long timeTook = System.currentTimeMillis() - startTime;
        BlazeLogger.addDefaultLog("Config","Took " + timeTook + " ms to do electronics config stuff");
    }



    /**
     * Registers the gamepads for the current OpMode cycle.
     *
     * @param gamepad1 The primary gamepad.
     * @param gamepad2 The secondary gamepad.
     */
    public static void addGamepads(Gamepad gamepad1,Gamepad gamepad2){
        BlazeCore.gamepad1 = gamepad1;
        BlazeCore.gamepad2 = gamepad2;
    }

    /**
     * Triggers the instantiation and autowiring of all registered modules.
     * Must be called after {@link #scanAnnotations()} and {@link #setTelemetry(Telemetry)}.
     */
    public static void createModules(){
        BlazeRegistry.createAll(dashTelemetry);
    }

    /**
     * Scans the APK for modules and telemetry providers, populating the registry.
     */
    public static void scanAnnotations(){
        BlazeAnnotationScanner.scanAndRegisterAll(hardwareMap);
    }

    /**
     * Configures the global telemetry instance.
     * It automatically checks for a custom {@link TelemetryProvider}
     * If none is found, it falls back to the default {@link MultiDashTelemetry}.
     *
     * @param coreTelemetry The standard FTC Telemetry object from the OpMode.
     */
    public static void setTelemetry(Telemetry coreTelemetry) {
        TelemetryProvider provider = BlazeRegistry.getTelemetryProvider();

        if (provider != null) {

            dashTelemetry = provider.create(coreTelemetry);
        } else {
            dashTelemetry = new MultiDashTelemetry(coreTelemetry);
            BlazeLogger.addDefaultLog("Config", "No @TelemetryProvider found. Using default MultiDashTelemetry.");
        }
    }

    // =========================================================================
    // Runtime Component Getters
    // =========================================================================

    public static Scheduler getScheduler() { return BlazeRuntime.scheduler(); }
    public static TopicBus getTopicBus() { return BlazeRuntime.bus(); }
    public static ExecutorManager getExecutorManager() { return BlazeRuntime.executors(); }
    public static SyncTopicBus getFastBus(){
        return BlazeRuntime.fastBus();
    }


    // =========================================================================
    // Hardware & PowerTrain Management
    // =========================================================================

    /**
     * Clears all registered PowerTrains and custom components.
     * Should be called at the beginning of a new OpMode initialization.
     */
    public static void destroyHardware() {

        allPowerTrains.clear();
        allCustom.clear();
        moduleToPowerTrain.clear();
    }


    /**
     * Registers a PowerTrain subsystem.
     *
     * @param powerTrain The PowerTrain instance to register.
     * @throws IllegalStateException if a PowerTrain with the same name already exists.
     */
    public static void addPowerTrain(PowerTrain powerTrain) {
        BlazeLogger.addDefaultLog("Config","added powertrain: "  + powerTrain.getName() + ". Autowired:" + powerTrain.getAutowireModule());
        String name = powerTrain.getName();
        if (allPowerTrains.containsKey(name)) {
            throw new IllegalStateException("Duplicate powerTrain '" + name + "'");
        }
        allPowerTrains.put(name, powerTrain);

        if (powerTrain.getAutowireModule() != null) {
            moduleToPowerTrain.put(powerTrain.getAutowireModule(), powerTrain.getName());
        }
    }

    /**
     * Registers multiple PowerTrain subsystems at once.
     *
     * @param powerTrains Varargs array of PowerTrain instances.
     */
    public static void addPowerTrains(PowerTrain... powerTrains) {
        for (PowerTrain powerTrain : powerTrains) {
            addPowerTrain(powerTrain);
        }
    }


    /**
     * Retrieves a PowerTrain by the name of the module it is autowired to.
     *
     * @param moduleName The name of the module.
     * @return The associated PowerTrain
     */
    public static PowerTrain getPowerTrainByModule(String moduleName) {

        for (PowerTrain powerTrain : allPowerTrains.values()) {
            if (Objects.equals(powerTrain.getAutowireModule(), moduleName)) {
                return powerTrain;
            }
        }
        throw new IllegalStateException("Couldn't find an autowired powertrain by module " + moduleName);
    }





    /**
     * Retrieves a specific actuator from a registered PowerTrain.
     *
     * @param powertrainName  The name of the PowerTrain.
     * @param classOrInterface The expected class or interface of the actuator.
     * @param actuatorName    The name of the actuator within the PowerTrain.
     * @param <T>             The type of the actuator.
     * @return The configured actuator instance.
     * @throws IllegalArgumentException if the actuator is not found.
     * @throws IllegalStateException    if the actuator type does not match the expected type.
     */
    public static <T extends Actuator> T getActuatorFromPowerTrain(String powertrainName, Class<T> classOrInterface, String actuatorName){
        PowerTrain powerTrain = allPowerTrains.get(powertrainName);
        if (powerTrain == null) {
            throw new IllegalArgumentException("PowerTrain '" + powertrainName + "' not found.");
        }

        Actuator actuator = powerTrain.get(classOrInterface, actuatorName);

        if (actuator == null) {
            String errorMsg = "ACTUATOR NOT FOUND: '" + actuatorName + "' in PowerTrain '" + powertrainName + "'.\n" +
                    "Available actuators: " + powerTrain.getAll() + "\n" +
                    "Check your PowerTrain builder configuration.";
            throw new IllegalArgumentException(errorMsg);
        }

        if (!classOrInterface.isInstance(actuator)) {
            String errorMsg = "TYPE MISMATCH for actuator '" + actuatorName + "':\n" +
                    "  Expected: " + classOrInterface.getSimpleName() + "\n" +
                    "  Actual:   " + actuator.getClass().getSimpleName() + "\n" +
                    "  Check your PowerTrain builder configuration!";
            throw new IllegalStateException(errorMsg);
        }

        return classOrInterface.cast(actuator);
    }






    // =========================================================================
    // Custom Component Registry
    // =========================================================================

    /**
     * Registers a custom object (e.g., a vision pipeline, a custom sensor manager)
     * into the global framework registry.
     *
     * @param name   The unique identifier for the custom component.
     * @param custom The component instance.
     * @param <T>    The type of the component.
     * @throws IllegalStateException if a component with the same name already exists.
     */
    public static <T> void addCustom(String name, T custom) {
        if (allCustom.containsKey(name)) {
            throw new IllegalStateException("Duplicate custom component registered with name: '" + name + "'");
        }
        allCustom.put(name, custom);
    }


    /**
     * Retrieves a registered custom component by name and type.
     *
     * @param name             The unique identifier of the component.
     * @param classOrInterface The expected class or interface.
     * @param <T>              The type of the component.
     * @return The custom component instance.
     * @throws IllegalArgumentException if the component is not found.
     * @throws IllegalStateException    if the component type does not match.
     */
    public static <T> T getCustom(String name, Class<T> classOrInterface) {
        Object custom = allCustom.get(name);

        if (custom == null) {
            String errorMsg = "CUSTOM COMPONENT NOT FOUND: '" + name + "'.\n" +
                    "Available components: " + allCustom.keySet() + "\n" +
                    "Ensure it was registered via BlazeCore.addCustom() before retrieval.";
            throw new IllegalArgumentException(errorMsg);
        }

        if (!classOrInterface.isInstance(custom)) {
            String errorMsg = "TYPE MISMATCH for custom component '" + name + "':\n" +
                    "  Expected: " + classOrInterface.getSimpleName() + "\n" +
                    "  Actual:   " + custom.getClass().getSimpleName();
            throw new IllegalStateException(errorMsg);
        }

        BlazeLogger.addDefaultLog("Config", "Retrieved custom component: '" + name + "' as " + classOrInterface.getSimpleName());
        return classOrInterface.cast(custom);
    }


    // =========================================================================
    // Internal Scanning Logic
    // =========================================================================





    // =========================================================================
    // Global Getters
    // =========================================================================
    public static HashMap<String, Object> getAllCustom() {
        return allCustom;
    }

    public static HashMap<String, PowerTrain> getAllPowerTrains() {
        return allPowerTrains;
    }

    public static HardwareMap getHardwareMap() {
        return hardwareMap;
    }

    public static MultiDashTelemetry getTelemetry() {
        return dashTelemetry;
    }

    public static Gamepad getGamepad2() {
        return gamepad2;
    }

    public static Gamepad getGamepad1() {
        return gamepad1;
    }

    public static PowerTrain getPowerTrain(String name){
        return allPowerTrains.get(name);
    }
}
