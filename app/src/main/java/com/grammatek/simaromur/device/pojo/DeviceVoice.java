package com.grammatek.simaromur.device.pojo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.network.remoteasset.VoiceFile;
import com.grammatek.simaromur.network.remoteasset.VoiceInfo;

import java.util.ArrayList;
import java.util.List;

public class DeviceVoice {
    static private String LOG_TAG = "Simaromur_" + DeviceVoice.class.getSimpleName();

    @SerializedName("name")
    public String Name;
    @SerializedName("internal_name")
    public String InternalName;
    @SerializedName("description")
    public String Description;
    @SerializedName("language")
    public String Language;
    @SerializedName("language_name")
    public String LanguageName;
    @SerializedName("gender")
    public String Gender;
    @SerializedName("type")
    public String Type;
    @SerializedName("version")
    public String Version;
    // Realtime factor, the higher, the better; measured on a Pixel-6 phone
    @SerializedName("rtf")
    public Float RTF;
    // assets, disk, network
    @SerializedName("residence")
    public String Residence;
    // release name, if available
    @SerializedName("release")
    public String Release;

    @SerializedName("files")
    public List<DeviceVoiceFile> Files;

    public DeviceVoice(String name, String internalName, String description, String language,
                       String languageName, String gender, String type, String version, String rtf, ArrayList<DeviceVoiceFile> files) {
        this.Name = name;
        this.InternalName = internalName;
        this.Description = description;
        this.Language = language;
        this.LanguageName = languageName;
        this.Gender = gender;
        this.Type = type;
        this.Version = version;
        try
        {
            this.RTF = Float.parseFloat(rtf);
        }
        catch (NumberFormatException e)
        {
            this.RTF = 1.0f;
        }
        this.Residence = "assets";
        this.Files = files;
    }

    public DeviceVoice(VoiceInfo voiceInfo, String repository, String releaseName, String platform) {
        this.Name = voiceInfo.name;
        this.InternalName = voiceInfo.internalName;
        this.Description = voiceInfo.description;
        this.Language = voiceInfo.language;
        // Currently only Icelandic voices are provided
        this.LanguageName = "íslenska";
        this.Gender = voiceInfo.gender;
        this.Type = voiceInfo.type;
        this.Version = voiceInfo.version;
        this.RTF = voiceInfo.rtf;
        this.Release = releaseName;
        this.Files = new ArrayList<>();
        this.Residence = "network" + ":" + repository + ":" + releaseName;
        for (VoiceFile voiceFile : voiceInfo.files) {
            if (voiceFile.platform.equals(platform)) {
                this.Files.add(new DeviceVoiceFile(voiceFile));
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "DeviceVoice{" +
                "name='" + Name + '\'' +
                ", internal_name='" + InternalName + '\'' +
                ", description='" + Description + '\'' +
                ", language='" + Language + '\'' +
                ", language_name='" + LanguageName + '\'' +
                ", gender='" + Gender + '\'' +
                ", type='" + Type + '\'' +
                ", version='" + Version + '\'' +
                ", residence='" + Residence + '\'' +
                ", files=[" + Files.toString() + ']' +
                '}';
    }

    /**
     * Converts a DeviceVoice into a Db voice. The returned voice object can be used for referencing
     * it inside the voice DB.
     *
     * @return  Db voice object
     */
    public Voice convertToDbVoice() {
        if (this.Files.size() == 0) {
            // no files, no voice
            return null;
        }
        String downloadUrl = "";
        if (this.Residence.equals("disk")) {
            for (DeviceVoiceFile file : this.Files) {
                if (!file.Path.isEmpty()) {
                    downloadUrl = file.Path;
                    break;
                }
            }
        }
        Log.v(LOG_TAG, "convertToDbVoice: " + this.toString());
        return new Voice(this.Name,
                this.InternalName,
                this.Gender,
                this.Language,
                this.LanguageName,
                this.Name,          // instead of variant, we use the name
                this.Type,
                this.Residence,     // url
                downloadUrl,                 // download path
                this.Version,
                "",         // md5sum
                0);                  // size
        // download path, md5sum and size are set later, when the file is downloaded
    }
}
