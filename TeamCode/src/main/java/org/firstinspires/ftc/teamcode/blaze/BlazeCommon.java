package org.firstinspires.ftc.teamcode.blaze;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.blaze.Anotations.GetModule;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.BlazingModule;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.SyncTopicBus;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Base class for common logic shared across OpModes using the Blaze framework.
 * <p>
 * Provides automatic module wiring, alliance-specific setup hooks, telemetry aggregation,
 * and convenient access to framework components like HardwareMap, Gamepads, and registered Modules.
 * </p>
 */
public class BlazeCommon {

    private final Map<String, BlazingModule> allModules;
    private final HardwareMap hardwareMap;
    protected final MultiDashTelemetry telemetry;
    protected Gamepad gamepad1;
    protected Gamepad gamepad2;
    protected Alliance currentAlliance;


    /**
     * Constructs the common base, capturing current framework state and triggering autowiring.
     *
     * @param alliance The current alliance color for this OpMode run.
     */
    public BlazeCommon(Alliance alliance) {
        this.allModules = BlazeRegistry.getAll();
        this.hardwareMap = BlazeCore.getHardwareMap();
        this.telemetry = BlazeCore.getTelemetry();
        gamepad1 = BlazeCore.getGamepad1();
        gamepad2 = BlazeCore.getGamepad2();
        this.currentAlliance = alliance;

        autoWireModule();
    }

    /**
     * Sets up alliance-specific configurations by calling the appropriate hook.
     */
    public void setupAlliance(){
        if (currentAlliance == Alliance.BLUE) {
            onBlueAlliance();
        } else if (currentAlliance == Alliance.RED) {
            onRedAlliance();
        }
    }

    /**
     * Calls the commonInit() method on all registered modules.
     */
    public void allModulesInit(){
        for(BlazingModule blazingModule :allModules.values()){
            blazingModule.commonInit();
        }
    }

    /**
     * Hook for custom initialization logic in the extending class.
     */
    public void init() {}

    /**
     * Automatically injects registered module instances into fields annotated with {@link GetModule}.
     */
    private void autoWireModule() {
        for (Field field : this.getClass().getDeclaredFields()) {
            // Check if the field is annotated with @GetModule
            if (field.isAnnotationPresent(GetModule.class)) {
                field.setAccessible(true);
                try {

                    Class<?> fieldType = field.getType();

                    if (BlazingModule.class.isAssignableFrom(fieldType)) {

                        @SuppressWarnings("unchecked")
                        Class<? extends BlazingModule> moduleClass = (Class<? extends BlazingModule>) fieldType;
                        BlazingModule blazingModuleInstance = module(moduleClass);
                        field.set(this, blazingModuleInstance);

                    } else {
                        throw new RuntimeException(
                                "Field '" + field.getName() + "' has @GetModule, but doesn't extend Module."
                        );
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            " Couldn't AutoWire for  field '" + field.getName() + "'", e
                    );
                }
            }
        }
    }

    /**
     * Calls the update() loop on all registered modules and updates telemetry.
     */
    public void update() {
        for (BlazingModule blazingModule : allModules.values()) {
            blazingModule.update();
        }
        addTelemetry();
        telemetry.update();
    }


    /**
     * Aggregates telemetry data from all registered modules.
     */
    public void addTelemetry() {
        if(telemetry != null) {
            for (BlazingModule blazingModule : allModules.values()) {
                blazingModule.addTelemetry();
            }
        }
    }

    /**
     * Called once at the start of the OpMode (start() phase).
     */
    public void onStart() {
        for (BlazingModule blazingModule : allModules.values()) {
            blazingModule.onStart();
        }
    }

    /**
     * Called at the end of the OpMode (stop() phase).
     */
    public void onStop(){
        for (BlazingModule blazingModule : allModules.values()){
            blazingModule.onStop();
        }
    }


    /**
     * Publishes a command with arguments to a specific module via the SyncTopicBus.
     *
     * @param moduleClass The target module class.
     * @param command     The command object to publish.
     * @param <T>         The type of the target module.
     */
    public <T extends BlazingModule> void publishCommand(Class<T> moduleClass, Command command) {
        T module = module(moduleClass);
        SyncTopicBus.publish(module.getName(), command);
    }

    /**
     * Publishes an arbitrary data object to a specific module via the SyncTopicBus.
     *
     * @param moduleClass The target module class.
     * @param data        The data object to publish.
     * @param <T>         The type of the target module.
     */
    public <T extends BlazingModule> void publishToModule(Class<T> moduleClass, Object data) {
        T module = module(moduleClass);
        SyncTopicBus.publish(module.getName(), data);
    }



    /**
     * Calls the reset() method on all registered modules.
     */
    public void reset() {
        for (BlazingModule blazingModule : allModules.values()) {
            blazingModule.reset();
        }
    }

    /**
     * Updates the current alliance.
     */
    public void setCurrentAlliance(Alliance currentAlliance) {
        this.currentAlliance = currentAlliance;
    }

    /**
     * @return The current alliance.
     */
    public Alliance getCurrentAlliance() {
        return currentAlliance;
    }


    /**
     * @return The global HardwareMap instance.
     */
    public HardwareMap getHardwareMap() {
        return hardwareMap;
    }


    /**
     * @return The global MultiDashTelemetry instance.
     */
    public MultiDashTelemetry getTelemetry() {
        return telemetry;
    }


    // =========================================================================
    // Overridable Alliance Hooks
    // =========================================================================
    public void onRedAlliance() {}
    public void onBlueAlliance() {}


    // =========================================================================
    // Utilities for Quick Module Access
    // =========================================================================

    /**
     * Retrieves a module by its registered string name.
     *
     * @param name The registered name of the module.
     * @return The module instance.
     * @throws RuntimeException if the module is not found.
     */
    public BlazingModule module(String name) {
        BlazingModule blazingModule = allModules.get(name);
        if (blazingModule == null) {
            throw new RuntimeException("Module not found in BlazeCommon: " + name);
        }
        return blazingModule;
    }

    /**
     * Retrieves a module by its class type.
     *
     * @param type The class type of the module.
     * @return The module instance.
     * @throws RuntimeException if the module is not found.
     */
    @SuppressWarnings("unchecked")
    public <T extends BlazingModule> T module(Class<T> type) {
        for (BlazingModule blazingModule : allModules.values()) {
            if (type.isInstance(blazingModule)) {
                return (T) blazingModule;
            }
        }
        throw new RuntimeException("Module of type " + type.getSimpleName() + " not found in " + this.getClass().getSimpleName());
    }
}