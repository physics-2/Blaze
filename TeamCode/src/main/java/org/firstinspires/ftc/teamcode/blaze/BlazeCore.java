package org.firstinspires.ftc.teamcode.blaze;



import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.blaze.Actuators.Actuator;
import org.firstinspires.ftc.teamcode.blaze.Anotations.HardwareConfig;
import org.firstinspires.ftc.teamcode.blaze.AutoWire.ElectronicsConfig;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.PowerTrainSubsystem.PowerTrain;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.ExecutorManager;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.BlazeRuntime;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.Scheduler;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.SyncTopicBus;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.TopicBus;

import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Objects;

@Configurable
public class BlazeCore {

    private static boolean areManagersInitialized = false;
    private static boolean areModulesInitialized = false;
    private static boolean areCacheCleaned = false;
    private static HashMap<String, PowerTrain> allPowerTrains = new HashMap<>();
    private static HashMap<String, String> moduleToPowerTrain = new HashMap<>();
    private static HashMap<String, Object> allCustom = new HashMap<>();
    private static HardwareMap hardwareMap;
    private static Gamepad gamepad1;
    private static Gamepad gamepad2;
    private static ElectronicsConfig electronicsConfig;
    private static String packageName = "org.firstinspires.ftc.teamcode";
    private static MultiDashTelemetry dashTelemetry;


    BlazeCore(){}
    public static void createConfig(HardwareMap hardwareMap)  {
        BlazeCore.hardwareMap = hardwareMap;
        if(!areCacheCleaned){
            ModuleRegistry.removeCache(hardwareMap);
            BlazeLogger.addDefaultLog("Config","First launch,cleaning cache");
            areCacheCleaned = true;
        }

        if (!areManagersInitialized) {
            BlazeRuntime.create();
            areManagersInitialized = true;
        }

        destroyHardware();

        if(electronicsConfig == null) {
            Class<? extends ElectronicsConfig> configClass = scanForConfigClass(hardwareMap);

            try {
                BlazeCore.electronicsConfig = configClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Couldn't create configuration " + configClass.getSimpleName() +
                        ". Make sure that class has a public constructor.", e);
            }
        }

