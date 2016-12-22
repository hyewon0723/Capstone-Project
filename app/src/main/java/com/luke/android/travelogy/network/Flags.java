

package com.luke.android.travelogy.network;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Flags {

    @SerializedName("results")
    private List<Flag> flags = new ArrayList<>();

    public List<Flag> getFlags() {
        return flags;
    }
}

