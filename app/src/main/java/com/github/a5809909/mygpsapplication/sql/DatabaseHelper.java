package com.github.a5809909.mygpsapplication.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.github.a5809909.mygpsapplication.model.PhoneState;
import com.github.a5809909.mygpsapplication.model.User;

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
    public static final String COLUMN_LAC = "lac";
    public static final String COLUMN_MCC = "mcc";
    public static final String COLUMN_MNC = "mnc";

    private String CREATE_PHONE_STATE_TABLE = "CREATE TABLE " + TABLE_PHONE_STATE + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_LAC + " TEXT,"
            + COLUMN_MCC + " TEXT," + COLUMN_MNC + " TEXT" + ")";

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
        values.put(COLUMN_LAC, phoneState.getLac_0());
        values.put(COLUMN_MCC, phoneState.getMcc());
        values.put(COLUMN_MNC, phoneState.getMnc());

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
                COLUMN_MCC,
                COLUMN_LAC,
                COLUMN_MNC
        };
        // sorting orders
        String sortOrder =
                COLUMN_LAC + " ASC";
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
        if (cursor.moveToFirst()) {
            do {
                PhoneState phoneState = new PhoneState();
                phoneState.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_ID))));
                phoneState.setLac_0(cursor.getColumnIndex(COLUMN_LAC));
                phoneState.setMcc(cursor.getString(cursor.getColumnIndex(COLUMN_MCC)));
                phoneState.setMnc(cursor.getString(cursor.getColumnIndex(COLUMN_MNC)));
                // Adding phoneState record to list
                phoneStateList.add(phoneState);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        // return phoneState list
        return phoneStateList;
    }

    public void updateUser(User phoneState) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_LAC, phoneState.getName());
        values.put(COLUMN_MCC, phoneState.getEmail());
        values.put(COLUMN_MNC, phoneState.getPassword());

        // updating row
        db.update(TABLE_PHONE_STATE, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(phoneState.getId())});
        db.close();
    }

    /**
     * This method is to delete phoneState record
     *
     * @param phoneState
     */
    public void deletePhoneState(PhoneState phoneState) {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete phoneState record by id
        db.delete(TABLE_PHONE_STATE, COLUMN_ID + " = ?",
                new String[]{String.valueOf(phoneState.getId())});
        db.close();
    }

}
