package com.kairos.launcher.collector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import com.opencsv.CSVWriter;

public class LogDatabaseHelper extends SQLiteOpenHelper {

    public LogDatabaseHelper(Context context) {
        super(context, "ActivityLog.db", null, 1);
    }


    public void addLogEntry(String timestamp,
                            String foreground,
                            int activity,
                            int screen_active,
                            int call_active,
                            int music_active,
                            int ring_mode,
                            double latitude,
                            double longitude,
                            String provider,
                            String account){

        SQLiteDatabase activityLogDB = this.getWritableDatabase();

        String NEWTABLE = "CREATE TABLE IF NOT EXISTS 'ServiceLogs' "
                + "( _id  INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "timestamp NUMERIC, "
                + "foreground TEXT, "
                + "activity INTEGER,"
                + "screen_active INTEGER, "
                + "call_active INTEGER, "
                + "music_active INTEGER, "
                + "ring_mode INTEGER, "
                + "latitude REAL, "
                + "longitude REAL, "
                + "provider TEXT, "
                + "account TEXT"
                + ");";

        activityLogDB.execSQL(NEWTABLE);



        String ENTRY = "INSERT INTO ServiceLogs (" +
                "timestamp, " +
                "foreground, "+
                "activity, " +
                "screen_active, "+
                "call_active, "+
                "music_active, " +
                "ring_mode, "+
                "latitude, "+
                "longitude, "+
                "provider, "+
                "account) VALUES ('"+
                timestamp + "', '" +
                foreground + "', " +
                activity + "," +
                screen_active + ", "+
                call_active + ", "+
                music_active + ", "+
                ring_mode + ", "+
                latitude + ", "+
                longitude + ", '"+
                provider + "', '"+
                account  + "');";

        activityLogDB.execSQL(ENTRY);
    }


    public List<String> getAllLogs(){
        List<String> result = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM ServiceLogs";
        Cursor res = db.rawQuery(query, null );
        res.moveToFirst();


        while(res.isAfterLast() == false){
            String entry = "";
            entry = res.getString(res.getColumnIndex("_id"));
            entry = entry + "::" + res.getString(res.getColumnIndex("timestamp"));
            entry = entry + "::" + res.getString(res.getColumnIndex("screen_active"));
            entry = entry + "::" + res.getString(res.getColumnIndex("call_active"));
            entry = entry + "::" + res.getString(res.getColumnIndex("music_active"));
            entry = entry + "::" + res.getString(res.getColumnIndex("ring_mode"));
            entry = entry + "::" + res.getString(res.getColumnIndex("latitude"));
            entry = entry + "::" + res.getString(res.getColumnIndex("longitude"));
            entry = entry + "::" + res.getString(res.getColumnIndex("provider"));
            entry = entry + "::" + res.getString(res.getColumnIndex("account"));
            result.add(entry);
            res.moveToNext();
        }
        return result;
    }

    public void exportDatabaseCSV(File path, String fileName){
        if(!path.exists()){
            path.mkdirs();
        }

        File file = new File(path, fileName);
        try
        {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor curCSV = db.rawQuery("SELECT * FROM ServiceLogs",null);
            csvWrite.writeNext(curCSV.getColumnNames());

            while(curCSV.moveToNext())
            {
                //Which column you want to exprort
                String arrStr[] ={
                        curCSV.getString(curCSV.getColumnIndex("_id")),
                        curCSV.getString(curCSV.getColumnIndex("timestamp")),
                        curCSV.getString(curCSV.getColumnIndex("foreground")),
                        curCSV.getString(curCSV.getColumnIndex("activity")),
                        curCSV.getString(curCSV.getColumnIndex("screen_active")),
                        curCSV.getString(curCSV.getColumnIndex("call_active")),
                        curCSV.getString(curCSV.getColumnIndex("music_active")),
                        curCSV.getString(curCSV.getColumnIndex("ring_mode")),
                        curCSV.getString(curCSV.getColumnIndex("latitude")),
                        curCSV.getString(curCSV.getColumnIndex("longitude")),
                        curCSV.getString(curCSV.getColumnIndex("provider")),
                        curCSV.getString(curCSV.getColumnIndex("account"))
                };
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
        }
        catch(Exception sqlEx)
        {
            Log.e("CollectorServiceLog", sqlEx.getMessage(), sqlEx);
        }

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String NEWTABLE = "CREATE TABLE IF NOT EXISTS 'ServiceLogs' "
                + "( _id  INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "timestamp NUMERIC, "
                + "foreground TEXT, "
                + "activity INTEGER,"
                + "screen_active INTEGER, "
                + "call_active INTEGER, "
                + "music_active INTEGER, "
                + "ring_mode INTEGER, "
                + "latitude REAL, "
                + "longitude REAL, "
                + "provider TEXT, "
                + "account TEXT"
                + ");";

        db.execSQL(NEWTABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
