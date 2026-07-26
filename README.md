
**Welcome to B.L.A.Z.E. Framework!**

***(Base Layer Architecture for Zero-boilerplate Execution)***
#

A modern, annotation-driven framework for **FTC** (FIRST Tech Challenge) that eliminates boilerplate code and provides a clean, type-safe API for robot control.It is currently under development,so it needs pedropathing for now

#
**Why BLAZE?**

Tired of writing the same hardware initialization code in every OpMode? BLAZE solves this with:

**Zero Boilerplate** - Define your robot once, use it everywhere

**Annotation-Driven** - @AutowireActuator, @Command, @GetModule do the magic

**Modular Architecture** - Isolate logic into independent, testable modules

**Clean Code** - Write TeleOp in 3 lines, not 30

#
**Quick Start**

**1. Define your Hardware Configuration:**
```java
@HardwareConfig
public class RobotConfig extends ElectronicsConfig {
public RobotConfig(){}
@Override
public void addElectronicsFull(HardwareMap hardwareMap) {
	HotWrite.addPowerTrain(new PowerTrain.builder(hardwareMap, "MainDrive")
		.addMotor("leftMotor", DcMotorSimple.Direction.FORWARD)
		.addMotor("rightMotor", DcMotorSimple.Direction.REVERSE)
		.autowireTo("Chassis")
		.build()
	);
	}
}
```
**2. Create a Module:**
```java
@HotModule
@InitWithTelemetry
public class Chassis extends Module {

	public Chassis(String name) {
        super(name);
    }

    public Chassis(String name, MultiDashTelemetry telemetry) {
        super(name, telemetry);
    }
	
	@AutowireActuator
	SmartMotor leftMotor;
	
	@AutowireActuator
	SmartMotor rightMotor;
	
	@Command("forward")
	public void moveForward() {
		leftMotor.setPower(0.5);
		rightMotor.setPower(0.5);
	}
	
	@Command("stop")
	public void stop() {
		leftMotor.setPower(0);
		rightMotor.setPower(0);
	}
}
```

**3. Create your Core:**
```java
public class TeleCore extends BlazeCommon {
    @GetModule
    Chassis chassis;
    
    public TeleCore(Constants.Alliance alliance) {
        super(alliance);
    }
}
```
**4. Write your TeleOp (1 function!):**
```java
@TeleOp(name = "Main TeleOp")
public class MainTeleOp extends BlazeTeleOp<TeleCore> {
    @Override
    protected TeleCore createCore() {
        return new TeleCore(Constants.Alliance.BLUE);
    }
}
```

**That's it!** Your robot is ready to run.

#
**Core Concepts**
#


**Actuator** *(Lowest Level)*:

Wraps hardware (motors, servos) with smart features:

1.*PID control built-in*

2.*Automatic encoder handling*

3.*Power limiting*

4.*Position/Velocity modes*

```java
SmartMotor motor = new SmartMotor(hardwareMap, "motor", Direction.FORWARD);
motor.setTarget(100); // Automatic PID control
motor.setPower(0.5);  // Raw power mode
```


#
**PowerTrain**(Hardware Grouping)

*Groups related actuators together:*

1.*Bulk operations (setPower, stopAll)*

2.*Actuator groups with leaders*

3.*Mock mode for testing*

```java
PowerTrain drive = new PowerTrain.builder(hardwareMap, "Drive")
    .addMotor("left", Direction.FORWARD)
    .addMotor("right", Direction.REVERSE)
    .startGroup("motors")
        .setGroupLeader("left")
        .setGroupPositionMode(PositionMode.ABSOLUTE)
    .endGroup()
    .build();
```


#
**Module**(Logic Unit)

1.*Encapsulates robot functionality:*

2.*Auto-wired actuators from PowerTrains*

3.*Command pattern via @Command*

4.*Pub/Sub messaging via SyncTopicBus*

5.*Telemetry integration*

```java
@HotModule
public class Shooter extends Module {
    @AutowireActuator
    SmartMotor flywheel;
    
    @Command("shoot")
    public void shoot() {
        flywheel.setTarget(3000); 
    }
}
```

#
**Common**(Orchestrator)

*Ties everything together for an OpMode:*

1.*Auto-wired modules via @GetModule*

2.*Alliance-specific setup*

3.*Command publishing*

```java
public class TeleCore extends BlazeCommon {
    @GetModule
    Shooter shooter;
    
    @Override
    public void update() {
        super.update();
        if (gamepad1.a) {
            publishCommand(Shooter.class, "shoot");
        }
    }
}
```
#
**Key Features**
#

***Annotation Magic***

**@AutowireActuator** - *Automatically inject actuators into modules:*
```java
@AutowireActuator(name = "flywheel") // Explicit name
SmartMotor motor;

@AutowireActuator // Uses field name
SmartServo servo;
```


**@Command** - *Register methods as commands:*
```java
@Command("up")
public void liftUp() { ... }

@Command("setSpeed")
public void setSpeed(double speed) { ... }
```

**@GetModule** - *Auto-inject modules into Core:*
```java
@GetModule
Shooter shooter;
```

#
***Pub/Sub Messaging***

*Modules communicate without tight coupling:*
```java
// Publisher
publish("shooter/command", "shoot");

// Subscriber (in Shooter module)
subscribe("shooter/command", cmd -> {
    if (cmd.equals("shoot")) shoot();
});
```

***Actuator Groups***

*Control multiple actuators as one:*
```java
PowerTrain lift = new PowerTrain.builder(hardwareMap, "Lift")
	.startGroup("liftMotors")
    .addMotor("leftLift", Direction.FORWARD)
    .addMotor("rightLift", Direction.REVERSE)
	.setGroupLeader("leftLift")
    .endGroup()
    .build();

@AutowireActuator
ActuatorGroup group;
group.setTarget(500); // Both motors move together
```

***Alliance-Specific Setup***

```java
public class TeleCore extends BlazeCommon {
    @Override
    public void onBlueAlliance() {
        module(Chassis.class).invertDrive();
    }
    
    @Override
    public void onRedAlliance() {
        // Default behavior
    }
}
```

***Mock Mode (Testing)***

```java
PowerTrain testDrive = new PowerTrain.builder(hardwareMap, "Test")
    .addMotor("motor", Direction.FORWARD)
    .disable() // Mock mode - no hardware calls
    .build();
```
