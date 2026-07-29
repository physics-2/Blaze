package org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.PowerTrainSubsystem;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.blaze.Actuators.Actuator;
import org.firstinspires.ftc.teamcode.blaze.Actuators.SmartMotor;
import org.firstinspires.ftc.teamcode.blaze.Controllers.Controller;

import java.util.ArrayList;
import java.util.List;

public class ActuatorGroup implements Actuator {


    public enum PositionMode {
        LEADER,
        AVERAGE,
        MIN,
        MAX
    }

    private final String id;
    private final List<Actuator> actuators = new ArrayList<>();
    private Actuator leader;
    private PositionMode positionMode = PositionMode.LEADER;

    private double lastTarget = 0;

    public ActuatorGroup(String id) {
        this.id = id;
    }


    public ActuatorGroup(String id, Actuator leader,Actuator slave) {
        this.id = id;
        setLeader(leader);
        addActuator(slave);
    }

    public ActuatorGroup setLeader(Actuator leader) {
        this.leader = leader;
        if (leader != null && !actuators.contains(leader)) {
            actuators.add(leader);
        }
        return this;
    }

    public ActuatorGroup addActuator(Actuator actuator) {
        if (actuator != null && !actuators.contains(actuator)) {
            actuators.add(actuator);
        }
        return this;
    }


    public ActuatorGroup addActuators(Actuator... newActuators) {
        if (newActuators != null) {
            for (Actuator act : newActuators) {
                addActuator(act);
            }
        }
        if (leader == null && !actuators.isEmpty()) {
            leader = actuators.get(0);
        }
        return this;
    }

    public ActuatorGroup setPositionMode(PositionMode mode) {
        this.positionMode = mode;
        return this;
    }


    /**
     * Gets current from motors,because others don't have the needed method
     * @return current from motors
     */
    public double getCurrent(){
        double totalCurrent = 0;
        for (Actuator actuator : actuators) {
            if(actuator instanceof SmartMotor){
                totalCurrent += ((SmartMotor) actuator).getCurrent(CurrentUnit.AMPS);
            }
        }
        return totalCurrent;
    }

    @Override
    public String getID() {
        return id;
    }


    @Override
    public void setPower(double power) {
        for (Actuator act : actuators) {
            act.setPower(power);
        }
    }

    @Override
    public void setTarget(double target) {
        this.lastTarget = target;
        for (Actuator act : actuators) {
            act.setTarget(target);
        }
    }

    @Override
    public void setController(Controller controller) {
        for (Actuator act : actuators) {
            act.setController(controller);
        }
    }

    @Override
    public Controller getController() {
        return leader != null ? leader.getController() : actuators.get(0).getController();
    }

    @Override
    public void setMode(TargetMode targetMode) {
        for (Actuator act : actuators) {
            act.setMode(targetMode);
        }
    }



    @Override
    public TargetMode getMode() {
        return leader != null ? leader.getMode() : TargetMode.POSITION;
    }

    @Override
    public void setTolerance(double tolerance) {
        for (Actuator act : actuators) {
            act.setTolerance(tolerance);
        }
    }

    @Override
    public void update() {
        for (Actuator act : actuators) {
            act.update();
        }
    }

    @Override
    public void reset() {
        for (Actuator act : actuators) {
            act.reset();
        }
    }


    /**
     * Returns the current PID feedback
     * @return position,or velocity if in the VELOCITY mode
     */
    @Override
    public double getPosition() {
        if (actuators.isEmpty()) return 0;
        if (leader == null) leader = actuators.get(0);

        switch (positionMode) {
            case AVERAGE:
                double sum = 0;
                for (Actuator act : actuators) sum += act.getPosition();
                return sum / actuators.size();
            case MIN:
                double min = Double.MAX_VALUE;
                for (Actuator act : actuators) min = Math.min(min, act.getPosition());
                return min;
            case MAX:
                double max = Double.NEGATIVE_INFINITY;
                for (Actuator act : actuators) max = Math.max(max, act.getPosition());
                return max;
            default:
                return leader.getPosition();
        }
    }



    public double getLastTarget() {
        return lastTarget;
    }

    @Override
    public boolean isReady() {

        for (Actuator act : actuators) {
            if (!act.isReady()) return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return "ActuatorGroup{" +
                "leader=" + leader +
                ", lastTarget=" + lastTarget +
                '}';
    }

    public Actuator getActuator(String id) {
        for (Actuator act : actuators) {
            if (act.getID().equals(id)) return act;
        }
        return null;
    }

    public List<Actuator> getActuators() {
        return actuators;
    }

    public int size() {
        return actuators.size();
    }
}