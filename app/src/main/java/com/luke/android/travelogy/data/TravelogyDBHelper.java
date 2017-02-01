

package com.luke.android.travelogy.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Manages a local database for movies data.
 */
public class TravelogyDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "flags.db";

    public TravelogyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_PHOTO_TABLE = "CREATE TABLE " + TravelogyContract.PhotoEntry.TABLE_NAME
                + " (" +
                TravelogyContract.PhotoEntry._ID + " INTEGER PRIMARY KEY," +
                TravelogyContract.PhotoEntry.COLUMN_PHOTO_ID + " INTEGER NOT NULL, " +
                TravelogyContract.PhotoEntry.COLUMN_PHOTO_TITLE + " TEXT NOT NULL, " +
                TravelogyContract.PhotoEntry.COLUMN_PHOTO_PATH + " TEXT NOT NULL " +
                " );";

        final String SQL_CREATE_FLAG_TABLE = "CREATE TABLE " + TravelogyContract.FlagEntry.TABLE_NAME
                + " (" +
                TravelogyContract.FlagEntry._ID + " INTEGER PRIMARY KEY," +
                TravelogyContract.FlagEntry.COLUMN_FLAG_ID + " INTEGER NOT NULL, " +
                TravelogyContract.FlagEntry.COLUMN_FLAG_TITLE + " TEXT NOT NULL, " +
                TravelogyContract.FlagEntry.COLUMN_FLAG_POSTER_PATH + " TEXT NOT NULL, " +
                TravelogyContract.FlagEntry.COLUMN_FLAG_BACKDROP_PATH + " TEXT NOT NULL " +
                " );";


        sqLiteDatabase.execSQL(SQL_CREATE_PHOTO_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FLAG_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TravelogyContract.FlagEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
