package com.kairos.launcher.collector;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;

public class CollectorService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String NULL_APP = "NULL";

    private CollectorServiceBinder mCollectorServiceBinder = new CollectorServiceBinder();;
    private boolean inCycle;
    private LocationManager locationManager;
    private PackageManager packageManager;
    private LocationListener locationListener;
    private Location lastLocation;
    private boolean checkLocation = false;
    private LogDatabaseHelper logDatabaseHelper;
    private GoogleApiClient googleApiClient;
    private int userActivityNow = 4; // 4 is Detected activity UNKNOWN



    public void onCreate(){
        inCycle = false;
        Log.i("CollectorServiceLog", "ServiceCreated");
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        packageManager = this.getPackageManager();
        createLocationListener();
        checkLocationPermissions();
        logDatabaseHelper = new LogDatabaseHelper(this);

        ActivityMonitorReceiver amr = new ActivityMonitorReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("kairos.CollectorService.ACTIVITY_MONITOR_RESULT");
        registerReceiver(amr,filter);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();

        new Thread(){
            public void run(){
                mainLoop();
            }
        }.start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    private void mainLoop(){
        Log.i("CollectorServiceLog", "MainLoopStarted");
        while(getAndLockCycle()){
            try {
                double latitude = 0;
                double longitude = 0;
                String provider = "";
                int phoneActivity;
                int playingMusic;
                String time = getTimeNow();
                String appForeground = getForegroundTask();
                int callActivity = getPhoneCallState();
                String account = getUsername();
                int ringMode = getRingtoneMode();

                if(isPhoneActive()){
                    phoneActivity = 1;
                }else {
                    appForeground = NULL_APP;
                    phoneActivity = 0;
                }

                if(isMusicPlaying()){
                    playingMusic = 1;
                }else{
                    playingMusic = 0;
                }

                if(checkLocation){
                    latitude =lastLocation.getLatitude();
                    longitude = lastLocation.getLongitude();
                    provider = String.valueOf(lastLocation.getProvider());
                }else{
                    latitude = 0;
                    longitude = 0;
                    provider = "PROVIDER";
                }

                Log.i("CollectorServiceLog",
                        time + "::"
                                + appForeground + "::"
                                + userActivityNow + "::"
                                + phoneActivity + "::"
                                + callActivity + "::"
                                + playingMusic + "::"
                                + ringMode + "::"
                                + latitude + "::"
                                + longitude + "::"
                                + provider + "::"
                                + account);
                logDatabaseHelper.addLogEntry(time,appForeground,userActivityNow,phoneActivity,callActivity,playingMusic,ringMode,latitude,longitude,provider,account);

                Thread.sleep(10000);
            }catch(InterruptedException ex){
                Log.i("CollectorServiceLog", "Exception in Main Loop");
            }
        }
    }

    private String getTimeNow(){
        //YYYY-MM-DD HH:MM:SS.SSS
        Calendar localDate = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return formatter.format(localDate.getTime());
    }


    private String getForegroundTask() {
        String currentApp = NULL_APP;
        String result;
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 10000000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }
        try{
            result = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(currentApp, PackageManager.GET_META_DATA));
        }catch(Exception e){
            result = currentApp;
        }

        return result;
    }

    private boolean isPhoneActive(){
        boolean result;
        /*PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        try{
            result = pm.isInteractive();
        }catch(Exception ex){
            result = false;
        }
        return result;*/
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        switch(display.getState()){
            case Display.STATE_OFF : result = false; break;
            case Display.STATE_ON : result = true; break;
            case Display.STATE_VR : result = true; break;
            case Display.STATE_DOZE : result = true; break;
            case Display.STATE_DOZE_SUSPEND : result = true; break;
            case Display.STATE_UNKNOWN : result = false; break;
            default : result =false; break;
        }

        return result;
    }

    private boolean isMusicPlaying(){
        AudioManager am = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        return am.isMusicActive();
    }

    private int getPhoneCallState(){
        int result;
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        switch(tm.getCallState()){
            case TelephonyManager.CALL_STATE_IDLE : result = 0;
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK: result = 1;
                break;
            case TelephonyManager.CALL_STATE_RINGING: result = 2;
                break;
            default : result = 0;
        }

        return result;
    }

    private void checkLocationPermissions(){
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocation = false;
        }else{
            checkLocation = true;
        }

        lastLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria,true));

        if(lastLocation == null){
            checkLocation = false;
        }

        locationManager.requestLocationUpdates(
                locationManager.getBestProvider(criteria,true),
                1000,
                10,
                locationListener);

    }

    private String getUsername() {
        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<String>();
        for (Account account : accounts) {
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");
            if (parts.length > 0 && parts[0] != null)
                return parts[0];
            else
                return "null";
        } else
            return "null";
    }

    private int getRingtoneMode(){
        int result;
        AudioManager am = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        switch(am.getRingerMode()){
            case AudioManager.RINGER_MODE_NORMAL : result = 0; break;
            case AudioManager.RINGER_MODE_SILENT : result = 1; break;
            case AudioManager.RINGER_MODE_VIBRATE : result = 2; break;
            default : result = 0;
        }

        return result;
    }


    private synchronized boolean getAndLockCycle(){
        if(this.inCycle == false) {
            inCycle = true;
        }

        return inCycle;
    }

    private void createLocationListener(){
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastLocation = location;
                if(lastLocation == null){
                    checkLocation = false;
                }else{
                    checkLocation = true;
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                checkLocationPermissions();
            }

            @Override
            public void onProviderEnabled(String provider) {
                checkLocationPermissions();
            }

            @Override
            public void onProviderDisabled(String provider) {
                checkLocationPermissions();
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("CollectorServiceLog", "ServiceBound");
        return mCollectorServiceBinder;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ActivityRecognitionService.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );

        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( googleApiClient, 10000, pendingIntent );


        //ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(this);
        //activityRecognitionClient.requestActivityUpdates(3000, pendingIntent );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class CollectorServiceBinder extends Binder {
        public CollectorService getBinder(){
            Log.i("CollectorServiceLog", "GettingBinder");
            return CollectorService.this;
        }
    }

    public List<String> getLogDump(){
        return logDatabaseHelper.getAllLogs();
    }

    public void dumpDatabaseToCSV(File path, String fileName){
        logDatabaseHelper.exportDatabaseCSV(path,fileName);
    }

    /*public class ActivityMonitorReceiver extends ResultReceiver{

        public ActivityMonitorReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            //super.onReceiveResult(resultCode, resultData);
            userActivityNow = resultData.getInt("mostLikelyActivity");
            Log.i("CollectorServiceLog", String.valueOf(userActivityNow));
        }
    }*/

    class ActivityMonitorReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            userActivityNow = intent.getIntExtra("ActivityNow" , 4);
        }
    }



}
