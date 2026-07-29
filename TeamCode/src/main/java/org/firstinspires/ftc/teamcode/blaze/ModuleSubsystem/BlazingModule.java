package org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem;

import org.firstinspires.ftc.teamcode.blaze.Actuators.Actuator;
import org.firstinspires.ftc.teamcode.blaze.Actuators.Axon.SmartCRServo;
import org.firstinspires.ftc.teamcode.blaze.Actuators.SmartMotor;
import org.firstinspires.ftc.teamcode.blaze.Actuators.SmartServo;
import org.firstinspires.ftc.teamcode.blaze.Anotations.AutowireActuator;
import org.firstinspires.ftc.teamcode.blaze.BlazeCore;
import org.firstinspires.ftc.teamcode.blaze.BlazeLogger;
import org.firstinspires.ftc.teamcode.blaze.Command;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.PowerTrainSubsystem.PowerTrain;
import org.firstinspires.ftc.teamcode.blaze.MultiDashTelemetry;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.Scheduler;
import org.firstinspires.ftc.teamcode.blaze.MultiThread.SyncTopicBus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class BlazingModule {
    private interface CommandExecutor {
        void execute(Object[] args) throws Exception;
    }
    protected PowerTrain powerTrain;
    protected Scheduler scheduler;

    protected MultiDashTelemetry telemetry;

    private final Map<String, CommandExecutor> commandHandlers = new HashMap<>();
    protected String name;
    protected ModuleState moduleState;

    protected BlazingModule() {}
    public void autowireSelf(String name, MultiDashTelemetry telemetry){
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
        if(powerTrain == null) throw new RuntimeException("Module " + getName() +" was autowired,but found no coresponding powertrains."+
                "Please add a powerTrain autowired to this module");
        BlazeLogger.addDefaultLog("Module."+name,"Found autowire: " + powerTrain.getName());
    }

    private void autoWireActuators() {
        if (powerTrain == null) return;
        long startTime = System.currentTimeMillis();
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
        long timeToEnd = System.currentTimeMillis() - startTime;
        BlazeLogger.addDefaultLog("Module."+name,"Took " + timeToEnd + " ms to autowire self");
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
            org.firstinspires.ftc.teamcode.blaze.Anotations.Command cmdAnnotation = method.getAnnotation(org.firstinspires.ftc.teamcode.blaze.Anotations.Command.class);
            if (cmdAnnotation != null) {
                method.setAccessible(true);
                String cmdName = cmdAnnotation.value().isEmpty() ? method.getName() : cmdAnnotation.value();
                Class<?>[] paramTypes = method.getParameterTypes();

                commandHandlers.put(cmdName, (args) -> {
                    try {
                        // 1. Проверка количества аргументов
                        if (args.length != paramTypes.length) {
                            throw new IllegalArgumentException(
                                    "Command '" + cmdName + "' expects " + paramTypes.length +
                                            " args, but got " + args.length
                            );
                        }

                        Object[] finalArgs = new Object[paramTypes.length];
                        for (int i = 0; i < paramTypes.length; i++) {
                            finalArgs[i] = convertArgument(args[i], paramTypes[i]);
                        }

                        method.invoke(this, finalArgs);

                    } catch (Exception e) {
                        Throwable cause = (e instanceof java.lang.reflect.InvocationTargetException)
                                ? ((java.lang.reflect.InvocationTargetException) e).getTargetException()
                                : e;
                        BlazeLogger.addErrorLog(name, "Error executing command '" + cmdName + "': " + cause.getMessage());
                        throw new RuntimeException(cause);
                    }
                });
            }
        }
    }

    private Object convertArgument(Object arg, Class<?> targetType) {
        if (arg == null) {
            if (targetType.isPrimitive()) {
                throw new IllegalArgumentException("Cannot pass null to primitive parameter: " + targetType.getSimpleName());
            }
            return null;
        }

        if (arg instanceof Number) {
            Number num = (Number) arg;
            if (targetType == double.class || targetType == Double.class) return num.doubleValue();
            if (targetType == int.class || targetType == Integer.class) return num.intValue();
            if (targetType == float.class || targetType == Float.class) return num.floatValue();
            if (targetType == long.class || targetType == Long.class) return num.longValue();
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            if (arg instanceof Boolean) return arg;
            return Boolean.parseBoolean(String.valueOf(arg));
        }

        if (targetType == String.class) return String.valueOf(arg);

        if (targetType.isInstance(arg)) return arg;


        String argStr = String.valueOf(arg);
        try {
            if (targetType == double.class || targetType == Double.class) return Double.parseDouble(argStr);
            if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(argStr);
            if (targetType == float.class || targetType == Float.class) return Float.parseFloat(argStr);
            if (targetType == long.class || targetType == Long.class) return Long.parseLong(argStr);

        } catch (NumberFormatException e) {
            BlazeLogger.addErrorLog("BlazingModule", "Failed to convert arg '" + arg + "' to " + targetType.getSimpleName());
        }
        return arg;
    }

    public void onStop(){
        powerTrain.stopAll();
    }

    protected void processCommand(Object command) {
        if (command instanceof String) {
            String cmd = (String) command;
            CommandExecutor handler = commandHandlers.get(cmd);
            if (handler != null) {
                try { handler.execute(new Object[0]); }
                catch (Exception e) { throw new RuntimeException(e); }
            } else {
                BlazeLogger.addWarnLog(name, "Unknown command: " + cmd);
            }

        } else if (command instanceof Command) {
            Command cmd = (Command) command;
            CommandExecutor handler = commandHandlers.get(cmd.name);


            if (handler != null) {
                try { handler.execute(cmd.args); }
                catch (Exception e) { throw new RuntimeException(e); }
            } else {
                BlazeLogger.addWarnLog(name, "Unknown command: " + cmd.name);
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
