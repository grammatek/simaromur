package com.grammatek.simaromur.device.pojo;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class DeviceVoiceFile {
    @SerializedName("description")
    public String Description;
    @SerializedName("path")
    public String Path;
    @SerializedName("type")
    public String Type;
    @SerializedName("phoneme_type")
    public String PhonemeType;
    @SerializedName("md5sum")
    public String Md5Sum;
    public DeviceVoiceFile(String description, String path, String type, String phonemeType, String md5Sum) {
        this.Description = description;
        this.Path = path;
        this.Type = type;
        this.PhonemeType = phonemeType;
        this.Md5Sum = md5Sum;
    }

    @NonNull
    @Override
    public String toString() {
        return "DeviceVoiceFile{" +
                "Description='" + Description + '\'' +
                ", Path='" + Path + '\'' +
                ", Type='" + Type + '\'' +
                ", PhonemeType='" + PhonemeType + '\'' +
                ", Md5Sum='" + Md5Sum + '\'' +
                '}';
    }
}
