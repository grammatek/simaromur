package com.grammatek.simaromur.device.pojo;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.grammatek.simaromur.network.remoteasset.VoiceFile;

import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeviceVoiceFile other = (DeviceVoiceFile) obj;
        return Type.equals(other.Type) && Path.equals(other.Path) && Md5Sum.equals(other.Md5Sum) && Platform.equals(other.Platform) && PhonemeType.equals(other.PhonemeType);
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


    public int hashCode() {
        // you pick a hard-coded, randomly chosen, non-zero, odd number
        // ideally different for each class
        return new HashCodeBuilder(17, 31).
                append(Type).
                append(Path).
                append(Md5Sum).
                append(Platform).
                append(PhonemeType).
                toHashCode();
    }
}
