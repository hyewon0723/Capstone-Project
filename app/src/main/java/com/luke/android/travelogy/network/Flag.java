
package com.luke.android.travelogy.network;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.luke.android.travelogy.R;
import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Flag implements Parcelable {

    public static final String LOG_TAG = Flag.class.getSimpleName();
    public static final float POSTER_ASPECT_RATIO = 0.60f;

    @SerializedName("id")
    private long mId;
    @SerializedName("original_title")
    private String mTitle;
    @SerializedName("poster_path")
    private String mFlag;
    @SerializedName("overview")
    private String mOverview;
    @SerializedName("vote_average")
    private String mUserRating;
    @SerializedName("release_date")
    private String mReleaseDate;
    @SerializedName("backdrop_path")
    private String mBackdrop;

    // Only for createFromParcel
    private Flag() {
    }

    public Flag(long id, String title, String flag, String overview, String userRating,
                String releaseDate, String backdrop) {
        mId = id;
        mTitle = title;
        mFlag = flag;
        mOverview = overview;
        mUserRating = userRating;
        mReleaseDate = releaseDate;
        mBackdrop = backdrop;
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    public long getId() {
        return mId;
    }

    @Nullable
    public String getPosterUrl(Context context) {
        if (mFlag != null && !mFlag.isEmpty()) {
            //return context.getResources().getString(R.string.url_for_downloading_poster) + mFlag;
            Log.v("Luke","Flag. getPosterUrl mFlag " +mFlag.toLowerCase());
            //String url = "http://flags.fmcdn.net/data/flags/normal/us.png";
            String url = "http://flags.fmcdn.net/data/flags/normal/"+mFlag.toLowerCase()+".png";
            Log.v("Luke","Flag. getPosterUrl url " +url);
            return url;
        }
        // IllegalArgumentException: Path must not be empty. at com.squareup.picasso.Picasso.load.
        // Placeholder/Error/Title will be shown instead of a crash.
        return null;
    }

    public String getPoster() {
        return mFlag;
    }

    public String getReleaseDate(Context context) {
        String inputPattern = "yyyy-MM-dd";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.US);
        if (mReleaseDate != null && !mReleaseDate.isEmpty()) {
            try {
                Date date = inputFormat.parse(mReleaseDate);
                return DateFormat.getDateInstance().format(date);
            } catch (ParseException e) {
                Log.e(LOG_TAG, "The Release data was not parsed successfully: " + mReleaseDate);
                // Return not formatted date
            }
        } else {
            mReleaseDate = context.getString(R.string.release_date_missing);
        }

        return mReleaseDate;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    @Nullable
    public String getOverview() {
        return mOverview;
    }

    @Nullable
    public String getUserRating() {
        return mUserRating;
    }

    @Nullable
    public String getBackdropUrl(Context context) {
        if (mBackdrop != null && !mBackdrop.isEmpty()) {
            return context.getResources().getString(R.string.url_for_downloading_backdrop) +
                    mBackdrop;
        }
        // Placeholder/Error/Title will be shown instead of a crash.
        return null;
    }

    public String getBackdrop() {
        return mBackdrop;
    }

    public static final Parcelable.Creator<Flag> CREATOR = new Creator<Flag>() {
        public Flag createFromParcel(Parcel source) {
            Flag movie = new Flag();
            movie.mId = source.readLong();
            movie.mTitle = source.readString();
            movie.mFlag = source.readString();
            movie.mOverview = source.readString();
            movie.mUserRating = source.readString();
            movie.mReleaseDate = source.readString();
            movie.mBackdrop = source.readString();
            return movie;
        }

        public Flag[] newArray(int size) {
            return new Flag[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mId);
        parcel.writeString(mTitle);
        parcel.writeString(mFlag);
        parcel.writeString(mOverview);
        parcel.writeString(mUserRating);
        parcel.writeString(mReleaseDate);
        parcel.writeString(mBackdrop);
    }
}
