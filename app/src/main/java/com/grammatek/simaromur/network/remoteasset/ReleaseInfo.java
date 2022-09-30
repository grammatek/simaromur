package com.grammatek.simaromur.network.remoteasset;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReleaseInfo {
    @SerializedName("Description")
    public String description;
    @SerializedName("Voices")
    public List<VoiceInfo> voices;
}
