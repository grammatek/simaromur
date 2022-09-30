package com.grammatek.simaromur.network.tiro.pojo;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/*
    GSON adaptions for Response of $ curl -X GET https://tts.tiro.is/v0/voices | jq
      [
        {
          "Name": "Other",
          "Gender": "Male",
          "LanguageCode": "is-IS",
          "LanguageName": "Íslenska",
          "SupportedEngines": [
            "standard"
          ],
          "VoiceId": "Other"
        },
        ...
      ]
 */
public class VoiceResponse {
    @SerializedName("Name")
    public String Name;
    @SerializedName("Gender")
    public String Gender;
    @SerializedName("LanguageCode")
    public String LanguageCode;
    @SerializedName("LanguageName")
    public String LanguageName;
    @SerializedName("SupportedEngines")
    public List<String> SupportedEngines = null;
    @SerializedName("VoiceId")
    public String VoiceId;

    @NonNull
    @Override
    public String toString() {
        return "VoiceResponse{" +
                "Name='" + Name + '\'' +
                ", Gender='" + Gender + '\'' +
                ", LanguageCode='" + LanguageCode + '\'' +
                ", LanguageName='" + LanguageName + '\'' +
                ", SupportedEngines=" + SupportedEngines +
                ", VoiceId='" + VoiceId + '\'' +
                '}';
    }
}

