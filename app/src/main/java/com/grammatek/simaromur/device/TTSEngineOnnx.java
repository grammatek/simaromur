package com.grammatek.simaromur.device;

import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.grammatek.simaromur.audio.AudioManager;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.device.pojo.DeviceVoiceFile;
import com.grammatek.simaromur.device.pojo.VitsConfig;
import com.grammatek.simaromur.utils.FileUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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
    private final static float SENTENCE_PAUSE = 0.5f;
    private static DeviceVoice sVoice = null;
    // matches a position preceded by any of the characters '.!?;' not followed by zero or
    // more whitespace characters ([\\s]*) and then a double quote (\").
    final static  String SplitPunctuationSymbols = "(?<=[.!?;])(?![\\s]*\")";

    final byte[] mPauseSilence;
    private OrtEnvironment mOrtEnv;
    private OrtSession mOrtSession;
    private VitsConfig mModelConfig;
    private final VitsPhoneConverter mPhoneConverter;


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
        if (sVoice != voice) {
            readVitsModelConfig(asm, configPath);
            createVitsModel(asm, modelPath, configPath);
        }
        mPhoneConverter = new VitsPhoneConverter(mModelConfig.phonemeIdMap);

        // check if sample rate of model is in a valid range between 11kHz and 48kHz
        if (mModelConfig.audio.sampleRate < 11025 || mModelConfig.audio.sampleRate > 48000) {
            throw new RuntimeException("Voice " + voice.Name + ": invalid sample rate " +
                    mModelConfig.audio.sampleRate + " Hz");
        }
        mPauseSilence = AudioManager.generatePcmSilence(SENTENCE_PAUSE, GetNativeSampleRate());

        Log.v(LOG_TAG, "Onnx model loaded from assets/" + modelPath);
        sVoice = voice;
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
        SessionOptions mOrtOpts = new SessionOptions();

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
        Log.i(LOG_TAG, "VITS voice generation");
        Instant startTime = Instant.now();

        List<byte[]> pcmList = new ArrayList<>();
        List<String> sentences = new ArrayList<>();

        // split ipa's to sentences by splitting at punctuation; we also need to
        // add the punctuation symbol at the end of a split sentence
        Collections.addAll(sentences, ipas.split(SplitPunctuationSymbols));
        long generatedPcmLength = 0;
        for (String sentence : sentences) {
            Log.v(LOG_TAG, "VITS sentence: " + sentence.strip());
            byte[] pcmSentence = speakSentenceToPCM(sentence.strip());
            pcmList.add(pcmSentence);
            generatedPcmLength += pcmSentence.length;
            // add silence after each sentence, as the voice doesn't have any pauses
            pcmList.add(mPauseSilence);
        }
        // remove the last silence again
        pcmList.remove(pcmList.size()-1);

        // collect the size of all pcm buffers, create a new buffer with the size and copy all
        // pcm buffers into the new buffer
        int totalSize = 0;
        for (byte[] pcmBuffer : pcmList) {
            totalSize += pcmBuffer.length;
        }
        ByteBuffer buffer = ByteBuffer.wrap(new byte[totalSize]);
        for (byte[] pcmBuffer : pcmList) {
            buffer.put(pcmBuffer);
        }
        byte[] pcm = buffer.array();
        Instant stopTime = Instant.now();

        final float timeElapsed = Duration.between(startTime, stopTime).toMillis() / 1000.0F;
        final long sampleSize = 2;
        final long nSamples = generatedPcmLength / GetNativeSampleRate() / sampleSize;
        Log.i(LOG_TAG, "VITS voice generation ran for " + timeElapsed + " secs, " +
                nSamples / timeElapsed + " x real-time");
        return pcm;
    }

    @NonNull
    private byte[] speakSentenceToPCM(String ipas) {
        long[] ipa2VecInput = mPhoneConverter.convertToPhonemeIds(ipas);

        long[][] phoneIdsArray = new long[1][ipa2VecInput.length];
        System.arraycopy(ipa2VecInput, 0, phoneIdsArray[0], 0, ipa2VecInput.length);
        long[] phoneIdsLengths = new long[] {phoneIdsArray[0].length};
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

            // output 0: array of longs, TODO: maybe use OnnxTensor.getByteBuffer() instead ?
            Object outputTensor = output.get(0).getValue();
            // TODO: what about speech mark timings ?
            if (outputTensor instanceof float[][][][]) {
                float[][][][] tensor4D = (float[][][][]) outputTensor;
                if (tensor4D.length == 1 && tensor4D[0].length == 1) {
                    float[] samples = tensor4D[0][0][0];

                    // TODO optimization: dithering needs a lot of time, we should see, if we can
                    //  conditionally switch it on/off
                    //byte[] bytes = AudioManager.pcmFloatTo16BitPCMWithDither(samples, 1.0f, true);
                    return AudioManager.pcmFloatTo16BitPCM(samples);
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
        return mModelConfig.audio.sampleRate;
    }

    public static class VitsPhoneConverter {
        private final Map<String, Integer> phonemeIdMap;
        private static final  String PunctuationSymbols = "[.,?!;:\"-]";
        private final static String PAD_SYMBOL = "_";
        private final long SPACE_PHONEME_ID;

        public VitsPhoneConverter(Map<String, Integer> phonemeIdMap) {
            this.phonemeIdMap = phonemeIdMap;
            SPACE_PHONEME_ID = phonemeIdMap.get(" ");
        }

        public long[] convertToPhonemeIds(String ipaString) {
            // first get tokens
            List<String> tokenList = new ArrayList<>();
            Collections.addAll(tokenList, ipaString.split("\\s+"));

            // iterate over all tokens in the list. Each token is split into phonemes by splitting
            // at PAD_SYMBOL. The phoneme ids are then looked up in the phonemeIdMap and added
            // to the phonemeIdList. Some symbols are atomic and do not have a PAD_SYMBOL, these
            // are looked up directly in the phonemeIdMap.

            List<Long> phonemeIdList = new ArrayList<>();
            addPhonemeIdToList("BOS", phonemeIdList);

            // check if the first token is a space symbol, if so, remove it
            if (tokenList.get(0).equals(" ")) {
                tokenList.remove(0);
            }
            for (String token : tokenList) {
                boolean space_removed = removeSpaceBeforePunctuation(phonemeIdList, token);

                // split token into phonemes via splitting at PAD_SYMBOL
                List<String> phonemeList = new ArrayList<>();
                Collections.addAll(phonemeList, token.split(PAD_SYMBOL));

                // add phoneme ids
                for (String phoneme : phonemeList) {
                    addPhonemeIdToList(phoneme, phonemeIdList);
                }
                if (!space_removed && !token.matches(PunctuationSymbols)) {
                    // before and after punctuation symbols, don't add a space symbol
                    addPhonemeIdToList(" ", phonemeIdList);
                }
            }
            if (phonemeIdList.size() == 0) {
                Log.w(LOG_TAG + "::VitsPhoneConverter", "Empty phonemeIdList, returning empty array");
                return new long[0];
            }
            // remove the last two symbols, which are the ids for " " and 0
            if (SPACE_PHONEME_ID == phonemeIdList.get(phonemeIdList.size() - 2)) {
                phonemeIdList.remove(phonemeIdList.size() - 1);
                phonemeIdList.remove(phonemeIdList.size() - 1);
            }

            addPhonemeIdToList("EOS", phonemeIdList);
            return toLongArray(phonemeIdList);
        }

        @NonNull
        private static long[] toLongArray(List<Long> phonemeIdList) {
            long[] phonemeIds = new long[phonemeIdList.size()];
            for (int i = 0; i < phonemeIdList.size(); i++) {
                phonemeIds[i] = phonemeIdList.get(i);
            }
            return phonemeIds;
        }

        // This method is a workaround for the phonemization of the VITS voice, where
        //  punctuation symbols are not padded after a word with space, but added directly
        //  after the non-punctuation character. Spaces are only used to separate words.
        //  Remove previous SPACE symbol, if phonemeIdlist has size > 2 and token is
        //  a punctuation symbol.
        //
        // @param phonemeIdList    The list of phoneme ids. This list is changed in place.
        // @param token            The token to be checked
        // @return                 True, if a SPACE symbol was removed, false otherwise
        private boolean removeSpaceBeforePunctuation(List<Long> phonemeIdList, String token) {
            boolean rv = false;
            try {
                if (token.matches(PunctuationSymbols)) {
                    if ((phonemeIdList.size() >= 2)
                            && (SPACE_PHONEME_ID == phonemeIdList.get(phonemeIdList.size() - 2))) {
                        // remove the previous 2 phoneme ids, which are the corresponding ids for space
                        phonemeIdList.remove(phonemeIdList.size() - 2);
                        phonemeIdList.remove(phonemeIdList.size() - 1);
                        rv = true;
                    }
                }
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, "Accessing phonemeIdMap('<SPACE>') failed: ", e);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception: ", e);
            }
            return rv;
        }

        /**
         * Add the phoneme id for the given symbol to the phonemeIdList. If the symbol is not
         * found in the phonemeIdMap, a warning is logged and the symbol is ignored.
         * <p>
         * All symbols are padded with 0, which is because of how the model was trained.
         *
         * @param symbol        The symbol to be looked up in the phonemeIdMap
         * @param phonemeIdList The list to which the phoneme id is added
         */
        private void addPhonemeIdToList(String symbol, List<Long> phonemeIdList) {
            Integer symbolValue = phonemeIdMap.get(symbol);
            if (symbolValue != null) {
                phonemeIdList.add((long) symbolValue);
                phonemeIdList.add(0L);
            } else {
                Log.w(LOG_TAG + "::VitsPhoneConverter", "Ignore unknown symbol (" + symbol + ") !");
            }
        }
    }

}
