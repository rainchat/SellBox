package com.rainchat.sellbox.data;

public class TimeSaver {

    private final int hours;
    private final int minutes;
    private boolean isComplete;

    public TimeSaver(int hours, int minutes) {
        this.hours = hours;
        this.minutes = minutes;
        this.isComplete = false;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }
}
