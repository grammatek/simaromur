package com.grammatek.simaromur.device;

import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.grammatek.simaromur.App;
import com.grammatek.simaromur.audio.AudioManager;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.device.pojo.DeviceVoiceFile;
import com.grammatek.simaromur.device.pojo.VitsConfig;
import com.grammatek.simaromur.frontend.Pronunciation;
import com.grammatek.simaromur.frontend.PronunciationVits;
import com.grammatek.simaromur.utils.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtProvider;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.Result;
import ai.onnxruntime.OrtSession.SessionOptions;
import ai.onnxruntime.OrtSession.SessionOptions.OptLevel;

public class TTSEngineOnnx  implements TTSEngine {
    private final static String LOG_TAG = "Simaromur_" + TTSEngineOnnx.class.getSimpleName();
    private final static int SAMPLE_RATE = 22050;
    private static DeviceVoice mVoice = null;

    private OrtEnvironment mOrtEnv;
    private OrtSession.SessionOptions mOrtOpts;
    private OrtSession mOrtSession;

    private final Pronunciation mPronunciation;
    private final PronunciationVits mPronunciationVits;
    private VitsConfig mModelConfig;

    public TTSEngineOnnx(AssetManager asm, DeviceVoice voice) {
        assert (voice.Type.equals("onnx"));

        String modelPath = null;
        String configPath = null;
        for (DeviceVoiceFile voiceFile : voice.Files) {
            switch (voiceFile.Type) {
                case "vits":
                    modelPath = "voices/" + voiceFile.Path;
                    break;
                case "json":
                    configPath = "voices/" + voiceFile.Path;
                    break;
                default:
                    Log.e(LOG_TAG, "Unknown model type " + voiceFile.Type);
                    assert (false);
                    break;
            }
        }

        if (modelPath == null) {
            throw new RuntimeException("Voice " + voice.Name + " doesn't specify all required models");
        }
        if (configPath == null) {
            throw new RuntimeException("Voice " + voice.Name + ": no config file found");
        }

        // In case of a new voice: unload all existing models
        if (mVoice != voice) {
            readVitsModelConfig(asm, configPath);
            createVitsModel(asm, modelPath, configPath);
        }
        mPronunciation = new Pronunciation(App.getContext());
        mPronunciationVits = new PronunciationVits(mPronunciation);

        Log.v(LOG_TAG, "Onnx model loaded from assets/" + modelPath);
        mVoice = voice;
    }

