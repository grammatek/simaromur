package com.grammatek.simaromur.network.remoteasset;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * This class represents a remote asset. It is used to download and cache remote assets, i.e.
 * voices and voice data.
 */
public class VoiceInfo {
    @SerializedName("Name")
    public String name;
    @SerializedName("InternalName")
    public String internalName;
    @SerializedName("Description")
    public String description;
    @SerializedName("Language")
    public String language;
    @SerializedName("Gender")
    public String gender;
    @SerializedName("Type")
    public String type;
    @SerializedName("Version")
    public String version;
    @SerializedName("RTF")
    public Float rtf;
    @SerializedName("Quality")
    public Integer quality;
    @SerializedName("Files")
    public List<VoiceFile> files;

    /**
     * Constructor
     *
     * @param name          The name of the asset.
     * @param internalName  The internal name of the asset.
     * @param description   The description of the asset.
     * @param language      The language of the asset.
     * @param gender        The gender of the voice
     * @param type          The asset type, e.g. flite, torchscript, ...
     * @param version       The version of the asset.
     * @param rtf           The real-time factor of the asset.
     * @param quality       The quality of the asset.
     * @param infoFiles     The list of files for this asset.
     */
    VoiceInfo(String name, String internalName, String description, String language, String gender,
              String type, String version, Float rtf, Integer quality, List<VoiceFile> infoFiles) {
        this.name = name;
        this.internalName = internalName;
        this.description = description;
        this.language = language;
        this.gender = gender;
        this.type = type;
        this.version = version;
        this.rtf = rtf;
        this.quality = quality;
        this.files = infoFiles;
    }

    public String getVoiceFileForPlatform(String platform) {
        for (VoiceFile file : files) {
            if (file.platform.equals(platform)) {
                return file.path;
            }
        }
        return null;
    }


    @NonNull
    @Override
    public String toString() {
        return "AssetInfo{" +
                "name='" + name + '\'' +
                ", internalName='" + internalName + '\'' +
                ", description='" + description + '\'' +
                ", language='" + language + '\'' +
                ", gender='" + gender + '\'' +
                ", type='" + type + '\'' +
                ", version='" + version + '\'' +
                ", rtf='" + rtf + '\'' +
                ", quality='" + quality + '\'' +
                ", files='" + files + '\'' +
                '}';
    }
}
