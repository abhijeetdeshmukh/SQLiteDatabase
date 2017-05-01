package com.example.abhijeet.sqlitedatabase.data;

/*** Created by ABHIJEET on 13-01-2017.*/

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.abhijeet.sqlitedatabase.data.ChannelContract.ChannelEntry;

/*** Database helper for <app_name> app. Manages database creation and version management.*/
public class ChannelDbHelper extends SQLiteOpenHelper {

    /** Name of the database file */
    private static final String DATABASE_NAME = "shelter.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ChannelDbHelper}.
     *
     * @param context of the app
     */
    public ChannelDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the channels table
        String SQL_CREATE_CHANNELS_TABLE =  "CREATE TABLE " + ChannelEntry.TABLE_NAME + " ("
                + ChannelEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ChannelEntry.COLUMN_CHANNEL_NAME + " TEXT NOT NULL, "
                + ChannelEntry.COLUMN_CHANNEL_ID + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_CHANNELS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
