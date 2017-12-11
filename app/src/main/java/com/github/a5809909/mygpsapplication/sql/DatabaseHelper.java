package com.github.a5809909.mygpsapplication.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.github.a5809909.mygpsapplication.model.PhoneState;


import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "PhoneStateManager.db";

    // User table name
    private static final String TABLE_PHONE_STATE = "phoneState";

    // User Table Columns names
    private static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_MCC = "mcc";
    public static final String COLUMN_MNC = "mnc";
    public static final String COLUMN_CELL_SIZE = "cellSize";
    public static final String COLUMN_WIFI_SIZE = "wifiSize";
    public static final String COLUMN_CELL_ID = "cellID";
    public static final String COLUMN_LAC = "lac";
    public static final String COLUMN_SIGNAL_STRENGTH = "signalStrength";
    public static final String COLUMN_CELL_INFO = "cellInfo";
    public static final String COLUMN_WIFI_INFO = "wifiStr";
    public static final String COLUMN_RADIO_TYPE = "radioType";




    private String CREATE_PHONE_STATE_TABLE = "CREATE TABLE " + TABLE_PHONE_STATE + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_TIME + " TEXT,"+
            COLUMN_MCC + " TEXT," +
            COLUMN_MNC + " TEXT," +
            COLUMN_CELL_SIZE + " TEXT," +
            COLUMN_WIFI_SIZE + " TEXT," +
            COLUMN_CELL_ID + " TEXT," +
            COLUMN_LAC + " TEXT," +
            COLUMN_SIGNAL_STRENGTH + " TEXT," +
            COLUMN_CELL_INFO + " TEXT," +
            COLUMN_WIFI_INFO + " TEXT," +
            COLUMN_RADIO_TYPE + " TEXT" + ")";

    private String DROP_PHONE_STATE_TABLE = "DROP TABLE IF EXISTS " + TABLE_PHONE_STATE;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PHONE_STATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //Drop User Table if exist
        db.execSQL(DROP_PHONE_STATE_TABLE);

        // Create tables again
        onCreate(db);
    }




    public void addUser(PhoneState phoneState) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIME, phoneState.getTime());
        values.put(COLUMN_MCC, phoneState.getMcc());
        values.put(COLUMN_MNC, phoneState.getMnc());
        values.put(COLUMN_CELL_SIZE, phoneState.getNumberOfCells());
        values.put(COLUMN_WIFI_SIZE, phoneState.getNumberOfWifi());
        values.put(COLUMN_CELL_ID, phoneState.getCellId());
        values.put(COLUMN_LAC, phoneState.getLac());
        values.put(COLUMN_SIGNAL_STRENGTH, phoneState.getSignalStrength_0());
        values.put(COLUMN_CELL_INFO, phoneState.getCellInfo());
        values.put(COLUMN_WIFI_INFO, phoneState.getWifiInfo());
        values.put(COLUMN_RADIO_TYPE, phoneState.getRadioType());

        // Inserting Row
        db.insert(TABLE_PHONE_STATE, null, values);
        db.close();
    }

    /**
     * This method is to fetch all phoneState and return the list of phoneState records
     *
     * @return list
     */

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PHONE_STATE, null, null, null, null, null, null);
        Log.i("cc", "getAllItems: "+cursor.getPosition());
        return cursor;
    }

    public List<PhoneState> getAllPhoneStates() {
        // array of columns to fetch
        String[] columns = {
                COLUMN_ID,
                COLUMN_TIME,
                COLUMN_MCC,
                COLUMN_MNC,
                COLUMN_CELL_SIZE,
                COLUMN_WIFI_SIZE,
                COLUMN_CELL_ID,
                COLUMN_LAC,
                COLUMN_SIGNAL_STRENGTH,
                COLUMN_CELL_INFO,
                COLUMN_WIFI_INFO,
                COLUMN_RADIO_TYPE
        };
        // sorting orders
        String sortOrder =
                COLUMN_TIME + " ASC";
        List<PhoneState> phoneStateList = new ArrayList<PhoneState>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PHONE_STATE, //Table to query
                columns,    //columns to return
                null,        //columns for the WHERE clause
                null,        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                sortOrder); //The sort order

        // Traversing through all rows and adding to list
        PhoneState phoneState = new PhoneState();
        if (cursor.moveToFirst()) {
            do {
                phoneState.set_id(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_ID))));
                phoneState.setLac(cursor.getColumnIndex(COLUMN_TIME));
                phoneState.setMcc(cursor.getString(cursor.getColumnIndex(COLUMN_MCC)));
                phoneState.setMnc(cursor.getString(cursor.getColumnIndex(COLUMN_MNC)));
                phoneState.setNumberOfCells(cursor.getInt(cursor.getColumnIndex(COLUMN_CELL_SIZE)));
                phoneState.setNumberOfWifi(cursor.getInt(cursor.getColumnIndex(COLUMN_WIFI_SIZE)));
                // Adding phoneState record to list
                phoneStateList.add(phoneState);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        // return phoneState list
        return phoneStateList;
    }

//    public void updateUser(PhoneState phoneState) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_LAC, phoneState.getMnc());
//        values.put(COLUMN_MCC, phoneState.getMcc());
//        values.put(COLUMN_MNC, phoneState.getLac());
//
//        // updating row
//        db.update(TABLE_PHONE_STATE, values, COLUMN_ID + " = ?",
//                new String[]{String.valueOf(phoneState.get_id())});
//        db.close();
//    }

    /**
     * This method is to delete phoneState record
     *
     * @param phoneState
     */
    public void deletePhoneState(PhoneState phoneState) {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete phoneState record by id
        db.delete(TABLE_PHONE_STATE, COLUMN_ID + " = ?",
                new String[]{String.valueOf(phoneState.get_id())});
        db.close();
    }

}
