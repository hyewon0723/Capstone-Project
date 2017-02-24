

package com.luke.android.travelogy.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class TravelogyProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    static final int FLAGS = 300;
    static final int PHOTOS = 301;
    static final int PHOTOS_BY_FLAGS = 302;
    private TravelogyDBHelper mOpenHelper;


    private static final SQLiteQueryBuilder sPhotoByFlagQueryBuilder;

    static{
        sPhotoByFlagQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON photo.flag_id = flag._id
        sPhotoByFlagQueryBuilder.setTables(
                TravelogyContract.PhotoEntry.TABLE_NAME + " INNER JOIN " +
                        TravelogyContract.FlagEntry.TABLE_NAME +
                        " ON " + TravelogyContract.PhotoEntry.TABLE_NAME +
                        "." + TravelogyContract.PhotoEntry.COLUMN_FLAG_KEY +
                        " = " + TravelogyContract.FlagEntry.TABLE_NAME +
                        "." + TravelogyContract.FlagEntry._ID);
    }


    //flag.flag_setting = ?
    private static final String sFlagSettingSelection =
            TravelogyContract.FlagEntry.TABLE_NAME+
                    "." + TravelogyContract.FlagEntry.COLUMN_FLAG_SETTING + " = ? ";


    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TravelogyContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, TravelogyContract.PATH_FLAG, FLAGS);
        matcher.addURI(authority, TravelogyContract.PATH_PHOTO, PHOTOS);
        matcher.addURI(authority, TravelogyContract.PATH_PHOTO+"/*", PHOTOS_BY_FLAGS);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new TravelogyDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case FLAGS: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        TravelogyContract.FlagEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case PHOTOS: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        TravelogyContract.PhotoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case PHOTOS_BY_FLAGS: {
                cursor = getPhotoByFlag(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FLAGS:
                return TravelogyContract.FlagEntry.CONTENT_TYPE;
            case PHOTOS:
                return TravelogyContract.PhotoEntry.CONTENT_TYPE;
            case PHOTOS_BY_FLAGS:
                return TravelogyContract.PhotoEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        Log.v("Luke", "TP Insert~~~~~~~ START ");
        switch (match) {
            case FLAGS: {
                long id = db.insert(TravelogyContract.FlagEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = TravelogyContract.FlagEntry.buildFlagUri(id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case PHOTOS: {
                long id = db.insert(TravelogyContract.PhotoEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = TravelogyContract.PhotoEntry.buildPhotoUri(id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Log.v("Luke", "TP Insert~~~~~~~ "+getContext() + " uri "+uri);
        if (getContext() != null) {
            Log.v("Luke", "TP Insert~~~~~~~ notifying!!!!!!");
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) {
            selection = "1";
        }
        switch (match) {
            case FLAGS:
                rowsDeleted = db.delete(
                        TravelogyContract.FlagEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case PHOTOS:
                rowsDeleted = db.delete(
                        TravelogyContract.PhotoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case FLAGS:
                rowsUpdated = db.update(TravelogyContract.FlagEntry.TABLE_NAME, values, selection,
                        selectionArgs);
            case PHOTOS:
                rowsUpdated = db.update(TravelogyContract.PhotoEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private Cursor getPhotoByFlag(Uri uri, String[] projection, String sortOrder) {
        String flagSetting = TravelogyContract.FlagEntry.getFlagFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = sFlagSettingSelection;
        selectionArgs = new String[]{flagSetting};

        return sPhotoByFlagQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );



    }

}
