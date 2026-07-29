package org.firstinspires.ftc.teamcode.blaze.blaze.MultiThread;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.blaze.blaze.BlazeLogger;

import java.util.concurrent.atomic.AtomicLong;

@TeleOp(name = "Scheduler Test", group = "Test")
public class MTTest extends OpMode {

    private TopicBus bus;
    private ExecutorManager executors;
    private Scheduler scheduler;
    private CoopScheduler coop;

    private final AtomicLong controlTicks = new AtomicLong();
    private final AtomicLong slowTaskRuns = new AtomicLong();
    private final AtomicLong visionRuns = new AtomicLong();
    private final AtomicLong oneShotHeavyRuns = new AtomicLong();
    private final AtomicLong onceRuns = new AtomicLong();
    private final AtomicLong heartbeat = new AtomicLong();
    private final AtomicLong topicReceived = new AtomicLong();
    private final AtomicLong batchFlushed = new AtomicLong();
    private final AtomicLong overrunCount = new AtomicLong();

    private final long lastLoopNs = 0;
    private final long loopDtUs = 0;
    private final long coopUs = 0;
    private final long batchUs = 0;

    private final long lastTopicPublishMs = 0;

    private final boolean lastA = false;
    private final boolean lastB = false;

    private Runnable batchTask;
    private Runnable visionTask;




    private static final Topic<Double> TEST_TOPIC =
            Topic.of("test/topic", Double.class);

    @Override
    public void init() {
        bus = new TopicBus();
        executors = new ExecutorManager();
        scheduler = new Scheduler(executors);

        coop = new CoopScheduler(2);



        batchTask = new Runnable() {
            @Override
            public void run() {
                batchFlushed.incrementAndGet();
            }
        };

        visionTask = new Runnable() {
            @Override
            public void run() {
                visionRuns.incrementAndGet();

                // Имитация тяжёлой vision-задачи.


            }
        };



        // Маленькая быстрая задача.
        coop.addOrReplace("control.fake", 10, new Runnable() {
            @Override
            public void run() {
                controlTicks.incrementAndGet();
            }
        });

        // Медленная задача, которая специально немного тормозит.
        // Нужна, чтобы видеть overrun.


        // Безопасная периодика.
        // Не scheduleAtFixedRate.
        scheduler.scheduleAtFixedRate("heartbeat", 100, heartbeat::incrementAndGet);
    }

    @Override
    public void loop() {
        telemetry.log().add("NEw line each time" + Math.random());
        if(Math.random() > 0.7){
            BlazeLogger.addWarnLog("","Random > 0.7!" + Math.random());
        }



        telemetry.update();
    }

    @Override
    public void stop() {
        if (scheduler != null) {
            scheduler.cancelAll();
        }

        if (executors != null) {
            executors.shutdown();
        }

        if (bus != null) {
            bus.clear();
        }

        if (coop != null) {
            coop.clear();
        }
    }


}