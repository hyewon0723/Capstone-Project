

package com.luke.android.travelogy.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class TravelogyProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    static final int FLAGS = 300;
    static final int PHOTOS = 301;
    private TravelogyDBHelper mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TravelogyContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, TravelogyContract.PATH_FLAG, FLAGS);
        matcher.addURI(authority, TravelogyContract.PATH_PHOTO, PHOTOS);
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
        if (getContext() != null) {
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
}
