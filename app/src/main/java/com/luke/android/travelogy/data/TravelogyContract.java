

package com.luke.android.travelogy.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class TravelogyContract {

    public static final String CONTENT_AUTHORITY = "com.luke.android.travelogy";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FLAG = "flag";
    public static final String PATH_PHOTO = "photo";

    public static final class PhotoEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PHOTO).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHOTO;

        public static final String TABLE_NAME = "photo";
        public static final String COLUMN_PHOTO_ID = "photo_id";
        public static final String COLUMN_PHOTO_TITLE = "photo_title";
        public static final String COLUMN_PHOTO_PATH = "photo_path";
        // Column with the foreign key into the location table.
        public static final String COLUMN_FLAG_KEY = "flag_id";
        public static final String FULL_ID = TABLE_NAME + "." + _ID;

        public static Uri buildPhotoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPhotoUriByFlag(String flagkey) {
            return CONTENT_URI.buildUpon().appendPath(flagkey).build();
        }


        public static final String[] PHOTO_COLUMNS = {
                TABLE_NAME + "." + PhotoEntry._ID,
                COLUMN_PHOTO_ID,
                COLUMN_PHOTO_TITLE,
                COLUMN_PHOTO_PATH,
                FlagEntry.COLUMN_FLAG_SETTING
        };

        public static final int COL_PHOTO_ID = 0;
        public static final int COL_PHOTO_TITLE = 1;
        public static final int COL_PHOTO_PATH = 2;
        public static final int COL_PHOTO_FLAG_KEY = 3;
    }

    public static final class FlagEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FLAG).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FLAG;

        public static final String TABLE_NAME = "flag";
        public static final String COLUMN_FLAG_SETTING = "flag_setting";
        public static final String COLUMN_FLAG_ID = "flag_id";
        public static final String COLUMN_FLAG_TITLE = "original_title";
        public static final String COLUMN_FLAG_POSTER_PATH = "poster_path";
        public static final String COLUMN_FLAG_BACKDROP_PATH = "backdrop_path";

        public static Uri buildFlagUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getFlagFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static final String[] FLAG_COLUMNS = {
                COLUMN_FLAG_ID,
                COLUMN_FLAG_TITLE,
                COLUMN_FLAG_POSTER_PATH,
                COLUMN_FLAG_BACKDROP_PATH,
                COLUMN_FLAG_SETTING
        };

        public static final int COL_FLAG_ID = 0;
        public static final int COL_FLAG_TITLE = 1;
        public static final int COL_FLAG_POSTER_PATH = 2;
        public static final int COL_FLAG_BACKDROP_PATH = 3;
    }
}
