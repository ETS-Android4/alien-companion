package com.gDyejeekis.aliencompanion.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by George on 4/19/2017.
 */

public class SyncSchedule implements Serializable {

    private static final long serialVersionUID = 1422345L;

    static class TimeWindow {

        long windowStart;
        long windowLength;

        TimeWindow(long startTime, long windowLength) {
            this.windowStart = startTime;
            this.windowLength = windowLength;
        }
    }

    private final int scheduleId;
    private int startTime;
    private int endTime;
    private String days;

    public SyncSchedule() {
        scheduleId = UUID.randomUUID().hashCode();
        startTime = -1;
        endTime = -1;
        days = "";
    }

    public SyncSchedule(int startTime, int endTime, String days) {
        this.scheduleId = UUID.randomUUID().hashCode();
        this.startTime = startTime;
        this.endTime = endTime;
        this.days = days;
    }

    public List<TimeWindow> getSyncTimeWindows() {
        List<TimeWindow> timeWindows = new ArrayList<>();

        int windowStart = (startTime > endTime) ? endTime : startTime;
        int windowLength = Math.abs(startTime - endTime);
        if(windowLength==0) windowLength = 1;

        long windowLengthMillis = TimeUnit.HOURS.toMillis(windowLength);

        Calendar cur_cal = new GregorianCalendar();
        cur_cal.setTimeInMillis(System.currentTimeMillis());
        int currentDay = cur_cal.get(Calendar.DAY_OF_WEEK);
        int currentHour = cur_cal.get(Calendar.HOUR_OF_DAY);

        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        //calendar.set(Calendar.WEEK_OF_MONTH, cur_cal.get(Calendar.WEEK_OF_MONTH));

        if(days.contains("mon")) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar.set(Calendar.HOUR_OF_DAY, windowStart);
            long windowStartMillis = calendar.getTimeInMillis();
            if(currentDay > Calendar.MONDAY || (currentDay == Calendar.MONDAY && currentHour >= windowStart)) {
                windowStartMillis += TimeUnit.DAYS.toMillis(7);
            }

            timeWindows.add(new TimeWindow(windowStartMillis, windowLengthMillis));
        }
        if(days.contains("tue")) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
            calendar.set(Calendar.HOUR_OF_DAY, windowStart);
            long windowStartMillis = calendar.getTimeInMillis();
            if(currentDay > Calendar.TUESDAY || (currentDay == Calendar.TUESDAY && currentHour >= windowStart)) {
                windowStartMillis += TimeUnit.DAYS.toMillis(7);
            }

            timeWindows.add(new TimeWindow(windowStartMillis, windowLengthMillis));
        }
        if(days.contains("wed")) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
            calendar.set(Calendar.HOUR_OF_DAY, windowStart);
            long windowStartMillis = calendar.getTimeInMillis();
            if(currentDay > Calendar.WEDNESDAY || (currentDay == Calendar.WEDNESDAY && currentHour >= windowStart)) {
                windowStartMillis += TimeUnit.DAYS.toMillis(7);
            }

            timeWindows.add(new TimeWindow(windowStartMillis, windowLengthMillis));
        }
        if(days.contains("thu")) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
            calendar.set(Calendar.HOUR_OF_DAY, windowStart);
            long windowStartMillis = calendar.getTimeInMillis();
            if(currentDay > Calendar.THURSDAY || (currentDay == Calendar.THURSDAY && currentHour >= windowStart)) {
                windowStartMillis += TimeUnit.DAYS.toMillis(7);
            }

            timeWindows.add(new TimeWindow(windowStartMillis, windowLengthMillis));
        }
        if(days.contains("fri")) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
            calendar.set(Calendar.HOUR_OF_DAY, windowStart);
            long windowStartMillis = calendar.getTimeInMillis();
            if(currentDay > Calendar.FRIDAY || (currentDay == Calendar.FRIDAY && currentHour >= windowStart)) {
                windowStartMillis += TimeUnit.DAYS.toMillis(7);
            }

            timeWindows.add(new TimeWindow(windowStartMillis, windowLengthMillis));
        }
        if(days.contains("sat")) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            calendar.set(Calendar.HOUR_OF_DAY, windowStart);
            long windowStartMillis = calendar.getTimeInMillis();
            if(currentDay > Calendar.SATURDAY || (currentDay == Calendar.SATURDAY && currentHour >= windowStart)) {
                windowStartMillis += TimeUnit.DAYS.toMillis(7);
            }

            timeWindows.add(new TimeWindow(windowStartMillis, windowLengthMillis));
        }
        if(days.contains("sun")) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            calendar.set(Calendar.HOUR_OF_DAY, windowStart);
            long windowStartMillis = calendar.getTimeInMillis();
            if(currentDay > Calendar.SUNDAY || (currentDay == Calendar.SUNDAY && currentHour >= windowStart)) {
                windowStartMillis += TimeUnit.DAYS.toMillis(7);
            }

            timeWindows.add(new TimeWindow(windowStartMillis, windowLengthMillis));
        }

        return timeWindows;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public String getDays() {
        return days;
    }

    public int getScheduleId() {
        return scheduleId;
    }

}
