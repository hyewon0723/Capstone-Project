
package com.luke.android.travelogy.network;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class Flag implements Parcelable {

    public static final String LOG_TAG = Flag.class.getSimpleName();
    public static final float POSTER_ASPECT_RATIO = 0.60f;

    @SerializedName("id")
    private long mId;
    @SerializedName("original_title")
    private String mTitle;
    @SerializedName("poster_path")
    private String mFlag;
    @SerializedName("backdrop_path")
    private String mBackdrop;

    // Only for createFromParcel
    private Flag() {
    }

    public Flag(long id, String title, String flag, String backdrop) {
        mId = id;
        mTitle = title;
        mFlag = flag;
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
            String url = "http://flags.fmcdn.net/data/flags/normal/"+mFlag.toLowerCase()+".png";
            return url;
        }
        return null;
    }

    @Nullable
    public String getBackdropUrl(Context context) {

        if (mFlag != null && !mFlag.isEmpty()) {
            String url = "http://flags.fmcdn.net/data/flags/normal/"+mFlag.toLowerCase()+".png";
            return url;
        }
        return null;
    }

    public static final Parcelable.Creator<Flag> CREATOR = new Creator<Flag>() {
        public Flag createFromParcel(Parcel source) {
            Flag flag = new Flag();
            flag.mId = source.readLong();
            flag.mTitle = source.readString();
            flag.mFlag = source.readString();
            flag.mBackdrop = source.readString();
            return flag;
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
        parcel.writeString(mBackdrop);
    }
}