    /**
     * Return a boolean, if NNAPI is supported on the device.
     *
     * @return  Boolean, true if NNAPI is supported, false otherwise.
     */
    private boolean isNnapiSupported() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1;
    }

    private void createVitsModel(AssetManager asm, String modelPath, String configPath) {
        mOrtEnv = OrtEnvironment.getEnvironment();
        mOrtOpts = new SessionOptions();

        try {
            if (false && isNnapiSupported()) {
                // TODO: Unfortunately, NNAPI is not working optimally yet in combination with our
                //       VITS model according to the ORT checker, let's wait for a newer version of
                //       onnxruntime
                Log.i(LOG_TAG, "NNAPI is supported, using NNAPI for inference");
                mOrtOpts.addConfigEntry("session.set_execution_providers", OrtProvider.NNAPI.getName());
                // NNAPI should not fall back to CPU
                mOrtOpts.addConfigEntry("NNAPIFlags", "1");
            } else {
                Log.i(LOG_TAG, "NNAPI is not supported, using CPU for inference");
                mOrtOpts.addConfigEntry("session.set_execution_providers", OrtProvider.CPU.getName());
            }
            mOrtOpts.setOptimizationLevel(OptLevel.ALL_OPT);
        } catch (OrtException e) {
            throw new RuntimeException(e);
        }
        try {
            byte[] modelBytes = FileUtils.readFileFromAssets(asm, modelPath);
            mOrtSession = mOrtEnv.createSession(modelBytes, mOrtOpts);
        } catch (IOException | OrtException e) {
            throw new RuntimeException(e);
        }
    }

    private void readVitsModelConfig(AssetManager asm, String configPath) {
        // read configuration from assets
        try {
            byte[] buf = FileUtils.readFileFromAssets(asm, configPath);
            try {
                String jsonString = new String(buf, StandardCharsets.UTF_8);
                Gson gson = new Gson();
                mModelConfig = gson.fromJson(jsonString, VitsConfig.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (JsonSyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] SpeakToPCM(String ipas) {
        Instant startTime = Instant.now();

        // squeeze out all spaces and replace §sp to space again
        Log.v(LOG_TAG, "Vits phonemes: " + ipas);
        // Python:
        // phoneme_ids_array = np.expand_dims(np.array(phoneme_ids, dtype=np.int64), 0)
        //        phoneme_ids_lengths = np.array([phoneme_ids_array.shape[1]], dtype=np.int64)
        //        scales = np.array(
        //            [noise_scale, length_scale, noise_w],
        //            dtype=np.float32,
        //        )
        // "noise_scale": 0.667,
        // "length_scale": 1,
        // "noise_w": 0.8

        // append each character in variable ipas with a PAD_SYMBOL, independent of space, dot, comma, etc.
        ipas = ipas.replaceAll("(.)", "$1" + '_');
        // prepend with BOS and append with EOS
        ipas = "^" + ipas + "$";

        VitsPhoneConverter phoneConverter = new VitsPhoneConverter(mModelConfig.phonemeIdMap);
        long[] ipa2VecInput = phoneConverter.convertToPhonemeIds(ipas);
        long[][] phoneIdsArray = new long[1][ipa2VecInput.length];
        System.arraycopy(ipa2VecInput, 0, phoneIdsArray[0], 0, ipa2VecInput.length);
        long[] phoneIdsLengths = new long[] {phoneIdsArray[0].length};
        // these are fixed now, but should be used from the voice config file
        float noiseScale = mModelConfig.inference.noiseScale;
        float lengthScale = mModelConfig.inference.lengthScale;
        float noiseW = mModelConfig.inference.noiseW;
        float[] scales = new float[] {noiseScale, lengthScale, noiseW};
        // textTensor
        try {
            Map<String, OnnxTensor> inputMap = new HashMap<String, OnnxTensor>();
            // this call uses inflection for the input tensor
            inputMap.put("input", OnnxTensor.createTensor(mOrtEnv, phoneIdsArray));
            inputMap.put("input_lengths", OnnxTensor.createTensor(mOrtEnv, phoneIdsLengths));
            inputMap.put("scales", OnnxTensor.createTensor(mOrtEnv, scales));
            // in case we have a speaker id, we need to add an int64 array with the speaker ids like that:
            // inputMap.put("sid", OnnxTensor.createTensor(mOrtEnv, speakerIds));
            Result output = mOrtSession.run(inputMap);

            // output 0: array of longs
            Object outputTensor = output.get(0).getValue();
            if (outputTensor instanceof float[][][][]) {
                float[][][][] tensor4D = (float[][][][]) outputTensor;
                if (tensor4D.length == 1 && tensor4D[0].length == 1) {
                    float[] samples = tensor4D[0][0][0];
                    byte[] bytes = AudioManager.pcmFloatTo16BitPCMWithDither(samples, 1.0f, true);
                    Instant stopTime = Instant.now();

                    final long timeElapsed = Duration.between(startTime, stopTime).toMillis();
                    Log.i(LOG_TAG, "Voice generation ran for " + timeElapsed / 1000.0F + " secs, " +
                            samples.length * 1000.0F / timeElapsed / GetNativeSampleRate() + " x real-time");
                    return bytes;
                }
            } else {
                Log.e(LOG_TAG, "Unexpected output tensor type: " + outputTensor.getClass().getName());
            }
        } catch (OrtException e) {
            throw new RuntimeException(e);
        }
        return new byte[0];
    }

    @Override
    public int GetNativeSampleRate() {
        return SAMPLE_RATE;
    }

    public static class VitsPhoneConverter {
        private Map<String, int[]> phonemeIdMap;

        public VitsPhoneConverter(Map<String, int[]> phonemeIdMap) {
            this.phonemeIdMap = phonemeIdMap;
        }

        public long[] convertToPhonemeIds(String ipaString) {
            List<Long> phonemeIdList = new ArrayList<>();

            for (int i = 0; i < ipaString.length(); i++) {
                String symbol = String.valueOf(ipaString.charAt(i));
                int[] idArray = phonemeIdMap.get(symbol);

                if (idArray != null && idArray.length > 0) {
                    phonemeIdList.add((long) idArray[0]);
                } else {
                    // Log a warning with the symbol and its position
                    Log.w(LOG_TAG+"::VitsPhoneConverter", "Unknown symbol '" + symbol + "' at position " + i);
                    // Ignore the unknown symbol
                }
            }

            // Convert the List<Long> to long[]
            long[] phonemeIds = new long[phonemeIdList.size()];
            for (int i = 0; i < phonemeIdList.size(); i++) {
                phonemeIds[i] = phonemeIdList.get(i);
            }

            return phonemeIds;
        }
    }

}
