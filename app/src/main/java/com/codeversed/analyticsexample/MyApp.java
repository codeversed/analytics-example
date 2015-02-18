package com.codeversed.analyticsexample;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class MyApp extends Application {

    // Prevent hits from being sent to reports, i.e. during testing.
    private static final boolean GA_IS_DRY_RUN = false;

    // Global ID to track from all company apps.
    // You would replace this with your global (all apps) specific identifier.
    private static final String GLOBAL_PROPERTY_ID = "UA-43834478-2";

    // Key used to store a user's tracking preferences in SharedPreferences.
    private static final String TRACKING_PREF_KEY = "trackingPreference";

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app. eg: 'specific client' tracking
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: 'roll-up' tracking.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialization for Google Analytics Instance.
        initializeGa();
    }

    /*
     * Method to handle basic Google Analytics initialization.
     * All Google Analytics work occurs off the main thread.
     */
    private void initializeGa() {

        // Set dryRun flag.
        GoogleAnalytics.getInstance(this).setDryRun(GA_IS_DRY_RUN);

        // Set the log level to verbose if dryRun.
        // DEFAULT is set to DRY RUN (only logging will happen)
        GoogleAnalytics.getInstance(this).getLogger()
                .setLogLevel(GA_IS_DRY_RUN || BuildConfig.DEBUG ?
                        Logger.LogLevel.VERBOSE : Logger.LogLevel.WARNING);

        // Set the opt out flag when user updates a tracking preference.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
                if (key.equals(TRACKING_PREF_KEY)) {
                    GoogleAnalytics.getInstance(getApplicationContext()).setAppOptOut(
                            pref.getBoolean(key, false));
                }
            }
        });
    }

    synchronized Tracker getTracker(TrackerName trackerId) {

        // You would replace this with your client (single app) specific identifier.
        String CLIENT_PROPERTY_ID = null;

        // Debug builds use testing id to prevent compromising real report data.
        // You could have the same logic for the global id as well.
        if (BuildConfig.DEBUG) CLIENT_PROPERTY_ID = null;

        boolean hasClientId = (CLIENT_PROPERTY_ID != null && (!CLIENT_PROPERTY_ID.equals("")));

        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) && hasClientId ?
                    analytics.newTracker(CLIENT_PROPERTY_ID) :
                    analytics.newTracker(GLOBAL_PROPERTY_ID);
            mTrackers.put(trackerId, t);
        }

        return mTrackers.get(trackerId);
    }

    public Boolean sendOnlyGlobalAnalyticsEvent(
            String[] subsequentParams, HitBuilders.EventBuilder event) {

        // GLOBAL TRACKER
        // Skip Global if in debug.
        //if (!BuildConfig.DEBUG) {
        //    sendAnalyticsEvent(TrackerName.GLOBAL_TRACKER, subsequentParams, event);
        //}

        // Removed check so we start collecting data.
        // In real world you want to have the check above.
        sendAnalyticsEvent(TrackerName.GLOBAL_TRACKER, subsequentParams, event);

        return true;
    }

    public Boolean sendBothAnalyticsEvent(
            String[] subsequentParams, HitBuilders.EventBuilder event) {

        // GLOBAL TRACKER
        // Skip Global if in debug.
        //if (!BuildConfig.DEBUG) {
        //    sendAnalyticsEvent(TrackerName.GLOBAL_TRACKER, subsequentParams, event);
        //}

        // Removed check so we start collecting data.
        // In real world you want to have the check above.
        sendAnalyticsEvent(TrackerName.GLOBAL_TRACKER, subsequentParams, event);


        // APP TRACKER
        sendAnalyticsEvent(TrackerName.APP_TRACKER, subsequentParams, event);

        return true;
    }

    public Boolean sendAnalyticsEvent(TrackerName trackerName, String[] subsequentParams,
                                      HitBuilders.EventBuilder event) {

        // Get tracker
        Tracker tracker = getTracker(trackerName);

        if (tracker == null) return false;

        // Set subsequent parameters
        if (subsequentParams != null) {
            if (subsequentParams.length >= 1 && subsequentParams[0] != null) {
                tracker.setScreenName(subsequentParams[0]);
            }
            if (subsequentParams.length >= 2 && subsequentParams[1] != null) {
                tracker.setTitle(subsequentParams[1]);
            }
        }

        // Send EventBuilder Map
        tracker.send(event.build());

        // Clear subsequent parameters
        tracker.setScreenName(null);
        tracker.setTitle(null);

        return true;
    }

    public Boolean sendBothAnalyticsScreen(String screenName) {

        // GLOBAL TRACKER
        // Skip Global if in debug.
        //if (!BuildConfig.DEBUG) {
        //    sendAnalyticsScreen(TrackerName.GLOBAL_TRACKER, screenName);
        //}

        // Removed check so we start collecting data.
        // In real world you want to have the check above.
        sendAnalyticsScreen(TrackerName.GLOBAL_TRACKER, screenName);

        // APP TRACKER
        sendAnalyticsScreen(TrackerName.APP_TRACKER, screenName);

        return true;
    }

    public Boolean sendAnalyticsScreen(TrackerName trackerName, String screenName) {

        // Get tracker
        Tracker tracker = getTracker(trackerName);

        if (tracker == null) return false;

        // Set the screen name.
        tracker.setScreenName(screenName);

        // Send AppView hit.
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        return true;
    }

    public Boolean sendBothAnalyticsTiming(HitBuilders.TimingBuilder event) {

        // GLOBAL TRACKER
        // Skip Global if in debug.
        //if (!BuildConfig.DEBUG) {
        //    sendAnalyticsEvent(TrackerName.GLOBAL_TRACKER, subsequentParams, event);
        //}

        // Removed check so we start collecting data.
        // In real world you want to have the check above.
        sendAnalyticsTiming(TrackerName.GLOBAL_TRACKER, event);

        // APP TRACKER
        sendAnalyticsTiming(TrackerName.APP_TRACKER, event);

        return true;
    }

    public Boolean sendAnalyticsTiming(TrackerName trackerName,
                                       HitBuilders.TimingBuilder event) {

        // Get tracker
        Tracker tracker = getTracker(trackerName);

        if (tracker == null) return false;

        // Send TimingBuilder Map
        tracker.send(event.build());

        return true;
    }
}
