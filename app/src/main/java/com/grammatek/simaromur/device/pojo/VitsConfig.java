package com.grammatek.simaromur.device.pojo;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class VitsConfig {
    @SerializedName("audio")
    public Audio audio;

    @SerializedName("espeak")
    public Espeak espeak;

    @SerializedName("inference")
    public Inference inference;

    @SerializedName("phoneme_map")
    public Map<String, Object> phonemeMap;

    @SerializedName("phoneme_id_map")
    public Map<String, int[]> phonemeIdMap;

    @SerializedName("num_symbols")
    public int numSymbols;

    @SerializedName("num_speakers")
    public int numSpeakers;

    @SerializedName("speaker_id_map")
    public Map<String, Object> speakerIdMap;

    @NonNull
    @Override
    public String toString() {
        return "Configuration{" +
                "audio=" + audio +
                ", espeak=" + espeak +
                ", inference=" + inference +
                ", phonemeMap=" + phonemeMap +
                ", phonemeIdMap=" + phonemeIdMap +
                ", numSymbols=" + numSymbols +
                ", numSpeakers=" + numSpeakers +
                ", speakerIdMap=" + speakerIdMap +
                '}';
    }

    public static class Audio {
        @SerializedName("sample_rate")
        public int sampleRate;

        @NonNull
        @Override
        public String toString() {
            return "Audio{" +
                    "sampleRate=" + sampleRate +
                    '}';
        }
    }

    public static class Espeak {
        @SerializedName("voice")
        public String voice;

        @NonNull
        @Override
        public String toString() {
            return "Espeak{" +
                    "voice='" + voice + '\'' +
                    '}';
        }
    }

    public static class Inference {
        @SerializedName("noise_scale")
        public float noiseScale;

        @SerializedName("length_scale")
        public float lengthScale;

        @SerializedName("noise_w")
        public float noiseW;

        @NonNull
        @Override
        public String toString() {
            return "Inference{" +
                    "noiseScale=" + noiseScale +
                    ", lengthScale=" + lengthScale +
                    ", noiseW=" + noiseW +
                    '}';
        }
    }
}
