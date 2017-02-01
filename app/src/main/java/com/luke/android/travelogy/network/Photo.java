
package com.luke.android.travelogy.network;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class Photo implements Parcelable {

    public static final String LOG_TAG = Photo.class.getSimpleName();
    public static final float POSTER_ASPECT_RATIO = 0.50f;

    @SerializedName("id")
    private long mId;
    @SerializedName("title")
    private String mTitle;
    @SerializedName("path")
    private String mPhoto;



    // Only for createFromParcel
    private Photo() {
    }

    public Photo(long id, String title, String photo) {
        mId = id;
        mTitle = title;
        mPhoto = photo;
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
        if (mPhoto != null && !mPhoto.isEmpty()) {
            return mPhoto;
        }
        return null;
    }

    public String getPoster() {
        return mPhoto;
    }


    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        public Photo createFromParcel(Parcel source) {
            Photo photo = new Photo();
            photo.mId = source.readLong();
            photo.mTitle = source.readString();
            photo.mPhoto = source.readString();
            return photo;
        }

        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int photos) {
        parcel.writeLong(mId);
        parcel.writeString(mTitle);
        parcel.writeString(mPhoto);
    }


}
