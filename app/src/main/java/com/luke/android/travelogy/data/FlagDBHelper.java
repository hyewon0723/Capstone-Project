

package com.luke.android.travelogy.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Manages a local database for movies data.
 */
public class FlagDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "movies.db";

    public FlagDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + FlagContract.FlagEntry.TABLE_NAME
                + " (" +
                FlagContract.FlagEntry._ID + " INTEGER PRIMARY KEY," +
                FlagContract.FlagEntry.COLUMN_FLAG_ID + " INTEGER NOT NULL, " +
                FlagContract.FlagEntry.COLUMN_FLAG_TITLE + " TEXT NOT NULL, " +
                FlagContract.FlagEntry.COLUMN_FLAG_POSTER_PATH + " TEXT NOT NULL, " +
                FlagContract.FlagEntry.COLUMN_FLAG_BACKDROP_PATH + " TEXT NOT NULL " +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FlagContract.FlagEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
