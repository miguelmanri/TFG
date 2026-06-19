package metrics;

public class MetricsManager {

    private long startTime;

    public void startTimer() {
        startTime = System.currentTimeMillis();
    }

    public long stopTimer() {
        return System.currentTimeMillis() - startTime;
    }
}