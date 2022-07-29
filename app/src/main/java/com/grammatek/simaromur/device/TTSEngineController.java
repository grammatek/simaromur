package com.grammatek.simaromur.device;

import static com.grammatek.simaromur.cache.AudioFormat.AUDIO_FMT_PCM;
import static com.grammatek.simaromur.cache.SampleRate.SAMPLE_RATE_22KHZ;

import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.util.Log;

import androidx.annotation.Nullable;

import com.grammatek.simaromur.App;
import com.grammatek.simaromur.TTSObserver;
import com.grammatek.simaromur.audio.AudioManager;
import com.grammatek.simaromur.cache.CacheItem;
import com.grammatek.simaromur.cache.PhonemeEntry;
import com.grammatek.simaromur.cache.Utterance;
import com.grammatek.simaromur.cache.UtteranceCacheManager;
import com.grammatek.simaromur.cache.VoiceAudioDescription;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.frontend.FrontendManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class to  manage on-device TTS Engines and to execute TTS utterances on these.
 */
public class TTSEngineController {
    final static String LOG_TAG = "Simaromur_" + TTSEngineController.class.getSimpleName();
    final AssetManager mAssetManager;
    final AssetVoiceManager mAVM;
    DeviceVoice mCurrentVoice;
    TTSEngine mEngine;
    final ExecutorService mExecutorService;
    final TTSAudioControl mTTSAudioControl;

