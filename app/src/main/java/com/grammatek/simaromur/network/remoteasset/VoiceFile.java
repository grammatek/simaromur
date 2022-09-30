package com.grammatek.simaromur.network.remoteasset;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents files of a remote asset.
 */
public class VoiceFile {
    @SerializedName("Description")
    public String description;
    @SerializedName("CompressedFile")
    public String compressedFile;
    @SerializedName("Path")
    public String path;
    // e.g. aarch64, ...
    @SerializedName("Platform")
    public String platform;
    // Type of the asset should match the type in VoiceDao in case of a voice asset, i.e.
    // "clustergen", "clunits", "torchscript", "phoneme"
    @SerializedName("Type")
    public String type;
    // The phoneme set to use for this asset, e.g. "ipa", "sampa", "flite"
    @SerializedName("PhonemeType")
    public String phonemeType;
    @SerializedName("Md5Sum")
    public String md5sum;

    /**
     * Constructor
     *
     * @param description       The description of the file.
     * @param compressedFile    The name of the compressed file.
     * @param path              The path of the file after decompression.
     * @param platform          The platform of the file.
     * @param type              The type of the file.
     * @param phoneme_type      The phoneme type of the file.
     * @param md5sum            The md5sum of the decompressed voice file given in path.
     */
    VoiceFile(String description, String compressedFile, String path, String platform, String type, String phoneme_type, String md5sum) {
        this.description = description;
        this.compressedFile = compressedFile;
        this.path = path;
        this.platform = platform;
        this.type = type;
        this.phonemeType = phoneme_type;
        this.md5sum = md5sum;
    }

    @NonNull
    @Override
    public String toString() {
        return "AssetFile{" +
                "description='" + description + '\'' +
                ", compressedFile=" + compressedFile +
                ", path=" + path +
                ", platform=" + platform +
                ", type=" + type +
                ", phoneme_type=" + phonemeType +
                ", md5sum='" + md5sum + '\'' +
                '}';
    }
}
