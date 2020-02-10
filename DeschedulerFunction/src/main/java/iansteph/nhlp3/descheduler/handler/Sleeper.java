package iansteph.nhlp3.descheduler.handler;

public class Sleeper implements Sleep {

    public void sleep(final int numberOfMillisecondsToSleep) {

        try {
            Thread.sleep(numberOfMillisecondsToSleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