    /**
     * Constructor
     *
     * @param asm       AssetManager reference
     * @param frontend  Frontend manager reference
     * @throws IOException  In case any problems are detected within device voices
     */
    public TTSEngineController(AssetManager asm, FrontendManager frontend) throws IOException {
        mAssetManager = asm;
        mAVM = new AssetVoiceManager(App.getContext());
        mCurrentVoice = null;
        mTTSAudioControl = new TTSAudioControl(22050, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        // we only need one thread per Audio setting
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Prepare everything necessary to use the given voice, e.g. create new TTSEngine instance,
     * load the TTS model into memory, etc. In case the current voice uses the same engine as the
     * given voice, no action is executed.
     *
     * @param voice The voice to be used for the next StartSpeak() call.
     */
    public void LoadEngine(Voice voice) throws IOException {
        DeviceVoice devVoice = mAVM.getInfoForVoice(voice.name);
        switch (voice.type) {
            case Voice.TYPE_TIRO:
                Log.v(LOG_TAG, "LoadEngine: Voice.TYPE_TIRO not supported");
                break;
            case Voice.TYPE_TORCH:
                if (mEngine == null || devVoice != mCurrentVoice) {
                    Log.v(LOG_TAG, "LoadEngine: " + devVoice.Type);
                    mEngine = new TTSEnginePyTorch(App.getContext().getAssets(), devVoice);
                }
                else {
                    Log.v(LOG_TAG, "LoadEngine: (cached)");
                }
                break;
            case Voice.TYPE_FLITE:  // FALLTHROUGH
                Log.e(LOG_TAG, "LoadEngine: Flite TTS engine not yet implemented ");
                return;
            default:
                throw new IllegalArgumentException("Given voice not supported for on-device TTS engines");
        }
        mCurrentVoice = devVoice;
    }

    /**
     * Start to speak given text with given voice.
     */
    public SpeakTask StartSpeak(CacheItem item, float speed, float pitch, int sampleRate,
                           TTSAudioControl.AudioFinishedObserver observer) {
        if (mEngine == null || mCurrentVoice == null) {
            String errorMsg = "No TTS engine loaded !";
            Log.e(LOG_TAG, errorMsg);
            throw new RuntimeException(errorMsg);
        }

        SpeakTask speakTask = new SpeakTask(item.getUuid(), speed, pitch, sampleRate, observer, mCurrentVoice);
        Log.v(LOG_TAG, "StartSpeak: scheduling new SpeakTask");
        mExecutorService.execute(speakTask);
        return speakTask;
    }

    /**
     * Start to speak given text with given voice and use given callback for applying the synthesized
     * output.
     */
    public void StartSpeak(TTSObserver observer, String itemUuid) {
        if (mEngine == null || mCurrentVoice == null) {
            String errorMsg = "No TTS engine loaded !";
            Log.e(LOG_TAG, errorMsg);
            throw new RuntimeException(errorMsg);
        }

        SpeakTask speakTask = new SpeakTask(observer, itemUuid, mCurrentVoice);
        Log.v(LOG_TAG, "StartSpeak: scheduling new SpeakTask");
        mExecutorService.execute(speakTask);
    }

    /**
     * Stop speaking. Ignored in case currently no speak execution is done.
     */
    public void StopSpeak(TTSEngineController.SpeakTask speakTask) {
        mTTSAudioControl.stop();
        if (speakTask != null) {
            speakTask.stopSynthesis();
        }
    }

    public class SpeakTask implements Runnable {
        private final String LOG_SPEAK_TASK_TAG = "Simaromur_" + SpeakTask.class.getSimpleName();
        CacheItem item;
        float speed;
        float pitch;
        int sampleRate;
        TTSObserver observer;
        TTSAudioControl.AudioFinishedObserver audioObserver;
        boolean isStopped = false;
        DeviceVoice voice;

        /**
         * This initializes a SpeakTask for direct speak synthesis.
         *
         * @param itemUuid      uuid of cache item to be spoken
         * @param speed         speed multiplier, i.e. how many times faster/slower than normal voice
         *                      speed
         * @param pitch         pitch multiplier of voice, how many times higher/lower than normal voice
         *                      pitch
         * @param sampleRate    sample rate to use for the synthesis
         */
        public SpeakTask(String itemUuid, float speed, float pitch, int sampleRate,
                         TTSAudioControl.AudioFinishedObserver audioObserver, DeviceVoice voice) {
            Optional<CacheItem> optItem = App.getAppRepository().getUtteranceCache().findItemByUuid(itemUuid);
            this.item = optItem.orElse(null);
            this.speed = speed;
            this.pitch = pitch;
            this.sampleRate = sampleRate;
            this.audioObserver = audioObserver;
            this.observer = null;
            this.voice = voice;
        }

        /**
         * This initializes a SpeakTask for use via given observer. The observer needs to be already
         * initialized with pitch, speed & sample rate
         *
         * @param observer      Observer that gets the synthesized PCM data
         * @param itemUuid      uuid of cache item to be spoken
         */
        public SpeakTask(TTSObserver observer, String itemUuid, DeviceVoice voice) {
            Optional<CacheItem> optItem = App.getAppRepository().getUtteranceCache().findItemByUuid(itemUuid);
            this.item = optItem.orElse(null);
            this.audioObserver = null;
            this.observer = observer;
            // pitch & speed & sampleRate is applied by observer
            this.speed = 1.0f;
            this.pitch = 1.0f;
            this.sampleRate = mEngine.GetNativeSampleRate();
            this.voice = voice;
        }

        /**
         * This will run the synthesis and call either a given callback or use the AudioController
         * to directly play the synthesized voice.
         * @TODO: unify observers
         */
        public void run() {
            Log.v(LOG_SPEAK_TASK_TAG, "run() called");
            assert(sampleRate == mEngine.GetNativeSampleRate());

            if (shouldStop()) return;

            Utterance utterance = item.getUtterance();
            if (utterance.getPhonemesCount() == 0) {
                Log.e(LOG_SPEAK_TASK_TAG, "run(): No phonemes found in cache item ?!");
                return;
            }

            // retrieve audio from utterance cache, if available
            UtteranceCacheManager ucm =  App.getAppRepository().getUtteranceCache();
            // TODO: voiceVersion parameter is not taken into account yet !
            final List<byte[]> audioBuffers =
                    ucm.getAudioForUtterance(item.getUtterance(), mCurrentVoice.InternalName, "v1");
            if (shouldStop()) return;

            byte[] pcmBytes16Bit;
            if (!audioBuffers.isEmpty()) {
                pcmBytes16Bit = audioBuffers.get(0);
            } else {
                // no audio for utterance yet
                PhonemeEntry phonemeEntry = utterance.getPhonemesList().get(0);
                pcmBytes16Bit = synthesizeSpeech(phonemeEntry);
                if (pcmBytes16Bit == null) return;
            }

            if (pcmBytes16Bit.length == 0) {
                Log.w(LOG_SPEAK_TASK_TAG, "run(): No audio generated ?!");
                return;
            }

            if (shouldStop()) return;
            if (observer == null) {
                byte[] audio = AudioManager.applyPitchAndSpeed(pcmBytes16Bit, sampleRate, pitch, speed);
                // TODO: also the media players should stop, if item has changed:
                //       - pass the cache item along
                mTTSAudioControl.play(new TTSAudioControl.AudioEntry(audio, audioObserver));
            } else {
                observer.update(pcmBytes16Bit);
            }
        }

        /**
         * Generates speech audio by calling the engine's SpeakToPCM() method. It also adds the
         * generated audio to the utterance cache.
         *
         * @param phonemeEntry  Phoneme entry to use for synthesizing audio
         *
         * @return  16 bit PCM buffer of synthesized speech or null in case prerequisites haven't been
         *          met or there was an error when synthesizing speech
         */
        @Nullable
        private byte[] synthesizeSpeech(PhonemeEntry phonemeEntry) {
            byte[] bytes;
            bytes = mEngine.SpeakToPCM(phonemeEntry.getSymbols());
            // TODO: voiceVersion parameter is not taken into account yet !
            final VoiceAudioDescription vad = UtteranceCacheManager.newAudioDescription(AUDIO_FMT_PCM,
                    SAMPLE_RATE_22KHZ, bytes.length, mCurrentVoice.InternalName, "v1");
            if (bytes.length == 0) {
                Log.w(LOG_SPEAK_TASK_TAG, "synthesizeSpeech(): No audio generated ?!");
                return null;
            }
            UtteranceCacheManager ucm = App.getAppRepository().getUtteranceCache();
            if (ucm.addAudioToCacheItem(this.item.getUuid(), phonemeEntry, vad, bytes)) {
                Log.v(LOG_SPEAK_TASK_TAG, "Cached speech audio " + this.item.getUuid());
            } else {
                Log.e(LOG_SPEAK_TASK_TAG, "Couldn't add audio to cache item " + this.item.getUuid());
                return null;
            }
            return bytes;
        }

        /**
         * Test for criteria to stop current synthesis/playback. Either the playback has been actively
         * stopped via calling method stopSynthesis() or by setting the global current utterance to
         * a different value than the one to be used here.
         *
         * @return  true in case the task should stop, false otherwise
         */
        synchronized
        private boolean shouldStop() {
            CacheItem currentUtterance = App.getAppRepository().getCurrentUtterance();
            if (currentUtterance == null) {
                return true;
            }
            final String globalItemUuid = currentUtterance.getUuid();
            boolean shouldBeStopped = isStopped || (item == null) || (! item.getUuid().equals(globalItemUuid));
            if (shouldBeStopped && (item != null)) {
                Log.v(LOG_SPEAK_TASK_TAG, "stopping: " + item.getUuid());
            }
            return shouldBeStopped;
        }

        /**
         * Stops synthesis of a SpeakTask. Stop criterium is checked multiple times, not only before
         * it's been queued, but also after each atomic step.
         */
        synchronized
        public void stopSynthesis() {
            Log.d(LOG_SPEAK_TASK_TAG, "stopSynthesis()");
            isStopped = true;
        }
    }
}
