package com.grammatek.simaromur.device.pojo;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes the GSON representation of the following JSON voice description format:
 * {
 *   "description": "Voice list of all voices packaged inside assets. This list is hand-crafted.",
 *   "voices": [{
 *     "name": "Alfur N",
 *     "description": "Alfur neural network voice: FastSpeech 2 model, Melgan vocoder",
 *     "language": "is-IS",
 *     "language_name": "Íslenska",
 *     "gender": "male",
 *     "type": "torchscript",
 *     "version": "0.0.1",
 *     "files": [{
 *       "description": "Dummy test input for vocoder, NOT a real FastSpeech 2 model",
 *       "path": "mel_postnet.input-0_0_1.pt",
 *       "type": "dummy-voicegen",
 *       "phoneme_type": "x-sampa",
 *       "md5sum": "26a561f7392d6977051893558e149f90"
 *       },
 *       {
 *         "description": "Melgan Vocoder",
 *         "path": "melgan_lite_alfur-0_0_1.pt",
 *         "type": "melgan",
 *         "md5sum": "0892656387e57ceb17169026fa572b77"
 *       }
 *     ]
 *   }
 *   ]
 * }
 */
public class DeviceVoices {
    @SerializedName("description")
    public String Description;
    @SerializedName("voices")
    public List<DeviceVoice> Voices;

    public DeviceVoices(String description, ArrayList<DeviceVoice> voices) {
        this.Description = description;
        this.Voices = voices;
    }

    @NonNull
    @Override
    public String toString() {
        return "DeviceVoices{" +
                "description='" + Description + '\'' +
                ", voices=[" + Voices.toString() + ']' +
                '}';
    }
}
