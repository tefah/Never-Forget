package com.tefah.neverforget.services;

import android.content.Context;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

public class ServiceUtils{


    private static final int DB_UPDATE_INTERVAL_MINUTES = 24*60;
    private static final int DB_UPDATE_INTERVAL_SECONDS = (int) (TimeUnit.MINUTES.toSeconds(DB_UPDATE_INTERVAL_MINUTES));
    private static final int SYNC_FLEXTIME_SECONDS = DB_UPDATE_INTERVAL_SECONDS;

    private static final String REMINDER_JOB_TAG = "get_facebook_events";
    private static boolean sInitialized;

    // COMPLETED (16) Create a synchronized, public static method called scheduleChargingReminder that takes
    // in a context. This method will use FirebaseJobDispatcher to schedule a job that repeats roughly
    // every DB_UPDATE_INTERVAL_SECONDS when the phone is charging. It will trigger WaterReminderFirebaseJobService
    // Checkout https://github.com/firebase/firebase-jobdispatcher-android for an example

    /**
     * This method will use FirebaseJobDispatcher to schedule a job that repeats roughly
     * every DB_UPDATE_INTERVAL_SECONDS
     * @param context
     */
    synchronized public static void scheduleChargingReminder(@NonNull final Context context) {


        // If the job has already been initialized, return
        if (sInitialized) return;

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        Job constraintReminderJob = dispatcher.newJobBuilder()
                /* The Service that will be used to write to preferences */
                .setService(UpdateDBService.class)
                /*
                 * Set the UNIQUE tag used to identify this Job.
                 */
                .setTag(REMINDER_JOB_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        0, DB_UPDATE_INTERVAL_SECONDS))
                .setReplaceCurrent(true)
                .build();
        dispatcher.schedule(constraintReminderJob);

        sInitialized = true;
    }
}