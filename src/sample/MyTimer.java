package sample;

public class MyTimer {

    private long startTime, endTime;

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void stop() {
        endTime = System.currentTimeMillis();
    }

    public long getTime() {

        return (System.currentTimeMillis() - startTime);
    }
}