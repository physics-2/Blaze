package org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.PowerTrainSubsystem;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.blaze.Actuators.Actuator;
import org.firstinspires.ftc.teamcode.blaze.Actuators.SmartMotor;
import org.firstinspires.ftc.teamcode.blaze.Actuators.SmartServo;
import org.firstinspires.ftc.teamcode.blaze.Controllers.Controller;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.BlazingModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PowerTrain {

    private final List<Actuator> allActuators = new ArrayList<>();
    private final HashMap<String, Actuator> registry = new HashMap<>();


    private final double globalPowerScale = 1.0;
    private final boolean emergencyStop = false;
    private boolean isEnabled = true;
    String autowireModule;
    String name;

    private PowerTrain(List<Actuator> actuators, HashMap<String, Actuator> registry,String autowireModule,String name,boolean isMock) {
        this.autowireModule = autowireModule;
        this.allActuators.addAll(actuators);
        this.registry.putAll(registry);
        this.name = name;
        isEnabled = !isMock;
    }


    public String getName() {
        return name;
    }

    public <T extends Actuator> T get(Class<T> classOrInterface, String id) {
        Actuator actuator = registry.get(id);
        if (actuator == null) {

            String errorMsg = "ACTUATOR NOT FOUND: '" + id + "'\n";
            errorMsg += "Available electronic in the power train: " + registry.keySet() + "\n";


            for (String key : registry.keySet()) {
                if (key != null && key.equalsIgnoreCase(id)) {
                    errorMsg += "Hint: Did you mean '" + key + "'? \n";
                    break;
                }
            }
            throw new RuntimeException(errorMsg);
        }



        if (!classOrInterface.isInstance(actuator)) {
            throw new RuntimeException(" TYPE MISMATCH for '" + id + "':\n" +
                    "  Expected: " + classOrInterface.getSimpleName() + "\n" +
                    "  Actual:   " + actuator.getClass().getSimpleName() + "\n" +
                    "  Check your PowerTrain builder configuration!");
        }



        return classOrInterface.cast(actuator);
    }


    public List<Actuator> getAll() {
        return allActuators;
    }

    // --- Глобальное управление ---

    public void update() {
        if (emergencyStop) {
            stopAll();
            return;
        }
        if (!isEnabled) return;


        for (Actuator act : allActuators) {
            if(act.getController() != null){
                act.update();
            }

        }
    }

    public void setTarget(double target){
        for (Actuator act : allActuators) {
            if(act.getController() != null){
                act.setTarget(target);
            }

        }
    }

    public void setPower(double power){
        if(isEnabled) {
            for (Actuator act : allActuators) {

                act.setPower(power);


            }
        }
    }

    public void resetAll() {
        for (Actuator act : allActuators) act.reset();
    }

    public void stopAll() {
        for (Actuator act : allActuators) act.setPower(0);
    }


    public boolean isAllReady() {
        for (Actuator act : allActuators) {
            if (!act.isReady()) return false;
        }
        return true;
    }

    public List<String> getReadyActuators() {
        List<String> busy = new ArrayList<>();
        for (Actuator act : allActuators) {
            if (act.isReady()) busy.add(act.getID());
        }
        return busy;
    }

    @Override
    public String toString() {
        return "PowerTrain{" +
                "autowireModule='" + autowireModule + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public String getAutowireModule() {
        return autowireModule;
    }

    // --- BUILDER ---

    public static class builder {
        private final HardwareMap hardwareMap;
        private final HashMap<String, Actuator> registry = new HashMap<>();
        private final List<Actuator> allActuators = new ArrayList<>();

        private boolean isMock = false;


        private final String name ;

        private ActuatorGroup currentGroup = null;

        String autowireModule = null;
        public builder(HardwareMap hardwareMap,String name) {
            this.hardwareMap = hardwareMap;
            this.name = name;
        }



        private void register(Actuator actuator) {

            if (currentGroup != null) {
                currentGroup.addActuator(actuator);
            }
            if (registry.containsKey(actuator.getID())) {
                throw new RuntimeException("Duplicate Actuator ID: '" + actuator.getID() + "' in PowerTrain '" + name + "'");
            }
            registry.put(actuator.getID(), actuator);

            allActuators.add(actuator);
        }

        public builder addActuator(Actuator actuator){
            register(actuator);
            return this;
        }

        public builder addMotor(String name, DcMotorSimple.Direction direction, double ticksToDegrees, Controller controller,boolean isEncoderReversed,double maxPower) {
            SmartMotor actuator = new SmartMotor(hardwareMap,name,direction,ticksToDegrees,controller,isEncoderReversed,maxPower);

            register(actuator);
            return this;
        }

        public builder addMotor(String name, DcMotorSimple.Direction direction) {
            SmartMotor actuator = new SmartMotor(hardwareMap,name,direction);

            register(actuator);
            return this;
        }

        public builder addServo(String name,Servo.Direction  direction) {
            SmartServo actuator = new SmartServo(hardwareMap,name, direction);

            register(actuator);
            return this;
        }

        public String getAutowireModule() {
            return autowireModule;
        }

        public builder addServo(String name, Servo.Direction  direction,
                                double scaleMin, double rawMin,
                                double scaleMax, double rawMax) {
            SmartServo actuator = new SmartServo(hardwareMap,name, direction);
            actuator.scale(scaleMin,rawMin,scaleMax,rawMax);
            register(actuator);

            return this;
        }

        public builder addPowerTrain(PowerTrain powerTrain){
            if(powerTrain != null){
                for (Actuator act : powerTrain.getAll()) {
                    registry.put(act.getID(), act);
                }
                allActuators.addAll(powerTrain.getAll());

            }

            return this;
        }



        public builder startGroup(String groupId) {
            currentGroup = new ActuatorGroup(groupId);

            return this;
        }

        public builder disable() {
            isMock = true;

            return this;
        }

        public builder setGroupPositionMode(ActuatorGroup.PositionMode positionMode) {
            if(currentGroup != null){
                currentGroup.setPositionMode(positionMode);
            }
            else{
                throw new IllegalStateException("Call startGroup() first");
            }

            return this;
        }

        private Actuator getActuator(String name){
            if(registry.get(name) != null){
                return registry.get(name);
            }
            else{
                String errorMsg = "ACTUATOR NOT FOUND: '" + name + "'\n";
                errorMsg += "Available actuator: " + registry.keySet() + "\n";
                throw new RuntimeException(errorMsg);
            }
        }

        public builder setGroupLeader(String leader){
            if(currentGroup != null){

                currentGroup.setLeader(getActuator(leader));
            }
            else{
                throw new IllegalStateException("Call startGroup() first");
            }

            return this;
        }

        public builder endGroup() {
            registry.put(currentGroup.getID(), currentGroup);
            allActuators.add(currentGroup);
            currentGroup = null;
            return this;
        }

        public builder autowireTo(String name){
            autowireModule = name;
            return this;
        }

        public builder autowireTo(Class<? extends BlazingModule> clazz){
            autowireModule = clazz.getSimpleName();
            return this;
        }


        public PowerTrain build() {
            return new PowerTrain(allActuators, registry,autowireModule,name,isMock);
        }
    }


}