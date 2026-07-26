package org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem;

import org.firstinspires.ftc.teamcode.blaze.Anotations.AutowireActuator;
import org.firstinspires.ftc.teamcode.blaze.Anotations.Command;
import org.firstinspires.ftc.teamcode.blaze.CommandWithArgs;
import org.firstinspires.ftc.teamcode.blaze.BlazeLogger;
import org.firstinspires.ftc.teamcode.blaze.MultiDashTelemetry;
import org.firstinspires.ftc.teamcode.blaze.Actuators.Actuator;
import org.firstinspires.ftc.teamcode.blaze.Actuators.Axon.SmartCRServo;
import org.firstinspires.ftc.teamcode.blaze.Actuators.SmartMotor;
import org.firstinspires.ftc.teamcode.blaze.Actuators.SmartServo;
import org.firstinspires.ftc.teamcode.blaze.BlazeCore;

import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.PowerTrainSubsystem.PowerTrain;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.Scheduler;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.SyncTopicBus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class BlazingModule {
    protected PowerTrain powerTrain;
    protected Scheduler scheduler;

    protected MultiDashTelemetry telemetry;
    private Map<String, Runnable> commandHandlers = new HashMap<>();
    private Map<String, Consumer<Object>> commandConsumers = new HashMap<>();
    protected String name;
    protected ModuleState moduleState;

    public BlazingModule(String name) {
        this.name = name;
        this.scheduler = BlazeCore.getScheduler();
        initPowerTrain();
        autoWireActuators();
        autoRegisterCommands();
        subscribe(name, this::processCommand);
        BlazeLogger.addDefaultLog("Module."+name,"Created");

    }

    public BlazingModule(String name, MultiDashTelemetry telemetry) {
        this.name = name;
        this.scheduler = BlazeCore.getScheduler();
        this.telemetry = telemetry;
        initPowerTrain();
        autoWireActuators();
        autoRegisterCommands();
        subscribe(name, this::processCommand);
        BlazeLogger.addDefaultLog("Module."+name,"Created");

    }

    private void initPowerTrain() {
        this.powerTrain = BlazeCore.getPowerTrainByModule(name);
        BlazeLogger.addDefaultLog("Module."+name,"Found autowire: " + powerTrain.getName());


    }

    private void autoWireActuators() {
        if (powerTrain == null) return;

        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            try {
                Class<?> fieldType = field.getType();
                if(field.isAnnotationPresent(AutowireActuator.class) && Actuator.class.isAssignableFrom(fieldType)){
                    @SuppressWarnings("unchecked")
                    Class<? extends Actuator> actuatorClass = (Class<? extends Actuator>) fieldType;
                    String actuatorName;
                    actuatorName = field.getAnnotation(AutowireActuator.class).name();
                    if(actuatorName.isEmpty()){
                        actuatorName = field.getName();
                    }
                    field.set(this, powerTrain.get(actuatorClass, actuatorName));
                    BlazeLogger.addDefaultLog("Module."+name,"Found " + actuatorClass.getSimpleName() +" actuator: " + actuatorName);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        "AutoWire failed for field '" + field.getName() +
                                "' in module '" + name + "'", e
                );
            }
        }
    }


    public void publish(String topic, Object message) {
        SyncTopicBus.publish(topic, message);
    }

    protected void subscribe(String topic, Consumer<Object> handler) {
        SyncTopicBus.subscribe(topic, handler);
    }

    protected void setState(ModuleState newState) {
        this.moduleState = newState;
        publish(name + "/state", newState);
    }

    protected void autoRegisterCommands() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            Command cmdAnnotation = method.getAnnotation(Command.class);
            if (cmdAnnotation != null) {
                method.setAccessible(true);
                String cmdName = cmdAnnotation.value();
                if (cmdName.isEmpty()){
                    cmdName = method.getName();
                }

                if (method.getParameterCount() == 0) {
                    commandHandlers.put(cmdName, () -> {
                        try {
                            method.invoke(this);
                        } catch (Exception e) {
                            BlazeLogger.addDefaultLog(name, "Error executing command '"  + "': " + e.getMessage());
                            throw new RuntimeException(e);
                        }
                    });
                } else if (method.getParameterCount() == 1) {
                    commandConsumers.put(cmdName, (args) -> {
                        try {
                            Class<?> paramType = method.getParameterTypes()[0];
                            Object convertedArg = convertArgument(args, paramType);
                            method.invoke(this, convertedArg);
                        } catch (Exception e) {
                            BlazeLogger.addDefaultLog(name, "Error executing command '"  + "': " + e.getMessage());
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    BlazeLogger.addDefaultLog(name, "Command '" + cmdName + "' has unsupported parameter count: " + method.getParameterCount());
                }
            }
        }
    }

    private Object convertArgument(Object arg, Class<?> targetType) {
        if (arg == null) return null;
        if (targetType.isInstance(arg)) return arg;

        // Конвертация примитивов
        if (targetType == double.class || targetType == Double.class) {
            return ((Number) arg).doubleValue();
        } else if (targetType == int.class || targetType == Integer.class) {
            return ((Number) arg).intValue();
        } else if (targetType == float.class || targetType == Float.class) {
            return ((Number) arg).floatValue();
        } else if (targetType == long.class || targetType == Long.class) {
            return ((Number) arg).longValue();
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return arg;
        } else if (targetType == String.class) {
            return arg.toString();
        }

        return arg;
    }

    public void onStop(){
        powerTrain.stopAll();
    }

    protected void processCommand(Object command) {
        if (command instanceof String) {
            String cmd = (String) command;
            Runnable handler = commandHandlers.get(cmd);
            if (handler != null) {
                handler.run();
            } else {
                BlazeLogger.addDefaultLog(name, "Unknown command: " + cmd);
            }
        } else if (command instanceof CommandWithArgs) {
            CommandWithArgs cmd = (CommandWithArgs) command;
            Consumer<Object> handler = commandConsumers.get(cmd.name);
            if (handler != null) {
                handler.accept(cmd.args);
            } else {
                BlazeLogger.addDefaultLog(name, "Unknown command with args: " + cmd.name);
            }
        }
    }
    public void addTelemetry(){
        if(telemetry == null) return;
        telemetry.addData(name.toUpperCase(),"");
    }
    public void onStart(){}
    public void update(){
        if(powerTrain != null){
            powerTrain.update();
        }

    }
    public void commonInit(){}

    public <T extends Actuator> T getAutoWiredActuator(String name,Class<T> classOrInterface){
        return powerTrain.get(classOrInterface,name);
    }


    public SmartMotor getAutoWiredMotor(String name){
        return powerTrain.get(SmartMotor.class,name);
    }

    public SmartServo getAutoWiredServo(String name){
        return powerTrain.get(SmartServo.class,name);
    }

    public SmartCRServo getAutoWiredCRServo(String name){
        return powerTrain.get(SmartCRServo.class,name);
    }

    public void reset(){
        powerTrain.resetAll();
    }

    public boolean isReady(){
       return powerTrain.isAllReady();
    }

    public ModuleState getModuleState() {
        return moduleState;
    }

    public String getName() {
        return name;
    }

    public void setModuleState(ModuleState moduleState) {
        this.moduleState = moduleState;
    }

    @Override
    public String toString() {
        return "Module{" +
                "name='" + name + '\'' +
                ", moduleState=" + moduleState +
                '}';
    }
}
