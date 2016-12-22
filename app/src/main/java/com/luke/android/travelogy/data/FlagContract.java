

package com.luke.android.travelogy.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class FlagContract {

    public static final String CONTENT_AUTHORITY = "com.luke.android.travelogy";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FLAG = "flag";

    public static final class FlagEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FLAG).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FLAG;

        public static final String TABLE_NAME = "flag";
        public static final String COLUMN_FLAG_ID = "flag_id";
        public static final String COLUMN_FLAG_TITLE = "original_title";
        public static final String COLUMN_FLAG_POSTER_PATH = "poster_path";
        public static final String COLUMN_FLAG_BACKDROP_PATH = "backdrop_path";

        public static Uri buildFlagUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String[] MOVIE_COLUMNS = {
                COLUMN_FLAG_ID,
                COLUMN_FLAG_TITLE,
                COLUMN_FLAG_POSTER_PATH,
                COLUMN_FLAG_BACKDROP_PATH
        };

        public static final int COL_FLAG_ID = 0;
        public static final int COL_FLAG_TITLE = 1;
        public static final int COL_FLAG_POSTER_PATH = 2;
        public static final int COL_FLAG_BACKDROP_PATH = 3;
    }
}
