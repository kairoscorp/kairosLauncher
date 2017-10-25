package com.kairos.launcher.testing;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.kairos.launcher.Launcher;
import com.kairos.launcher.LauncherAppState;
import com.kairos.launcher.Utilities;
import com.kairos.launcher.util.TestingUtils;

public class ToggleWeightWatcher extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = Utilities.getPrefs(this);
        boolean show = sp.getBoolean(TestingUtils.SHOW_WEIGHT_WATCHER, true);

        show = !show;
        sp.edit().putBoolean(TestingUtils.SHOW_WEIGHT_WATCHER, show).apply();

        Launcher launcher = (Launcher) LauncherAppState.getInstance(this).getModel().getCallback();
        if (launcher != null && launcher.mWeightWatcher != null) {
            launcher.mWeightWatcher.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        finish();
    }
}
