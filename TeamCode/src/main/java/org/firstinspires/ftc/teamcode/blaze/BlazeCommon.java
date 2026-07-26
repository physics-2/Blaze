package org.firstinspires.ftc.teamcode.blaze;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.teamcode.Testing.Configs.Constants;
import org.firstinspires.ftc.teamcode.blaze.Anotations.GetModule;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.BlazingModule;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.SyncTopicBus;

import java.lang.reflect.Field;
import java.util.Map;

public class BlazeCommon {

    private final Map<String, BlazingModule> allModules;
    private final HardwareMap hardwareMap;
    final MultiDashTelemetry telemetry;
    public Gamepad gamepad1;
    public Gamepad gamepad2;
    private Constants.Alliance currentAlliance;


    public BlazeCommon(Constants.Alliance alliance) {
        this.allModules = ModuleRegistry.getAll();
        this.hardwareMap = BlazeCore.getHardwareMap();
        this.telemetry = BlazeCore.getTelemetry();
        gamepad1 = BlazeCore.getGamepad1();
        gamepad2 = BlazeCore.getGamepad2();
        this.currentAlliance = alliance;

        autoWireModule();
    }

    public void setupAlliance(){
        if (currentAlliance == Constants.Alliance.BLUE) {
            onBlueAlliance();
        } else if (currentAlliance == Constants.Alliance.RED) {
            onRedAlliance();
        }
    }

    public void allModulesInit(){
        for(BlazingModule blazingModule :allModules.values()){
            blazingModule.commonInit();
        }
    }

    public void init() {}

    private void autoWireModule() {
        for (Field field : this.getClass().getDeclaredFields()) {
            // Проверяем, помечено ли поле аннотацией @GetModule
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
                                "Поле '" + field.getName() + "' помечено @GetModule, но не является наследником Module."
                        );
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "AutoWire не удалось выполнить для поля '" + field.getName() + "'", e
                    );
                }
            }
        }
    }

    public void update() {
        for (BlazingModule blazingModule : allModules.values()) {
            blazingModule.update();
        }
        addTelemetry();
        telemetry.update();
    }

    public void addTelemetry() {
        if(telemetry != null) {
            for (BlazingModule blazingModule : allModules.values()) {
                blazingModule.addTelemetry();
            }
        }
    }

    /**
     * Вызывается один раз при старте (start())
     */
    public void onStart() {
        for (BlazingModule blazingModule : allModules.values()) {
            blazingModule.onStart();
        }
    }

    public void onStop(){
        for (BlazingModule blazingModule : allModules.values()){
            blazingModule.onStop();
        }
    }

    public <T extends BlazingModule> void publishCommand(Class<T> moduleClass, String command) {
        T module = module(moduleClass);
        SyncTopicBus.publish(module.getName(), command);
    }

    /**
     * Публикация команды с аргументами в модуль по его классу
     */
    public <T extends BlazingModule> void publishCommand(Class<T> moduleClass, String command, Object args) {
        T module = module(moduleClass);
        SyncTopicBus.publish(module.getName(), new CommandWithArgs(command, args));
    }

    /**
     * Публикация произвольного объекта в модуль по его классу
     */
    public <T extends BlazingModule> void publishToModule(Class<T> moduleClass, Object data) {
        T module = module(moduleClass);
        SyncTopicBus.publish(module.getName(), data);
    }



    public void reset() {
        for (BlazingModule blazingModule : allModules.values()) {
            blazingModule.reset();
        }
    }

    public void setCurrentAlliance(Constants.Alliance currentAlliance) {
        this.currentAlliance = currentAlliance;
    }

    public Constants.Alliance getCurrentAlliance() {
        return currentAlliance;
    }

    public HardwareMap getHardwareMap() {
        return hardwareMap;
    }


    public MultiDashTelemetry getTelemetry() {
        return telemetry;
    }

    // --- Переопределяемые методы для настройки под альянс ---
    public void onRedAlliance() {}
    public void onBlueAlliance() {}

    // --- Пользовательская инициализация ---


    // --- Утилиты для быстрого доступа к модулям ---

    public BlazingModule module(String name) {
        BlazingModule blazingModule = allModules.get(name);
        if (blazingModule == null) {
            throw new RuntimeException("Module not found in HotWriteCommon: " + name);
        }
        return blazingModule;
    }

    @SuppressWarnings("unchecked")
    public <T extends BlazingModule> T module(Class<T> type) {
        for (BlazingModule blazingModule : allModules.values()) {
            if (type.isInstance(blazingModule)) {
                return (T) blazingModule;
            }
        }
        throw new RuntimeException("Module of type " + type.getSimpleName() + " not found in HotWriteCommon");
    }
}