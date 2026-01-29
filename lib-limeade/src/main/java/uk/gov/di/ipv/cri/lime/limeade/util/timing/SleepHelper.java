package uk.gov.di.ipv.cri.lime.limeade.util.timing;

public class SleepHelper {

    public final long maxSleepTimeMs;

    public SleepHelper(long maxSleepTimeMs) {
        this.maxSleepTimeMs = maxSleepTimeMs;
    }

    /**
     * Calculates a wait time based on number of calls - starting from zero for the first call.
     * Using a busy wait
     */
    public long busyWaitWithExponentialBackOff(int callNumber) {

        long waitDuration = Math.min(calculateExponentialBackOffTimeMS(callNumber), maxSleepTimeMs);

        long startTime = System.currentTimeMillis();
        long futureTime = startTime + waitDuration;

        while (System.currentTimeMillis() < futureTime) {
            // Intended
        }

        long endTime = System.currentTimeMillis();
        return (endTime - startTime);
    }

    public long busyWaitMilliseconds(int milliseconds) {

        long waitDuration = Math.min(milliseconds, maxSleepTimeMs);

        long startTime = System.currentTimeMillis();
        long futureTime = startTime + waitDuration;

        while (System.currentTimeMillis() < futureTime) {
            // Intended
        }

        long endTime = System.currentTimeMillis();
        return (endTime - startTime);
    }

    private long calculateExponentialBackOffTimeMS(int callNumber) {

        if (callNumber == 0) {
            return 0;
        }

        int power = callNumber - 1;

        return ((long) Math.pow(2, power) * 100L);
    }
}
