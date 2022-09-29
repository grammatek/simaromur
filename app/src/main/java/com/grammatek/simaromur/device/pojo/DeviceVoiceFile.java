package com.grammatek.simaromur.device.pojo;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.grammatek.simaromur.network.remoteasset.VoiceFile;

public class DeviceVoiceFile {
    @SerializedName("description")
    public String Description;
    // for residence == assets, this is the filename inside the /voice path
    // for residence == disk, this is the full path beginning with
    //      <internal_name>/<version>/ and then <extracted_filename_including_archtitecture>
    // for residence == network, this is <extracted_filename_including_archtitecture>
    @SerializedName("path")
    public String Path;
    @SerializedName("type")
    public String Type;
    @SerializedName("phoneme_type")
    public String PhonemeType;
    @SerializedName("md5sum")
    public String Md5Sum;
    @SerializedName("platform")
    public String Platform;

    public DeviceVoiceFile(VoiceFile voiceFile) {
        this.Description = voiceFile.description;
        this.Path = voiceFile.path;
        this.Type = voiceFile.type;
        this.PhonemeType = voiceFile.phonemeType;
        this.Md5Sum = voiceFile.md5sum;
        this.Platform = voiceFile.platform;
        // the following are not transferrable to a DeviceVoiceFile object
        // voiceFile.compressedFile: this is the filename of the downloaded file before decompression
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
