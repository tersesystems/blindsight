package example.conditional;

import io.timeandspace.cronscheduler.CronScheduler;
import io.timeandspace.cronscheduler.CronTask;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

// #periodic-cron
public class Periodic {
    private final CronScheduler cron = CronScheduler.create(Duration.ofMinutes(1));

    public void schedule(Duration duration, CronTask cronTask) {
        cron.scheduleAtFixedRateSkippingToLatest(0, duration.toMinutes(), TimeUnit.MINUTES, cronTask);
    }

    public void shutdown() {
        try {
            cron.shutdownNow();
            cron.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            // do nothing
        }
    }
}
// #periodic-cron
