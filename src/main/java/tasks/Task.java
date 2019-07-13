package tasks;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Task extends TimerTask {
    private static final long MILLI_TO_MIN_COF = 60000;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private long loopTime;
    private long delay;

    public Task(long loopTimeInMinutes, long delayInMinutes) {
        this.loopTime = loopTimeInMinutes;
        this.delay = delayInMinutes;
    }

    boolean isNotRunning() {
        return !running.get();
    }

    public void scheduleTask() {
        if (isNotRunning()) {
            running.set(true);
            Timer timer = new Timer(true);
            timer.scheduleAtFixedRate(this, delay * MILLI_TO_MIN_COF,
                loopTime * MILLI_TO_MIN_COF);
        }
    }

    public abstract void execute();

    @Override
    public void run() {
        execute();
    }
}
