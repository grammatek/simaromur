package com.grammatek.simaromur.network.tiro.pojo;

import com.google.gson.annotations.SerializedName;

/*
    GSON adaptions for Request of:

    API_URL="https://tts.tiro.is/v0/speech"
    BODY=$(cat <<'END_HEREDOC'
    {
     "Engine": "standard",
     "LanguageCode": "is-IS",
     "OutputFormat": "mp3",
     "SampleRate": "22050",
     "Text": "<speak><phoneme alphabet='x-sampa' ph='s E m'>sem</phoneme></speak>",
     "TextType": "ssml",
     "VoiceId": "Karl"
    }
    END_HEREDOC
    )
    curl -d "$BODY" -H "Content-Type: application/json" -X POST "$API_URL" --output test.mp3
 */
public class SpeakRequest {
    @SerializedName("Engine")
    public String Engine;
    @SerializedName("LanguageCode")
    public String LanguageCode;
    @SerializedName("OutputFormat")
    public String OutputFormat;
    @SerializedName("SampleRate")
    public Integer SampleRate;
    @SerializedName("Text")
    public String Text;
    @SerializedName("TextType")
    public String TextType;
    @SerializedName("VoiceId")
    public String VoiceId;

    /**
     * Creates an object for a SpeakRequest
     *
     * @param engine        One of ["standard", ...]
     * @param languageCode  ISO language code, e.g. is-IS
     * @param outputFormat  audio output format, one of ["mp3", "ogg", "pcm"]
     * @param sampleRate    Audio sample rate of returned Audio file e.g. 22050
     * @param text          The text to be converted to audio
     * @param textType      Type of text, one of ["ssml", "text", ...]
     * @param voiceId       The voice to use for TTS, e.g. "Karl", "Dora", "Bjartur", "Joanna", ...
     */
    public SpeakRequest(String engine, String languageCode, String outputFormat, Integer sampleRate,
                        String text, String textType, String voiceId) {
        this.Engine = engine;
        this.LanguageCode = languageCode;
        this.OutputFormat = outputFormat;
        this.SampleRate = sampleRate;
        this.Text = text;
        this.TextType = textType;
        this.VoiceId = voiceId;
    }
}

