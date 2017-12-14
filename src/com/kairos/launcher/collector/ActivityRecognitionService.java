package com.kairos.launcher.collector;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

public class ActivityRecognitionService extends IntentService {


    public ActivityRecognitionService() {
        super("ActivityRecognitionService");
    }

    public ActivityRecognitionService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if(ActivityRecognitionResult.hasResult(intent)) {
            Log.i("CollectorServiceLog", "MonitorReceivesRequest");
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result.getProbableActivities());
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        int mostSignificant = -1;
        int mostSignificantValue = 0;
        for( DetectedActivity activity : probableActivities ) {
           if(activity.getConfidence()>mostSignificantValue && activity.getType() != DetectedActivity.ON_FOOT) {
               mostSignificant = activity.getType();
           }
        }
        Intent result = new  Intent("kairos.CollectorService.ACTIVITY_MONITOR_RESULT");
        result.putExtra("ActivityNow",mostSignificant);
        sendBroadcast(result);
    }


}
