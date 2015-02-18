package com.codeversed.analyticsexample;

import android.content.Context;

import com.google.android.gms.analytics.HitBuilders;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeElapseUtils {

    private Map<String, TimeTrack> trackTrackMap = new HashMap<>();

    private static TimeElapseUtils instance = null;
    private static Context mContext;

    protected TimeElapseUtils(Context context) {
        if (mContext == null) mContext = context;
    }

    public static TimeElapseUtils getInstance(Context context) {
        if(instance == null) {
            synchronized(TimeElapseUtils.class) {
                if(instance == null) {
                    instance = new TimeElapseUtils(context);
                }
            }
        }
        return instance;
    }

    public void trackStart(String key, TimeTrack timeObj) {
        if (trackTrackMap.containsKey(key)) {
            if (BuildConfig.DEBUG) System.out.println("Track Time Restart - " + key);
        }

        trackTrackMap.put(key, timeObj);
    }

    public void trackStop(String key) {

        // Get TimeTrack Object with key
        TimeTrack timeObj = trackTrackMap.get(key);
        if (timeObj == null) return;

        // Analytics Tracking
        ((MyApp) mContext.getApplicationContext()).sendBothAnalyticsTiming(
                new HitBuilders.TimingBuilder()
                        .setCategory(timeObj.category)
                        .setVariable(timeObj.name)
                        .setLabel(timeObj.label)
                        .setValue(timeElapse(timeObj)));

        // Remove completed TimeTrack obj from map
        trackTrackMap.remove(key);
    }

    private long timeElapse(TimeTrack timeObj) {
        return getCurrentTime(timeObj.timeUnit) - timeObj.startTime;
    }

    private static long getCurrentTime(TimeUnit timeUnit) {

        switch (timeUnit)
        {
            case NANOSECONDS:
                return System.nanoTime();

            case MILLISECONDS:
                return System.currentTimeMillis();

            default:
                return System.currentTimeMillis();
        }

    }

    public static class TimeTrack {

        TimeUnit timeUnit;
        long startTime;

        String category;
        String name;
        String label;

        public TimeTrack(TimeUnit timeUnit, String category, String name, String label) {
            this.timeUnit = timeUnit;
            this.category = category;
            this.name = name;
            this.label = label;
            this.startTime = getCurrentTime(timeUnit);
        }

    }

}