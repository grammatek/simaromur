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

    /** Is needed for GSON */
    public DeviceVoices() {
        Voices = new ArrayList<>();
    }

    public DeviceVoices(String description, ArrayList<DeviceVoice> voices) {
        this.Description = description;
        this.Voices = voices;
    }

    /**
     * This method combines the given DeviceVoices into one DeviceVoices object. If a voice is
     * present in both DeviceVoices objects, the corresponding voicesOnDisk object is used.
     *
     * @param voicesOnDisk      DeviceVoices object containing the voices on disk
     * @param voicesOnServer    DeviceVoices object containing the voices on the server
     *
     * @return  DeviceVoices object containing the voices on disk and on the server
     */
    public static DeviceVoices combine(DeviceVoices voicesOnDisk, DeviceVoices voicesOnServer) {
        DeviceVoices combinedVoices = new DeviceVoices(voicesOnDisk.Description, new ArrayList<>());
        // first add all voicesOnDisk
        combinedVoices.Voices.addAll(voicesOnDisk.Voices);

        // then add all voicesOnServer that are not already present
        for (DeviceVoice voiceOnServer : voicesOnServer.Voices) {
            boolean voiceAlreadyPresent = false;
            for (DeviceVoice voiceOnDisk : voicesOnDisk.Voices) {
                // this only tests currently the internal name, but should be extended to
                // test also the version
                if (voiceOnServer.InternalName.equals(voiceOnDisk.InternalName)) {
                    voiceAlreadyPresent = true;
                    break;
                }
            }
            if (!voiceAlreadyPresent) {
                combinedVoices.Voices.add(voiceOnServer);
            }
        }
        return combinedVoices;
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
