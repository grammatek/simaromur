package com.grammatek.simaromur.device.pojo;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.grammatek.simaromur.db.Voice;

import java.util.ArrayList;
import java.util.List;

public class DeviceVoice {
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
    @SerializedName("files")
    public List<DeviceVoiceFile> Files;

    public DeviceVoice(String name, String internalName, String description, String language,
                       String languageName, String gender, String type, String version, ArrayList<DeviceVoiceFile> files) {
        this.Name = name;
        this.InternalName = internalName;
        this.Description = description;
        this.Language = language;
        this.LanguageName = languageName;
        this.Gender = gender;
        this.Type = type;
        this.Version = version;
        this.Files = files;
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
        return new Voice(this.Name,
                this.InternalName,
                this.Gender,
                this.Language,
                this.LanguageName,
                this.Name,
                this.Type,
                "assets");
    }
}
