package com.grammatek.simaromur.device;

import android.content.res.AssetManager;
import android.util.Log;

import com.grammatek.simaromur.audio.AudioManager;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.device.pojo.DeviceVoiceFile;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.nio.IntBuffer;
import java.time.Duration;
import java.time.Instant;

public class TTSEnginePyTorch implements TTSEngine {
    private final static String LOG_TAG = "Simaromur_" + TTSEnginePyTorch.class.getSimpleName();
    private final static int SAMPLE_RATE = 22050;
    private static DeviceVoice mVoice = null;
    private static Module mAcousticModel = null;
    private static Module mVocoderModel = null;
    private static Module mVocoderDummyInput = null;
    private Boolean mShouldStop = false;

    // Constructor for loading models from within Assets
    public TTSEnginePyTorch(AssetManager asm, DeviceVoice voice) {
        assert (voice.Type.equals("torchscript"));

        String fastSpeech2ModelPath = null;
        String melganModelPath = null;
        String melganDummyInputPath = null;

        for (DeviceVoiceFile voiceFile : voice.Files) {
            switch (voiceFile.Type) {
                case "fastspeech2":
                    fastSpeech2ModelPath = "voices/" + voiceFile.Path;
                    break;
                case "melgan":
                    melganModelPath = "voices/" + voiceFile.Path;
                    break;
                case "melgan_input":
                    melganDummyInputPath = "voices/" + voiceFile.Path;
                    break;
                default:
                    // Unknown
                    Log.e(LOG_TAG, "Unknown model type " + voiceFile.Type);
                    assert(false);
                    break;
            }
        }

        if (fastSpeech2ModelPath == null || melganModelPath == null) {
            throw new RuntimeException("Voice " + voice.Name + " doesn't specify all required models");
        }

        // In case of a new voice: unload all existing models
        if (mVoice != voice) {
            mAcousticModel = null;
            mVocoderModel = null;
            mVocoderDummyInput = null;
        }

        // load voice models
        if (mAcousticModel == null) {
            Log.v(LOG_TAG, "Loading FastSpeech2 model file " + fastSpeech2ModelPath + " ...");
            mAcousticModel = LiteModuleLoader.loadModuleFromAsset(asm, fastSpeech2ModelPath);
        } else {
            Log.v(LOG_TAG, "FastSpeech2 model file " + fastSpeech2ModelPath + " already loaded");
        }

        if (mVocoderModel == null) {
            Log.v(LOG_TAG, "Loading Melgan model file " + melganModelPath + " ...");
            mVocoderModel = LiteModuleLoader.loadModuleFromAsset(asm, melganModelPath);
        } else {
            Log.v(LOG_TAG, "Melgan model file " + melganModelPath + " already loaded");
        }

        if (mVocoderDummyInput == null && melganDummyInputPath != null) {
            Log.v(LOG_TAG, "Loading Melgan dummy input file " + melganDummyInputPath + " ...");
            // TODO DS: Doesn't work yet: pickles not supported on mobile ...
            // mVocoderDummyInput = LiteModuleLoader.loadModuleFromAsset(asm, melganDummyInputPath);
        }

        Log.v(LOG_TAG, "All models loaded");
        mVoice = voice;
    }

    @Override
    public byte[] SpeakToPCM(String sampas) {
        Log.v(LOG_TAG, "Speak: " + sampas);
        mShouldStop = false;
        Instant startTime = Instant.now();

        int[] sampa2VecInput = SymbolsLvLIs.MapSymbolsToVec(SymbolsLvLIs.Type.TYPE_SAMPA, sampas);
        // textTensor
        IntBuffer textTensorBuffer = Tensor.allocateIntBuffer(sampa2VecInput.length);
        textTensorBuffer.put(sampa2VecInput);
        long[] textTensorShape = {1, sampa2VecInput.length};
        Tensor textTensor = Tensor.fromBlob(textTensorBuffer, textTensorShape);
        byte[] bytes = new byte[0];
        do {
            if (mShouldStop) {
                break;
            }
            Log.v(LOG_TAG, "Inference start ...");
            final IValue melganInput = mAcousticModel.runMethod("mobile_inference",
                    IValue.from(textTensor));
            if (mShouldStop) {
                break;
            }
            Log.v(LOG_TAG, "Inference textTensor");
            // vocoder produces 22.05 kHz PCM float values between -20000.0 .. 20000.0
            final IValue voiceOutput = mVocoderModel.runMethod("inference", melganInput);

            if (mShouldStop) {
                break;
            }
            Log.v(LOG_TAG, "Inference voiceOuput");
            float[] samples = voiceOutput.toTensor().getDataAsFloatArray();
            bytes = AudioManager.pcmFloatTo16BitPCMWithDither(samples, 20000.0f, true);

            Instant stopTime = Instant.now();
            final long timeElapsed = Duration.between(startTime, stopTime).toMillis();
            Log.v(LOG_TAG, "Voice generation ran for " + timeElapsed / 1000.0F + " secs, " +
                    samples.length * 1000.0F / timeElapsed / GetNativeSampleRate() + " x real-time");

        } while(false);
        return bytes;
    }

    @Override
    public int GetNativeSampleRate() {
        return SAMPLE_RATE;
    }

    @Override
    public void Stop() {

    }
}
