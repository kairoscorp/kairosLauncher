package com.kairos.launcher;

import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;

import java.util.Locale;

/**--------------------------------------------------------------
 * TEAM KAIROS :: DEV CARLOS :: CLASS SYNOPSIS
 * Copyright (c) 2017 KAIROS
 *
 * Class used by IconCache to fetch app icons.
 * Also used to return the location (Country) to test if Icons are still valid in Cache.
 ----------------------------------------------------------------*/

public class IconProvider {

    private static final boolean DBG = false;
    private static final String TAG = "IconProvider";

    protected String mSystemState;

    public IconProvider() {
        updateSystemStateString();
    }

    public void updateSystemStateString() {
        mSystemState = Locale.getDefault().toString();
    }

    public String getIconSystemState(String packageName) {
        return mSystemState;
    }


    public Drawable getIcon(LauncherActivityInfo info, int iconDpi) {
        return info.getIcon(iconDpi);
    }
}
