/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kairos.launcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.provider.Settings.System;
import android.support.v4.os.BuildCompat;
import android.util.Log;

import com.kairos.launcher.collector.CollectorService;
import com.kairos.launcher.graphics.IconShapeOverride;

/**--------------------------------------------------------------
 * TEAM KAIROS :: DEV CARLOS :: CLASS SYNOPSIS
 * Copyright (c) 2017 KAIROS
 *
 * Settings activity for Launcher. Currently implements the following setting: Allow rotation
 ----------------------------------------------------------------*/

/**
 * Settings activity for Launcher. Currently implements the following setting: Allow rotation
 */
public class SettingsActivity extends Activity {

    private static final String ICON_BADGING_PREFERENCE_KEY = "pref_icon_badging";
    // TODO: use Settings.Secure.NOTIFICATION_BADGING
    private static final String NOTIFICATION_BADGING = "notification_badging";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new LauncherSettingsFragment())
                .commit();
    }

    /**
     * This fragment shows the launcher preferences.
     */
    public static class LauncherSettingsFragment extends PreferenceFragment {

        private SystemDisplayRotationLockObserver mRotationLockObserver;
        private IconBadgingObserver mIconBadgingObserver;
        private Context parentContext;

        //KAIROS
        private CollectorService mCollectorService;
        private boolean bound;
        private ServiceConnection mServiceConnection;



        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
            addPreferencesFromResource(R.xml.launcher_preferences);

            //KAIROS
            parentContext = getActivity();

            ContentResolver resolver = getActivity().getContentResolver();

            //KAIROS
            Preference button = findPreference("dumpButton");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    bindToCollectorService();
                    dumpToCSV();
                    return true;
                }
            });

            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.i("CollectorServiceLog", "vtrsying collector");
                    CollectorService.CollectorServiceBinder collectorBinder = (CollectorService.CollectorServiceBinder)service;
                    mCollectorService = collectorBinder.getBinder();
                    bound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    bound = false;
                }
            };

            // Setup allow rotation preference
            Preference rotationPref = findPreference(Utilities.ALLOW_ROTATION_PREFERENCE_KEY);
            if (getResources().getBoolean(R.bool.allow_rotation)) {
                // Launcher supports rotation by default. No need to show this setting.
                getPreferenceScreen().removePreference(rotationPref);
            } else {
                mRotationLockObserver = new SystemDisplayRotationLockObserver(rotationPref, resolver);

                // Register a content observer to listen for system setting changes while
                // this UI is active.
                resolver.registerContentObserver(
                        Settings.System.getUriFor(System.ACCELEROMETER_ROTATION),
                        false, mRotationLockObserver);

                // Initialize the UI once
                mRotationLockObserver.onChange(true);
                rotationPref.setDefaultValue(Utilities.getAllowRotationDefaultValue(getActivity()));
            }

            Preference iconBadgingPref = findPreference(ICON_BADGING_PREFERENCE_KEY);
            if (!BuildCompat.isAtLeastO()) {
                getPreferenceScreen().removePreference(
                        findPreference(SessionCommitReceiver.ADD_ICON_PREFERENCE_KEY));
                getPreferenceScreen().removePreference(iconBadgingPref);
            } else {
                // Listen to system notification badge settings while this UI is active.
                mIconBadgingObserver = new IconBadgingObserver(iconBadgingPref, resolver);
                resolver.registerContentObserver(
                        Settings.Secure.getUriFor(NOTIFICATION_BADGING),
                        false, mIconBadgingObserver);
                mIconBadgingObserver.onChange(true);
            }

            Preference iconShapeOverride = findPreference(IconShapeOverride.KEY_PREFERENCE);
            if (iconShapeOverride != null) {
                if (IconShapeOverride.isSupported(getActivity())) {
                    IconShapeOverride.handlePreferenceUi((ListPreference) iconShapeOverride);
                } else {
                    getPreferenceScreen().removePreference(iconShapeOverride);
                }
            }
        }

        @Override
        public void onDestroy() {
            if (mRotationLockObserver != null) {
                getActivity().getContentResolver().unregisterContentObserver(mRotationLockObserver);
                mRotationLockObserver = null;
            }
            if (mIconBadgingObserver != null) {
                getActivity().getContentResolver().unregisterContentObserver(mIconBadgingObserver);
                mIconBadgingObserver = null;
            }

            if(bound){
                parentContext.unbindService(mServiceConnection);
                bound = false;
            }

            super.onDestroy();
        }

        //KAIROS
        private void bindToCollectorService(){

            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.i("CollectorServiceLog", "vtrsying collector");
                    CollectorService.CollectorServiceBinder collectorBinder = (CollectorService.CollectorServiceBinder)service;
                    mCollectorService = collectorBinder.getBinder();
                    bound = true;
                }



                @Override
                public void onServiceDisconnected(ComponentName name) {
                    bound = false;
                }
            };

            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {

                    if(mServiceConnection != null){

                        Intent intent = new Intent(parentContext, CollectorService.class);
                        parentContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

                    }else {

                        handler.postDelayed(this, 1000);

                    }
                }
            });

        }

        //KAIROS
        private void dumpToCSV(){

            final Handler handler = new Handler();

            handler.post(new Runnable() {
                @Override
                public void run() {

                    if(mCollectorService != null){

                        mCollectorService.dumpDatabaseToCSV(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "KairosDump.csv" );

                    }else {

                        handler.postDelayed(this, 1000);

                    }
                }
            });
        }
    }

    /**
     * Content observer which listens for system auto-rotate setting changes, and enables/disables
     * the launcher rotation setting accordingly.
     */
    private static class SystemDisplayRotationLockObserver extends ContentObserver {

        private final Preference mRotationPref;
        private final ContentResolver mResolver;

        public SystemDisplayRotationLockObserver(
                Preference rotationPref, ContentResolver resolver) {
            super(new Handler());
            mRotationPref = rotationPref;
            mResolver = resolver;
        }

        @Override
        public void onChange(boolean selfChange) {
            boolean enabled = Settings.System.getInt(mResolver,
                    Settings.System.ACCELEROMETER_ROTATION, 1) == 1;
            mRotationPref.setEnabled(enabled);
            mRotationPref.setSummary(enabled
                    ? R.string.allow_rotation_desc : R.string.allow_rotation_blocked_desc);
        }
    }

    /**
     * Content observer which listens for system badging setting changes,
     * and updates the launcher badging setting subtext accordingly.
     */
    private static class IconBadgingObserver extends ContentObserver {

        private final Preference mBadgingPref;
        private final ContentResolver mResolver;

        public IconBadgingObserver(Preference badgingPref, ContentResolver resolver) {
            super(new Handler());
            mBadgingPref = badgingPref;
            mResolver = resolver;
        }

        @Override
        public void onChange(boolean selfChange) {
            boolean enabled = Settings.Secure.getInt(mResolver, NOTIFICATION_BADGING, 1) == 1;
            mBadgingPref.setSummary(enabled
                    ? R.string.icon_badging_desc_on
                    : R.string.icon_badging_desc_off);
        }
    }

}
