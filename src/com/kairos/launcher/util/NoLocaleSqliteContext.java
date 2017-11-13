package com.kairos.launcher.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * A context wrapper which creates databases without support for localized collators.
 */

/**--------------------------------------------------------------
 * TEAM KAIROS :: DEV Kiko :: CLASS SYNOPSIS
 * Copyright (c) 2017 KAIROS
 *
 * This class opens or creates databases but forces the MODE_NO_LOCALIZED_COLLATOR flag mode,
 * i.e. string comparisons are made without considering the current locale
 ----------------------------------------------------------------*/

public class NoLocaleSqliteContext extends ContextWrapper {

    public NoLocaleSqliteContext(Context context) {
        super(context);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(
            String name, int mode, CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return super.openOrCreateDatabase(
                name, mode | Context.MODE_NO_LOCALIZED_COLLATORS, factory, errorHandler);
    }
}