        electronicsConfig.addElectronicsFull(hardwareMap);




    }


    public static void setTelemetry(Telemetry telemetry){
        dashTelemetry = new MultiDashTelemetry(telemetry);
    }

    public static void addGamepads(Gamepad gamepad1,Gamepad gamepad2){
        BlazeCore.gamepad1 = gamepad1;
        BlazeCore.gamepad2 = gamepad2;
    }

    public static void createModules(){
        ModuleRegistry.scanAndRegisterAll(hardwareMap);
        ModuleRegistry.createAll(dashTelemetry);
        areModulesInitialized = true;
    }

    public static Scheduler getScheduler() { return BlazeRuntime.scheduler(); }
    public static TopicBus getTopicBus() { return BlazeRuntime.bus(); }
    public static ExecutorManager getExecutorManager() { return BlazeRuntime.executors(); }

    public static void destroyHardware() {

        allPowerTrains.clear();
        allCustom.clear();
        moduleToPowerTrain.clear();
    }


    public static void addPowerTrain(PowerTrain powerTrain) {
        BlazeLogger.addDefaultLog("Config","added powertrain: "  + powerTrain.getName() + ". Autowired:" + powerTrain.getAutowireModule());
        String name = powerTrain.getName();
        if (allPowerTrains.containsKey(name)) {
            throw new RuntimeException("Duplicate powerTrain '" + name + "'");
        }
        allPowerTrains.put(name, powerTrain);

        if (powerTrain.getAutowireModule() != null) {
            moduleToPowerTrain.put(powerTrain.getAutowireModule(), powerTrain.getName());
        }
    }



    public static PowerTrain getPowerTrainByModule(String moduleName) {

        for (PowerTrain powerTrain : allPowerTrains.values()) {
            if (Objects.equals(powerTrain.getAutowireModule(), moduleName)) {
                return powerTrain;
            }
        }
        return null;
    }
    public static <T> void addCustom(String name, T custom) {
        if (allCustom.containsKey(name)) {
            throw new RuntimeException("Duplicate custom: '" + name + "'");
        }
        allCustom.put(name, custom);
    }

    public static void addPowerTrains(PowerTrain... powerTrains) {
        for (PowerTrain powerTrain : powerTrains) {
            addPowerTrain(powerTrain);
        }
    }
    public static <T extends Actuator> T getActuatorFromPowerTrain(String powertrain,Class<T> classOrInterface,String actuatorName){
        Actuator actuator = allPowerTrains.get(powertrain).get(classOrInterface,actuatorName);

        if(actuator == null){
            String errorMsg = "ACTUATOR NOT FOUND: '" + actuatorName + "'\n";
            errorMsg += "Available actuator: " + Objects.requireNonNull(allPowerTrains.get(powertrain)).getAll().toString() + "\n";
            throw new RuntimeException(errorMsg);
        }

        if (!classOrInterface.isInstance(actuator)) {
            throw new RuntimeException(" TYPE MISMATCH for '" + actuatorName + "':\n" +
                    "  Expected: " + classOrInterface.getSimpleName() + "\n" +
                    "  Actual:   " + actuator.getClass().getSimpleName() + "\n" +
                    "  Check your PowerTrain builder configuration!");
        }

        return classOrInterface.cast(actuator);
    }

    private static Class<? extends ElectronicsConfig> scanForConfigClass(HardwareMap hardwareMap) {

        Class<? extends ElectronicsConfig> foundConfig = null;
        int foundCount = 0;

        try {
            String apkPath = hardwareMap.appContext.getPackageCodePath();
            dalvik.system.DexFile dexFile = new dalvik.system.DexFile(apkPath);
            Enumeration<String> entries = dexFile.entries();
            ClassLoader classLoader = hardwareMap.appContext.getClassLoader();

            while (entries.hasMoreElements()) {
                String className = entries.nextElement();
                if (className.startsWith(packageName)) {
                    try {
                        Class<?> clazz = Class.forName(className, false, classLoader);
                        if (clazz.isAnnotationPresent(HardwareConfig.class) &&
                                ElectronicsConfig.class.isAssignableFrom(clazz) &&
                                !Modifier.isAbstract(clazz.getModifiers())) {

                            foundConfig = (Class<? extends ElectronicsConfig>) clazz;
                            BlazeLogger.addDefaultLog("Config","Found config: "  + foundConfig.getSimpleName());
                            foundCount++;
                        }
                    } catch (ClassNotFoundException | NoClassDefFoundError ignored) {

                    }
                }
            }
            dexFile.close();
        } catch (Exception e) {
            throw new RuntimeException("Critical error during scanning:" + e.getMessage(), e);
        }

        // Валидация результатов
        if (foundCount == 0) {
            throw new RuntimeException("Error: no @HardwareConfig files found " + packageName);
        }
        if (foundCount > 1) {
            throw new RuntimeException("Error: Found more than 1 file with @HardwareConfig (" + foundCount + ").");
        }

        return foundConfig;
    }


    public static  <T> T getCustom(String name, Class<T> classOrInterface){
        Object custom = allCustom.get(name);
        BlazeLogger.addDefaultLog("Config","Got request for custom: "  + name + " with type" + classOrInterface.getSimpleName());
        if(custom == null){
            String errorMsg = "CUSTOM NOT FOUND: '" + name + "'\n";
            errorMsg += "Available electronic: " + allCustom.keySet() + "\n";
            throw new RuntimeException(errorMsg);
        }

        if (!classOrInterface.isInstance(custom)) {
            throw new RuntimeException(" TYPE MISMATCH for '" + name + "':\n" +
                    "  Expected: " + classOrInterface.getSimpleName() + "\n" +
                    "  Actual:   " + custom.getClass().getSimpleName() + "\n" +
                    "  Check your PowerTrain builder configuration!");
        }

        return classOrInterface.cast(custom);
    }



    public static SyncTopicBus getFastBus(){
        return BlazeRuntime.fastBus();
    }

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
