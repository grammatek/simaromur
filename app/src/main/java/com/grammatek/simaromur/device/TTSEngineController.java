package com.grammatek.simaromur.device;

import static com.grammatek.simaromur.cache.AudioFormat.AUDIO_FMT_PCM;
import static com.grammatek.simaromur.cache.SampleRate.SAMPLE_RATE_16KHZ;
import static com.grammatek.simaromur.cache.SampleRate.SAMPLE_RATE_22KHZ;

import android.media.AudioFormat;
import android.util.Log;

import androidx.annotation.Nullable;

import com.grammatek.simaromur.App;
import com.grammatek.simaromur.TTSObserver;
import com.grammatek.simaromur.TTSRequest;
import com.grammatek.simaromur.audio.AudioManager;
import com.grammatek.simaromur.cache.CacheItem;
import com.grammatek.simaromur.cache.PhonemeEntry;
import com.grammatek.simaromur.cache.SampleRate;
import com.grammatek.simaromur.cache.Utterance;
import com.grammatek.simaromur.cache.UtteranceCacheManager;
import com.grammatek.simaromur.cache.VoiceAudioDescription;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.device.pojo.DeviceVoice;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class to  manage on-device TTS Engines and to execute TTS utterances on these. The audio that
 * is produced by the TTS engine is for one cached in the utterance cache manager and is either
 * played directly via the TTSAudioControl or is sent to the TTSService for playback.
 */
public class TTSEngineController {
    final static String LOG_TAG = "Simaromur_" + TTSEngineController.class.getSimpleName();
    final AssetVoiceManager mAVM;
    final DownloadVoiceManager mDVM;
    DeviceVoice mCurrentVoice;
    TTSEngine mEngine;
    final ExecutorService mExecutorService;
    Future<?> mTaskFuture;  // the currently enqueued task, might be executed by the executor service
    final TTSAudioControl mTTSAudioControl16khz;
    final TTSAudioControl mTTSAudioControl22khz;

    /**
     * Constructor
     *
     * @param avm       AssetVoiceManager reference
     *                  (to get the list of available asset voices)
     * @param dvm       DownloadVoiceManager reference
     *                  (to get the list of available downloadable voices)
     */
    public TTSEngineController(AssetVoiceManager avm, DownloadVoiceManager dvm) {
        mAVM = avm;
        mDVM = dvm;
        mCurrentVoice = null;
        mTTSAudioControl16khz = new TTSAudioControl(AudioManager.SAMPLE_RATE_FLITE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mTTSAudioControl22khz = new TTSAudioControl(AudioManager.SAMPLE_RATE_TORCH,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
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
    synchronized
    public void LoadEngine(Voice voice) throws IOException {
        DeviceVoice devVoice;
        switch (voice.type) {
            case Voice.TYPE_NETWORK:
                Log.v(LOG_TAG, "LoadEngine: Voice.TYPE_NETWORK not supported");
                break;
            case Voice.TYPE_TORCH:
                devVoice = mAVM.getInfoForVoice(voice.name);
                if (mEngine == null || devVoice != mCurrentVoice) {
                    Log.v(LOG_TAG, "LoadEngine: " + devVoice.Type);
                    mEngine = new TTSEnginePyTorch(App.getContext().getAssets(), devVoice);
                    mCurrentVoice = devVoice;
                }
                else {
                    Log.v(LOG_TAG, "LoadEngine: (cached)");
                }
                break;
            case Voice.TYPE_FLITE:
                devVoice = mDVM.getInfoForVoice(voice.internalName);
                if (mEngine == null || devVoice != mCurrentVoice) {
                    Log.v(LOG_TAG, "LoadEngine: " + devVoice.Type);
                    try {
                        mEngine = new TTSEngineFlite(voice, devVoice);
                        mCurrentVoice = devVoice;
                    } catch (IllegalArgumentException e) {
                        Log.e(LOG_TAG, "LoadEngine: " + e.getMessage());
                        throw e;
                    }
                }
                else {
                    Log.v(LOG_TAG, "LoadEngine: (cached)");
                }
                break;
            default:
                throw new IllegalArgumentException("Given voice not supported for on-device TTS engines");
        }
    }

    public void UnloadEngine() {
        Log.v(LOG_TAG, "UnloadEngine()");
        if (mEngine != null) {
            mCurrentVoice = null;
            mEngine = null;
        }
    }

    /**
     * Start to speak given text with given voice.
     */
    public SpeakTask StartSpeak(CacheItem item, float speed, float pitch, int sampleRate,
                           TTSAudioControl.AudioFinishedObserver observer, TTSRequest ttsRequest) {
        if (mEngine == null || mCurrentVoice == null) {
            String errorMsg = "No TTS engine loaded !";
            Log.e(LOG_TAG, errorMsg);
            throw new RuntimeException(errorMsg);
        }

        if (mTaskFuture != null && !mTaskFuture.isDone()) {
            Log.v(LOG_TAG, "StartSpeak: Canceling previous task");
            mTaskFuture.cancel(true);
        }
        Log.v(LOG_TAG, "StartSpeak: scheduling new SpeakTask (1)");
        SpeakTask speakTask = new SpeakTask(item.getUuid(), speed, pitch, sampleRate, observer, mCurrentVoice, ttsRequest);
        mTaskFuture = mExecutorService.submit(speakTask);
        return speakTask;
    }

    /**
     * Start to speak given text with given voice and use given callback for applying the synthesized
     * output.
     */
    public void StartSpeak(TTSObserver observer, TTSRequest ttsRequest) {
        if (mEngine == null || mCurrentVoice == null) {
            String errorMsg = "No TTS engine loaded !";
            Log.e(LOG_TAG, errorMsg);
            throw new RuntimeException(errorMsg);
        }

        SpeakTask speakTask = new SpeakTask(observer, ttsRequest, mCurrentVoice);
        Log.v(LOG_TAG, "StartSpeak: scheduling new SpeakTask (2)");
        if ((mTaskFuture != null) && !mTaskFuture.isDone()) {
            Log.v(LOG_TAG, "StartSpeak: Canceling previous task");
            mTaskFuture.cancel(true);
        }
        mTaskFuture = mExecutorService.submit(speakTask);
    }

    /**
     * Stop speaking. Ignored in case currently no speak execution is done.
     */
    public void StopSpeak(TTSEngineController.SpeakTask speakTask) {
        mTTSAudioControl16khz.stop();
        mTTSAudioControl22khz.stop();
        if (speakTask != null) {
            speakTask.stopSynthesis();
        }
    }

    public TTSEngine getEngine() {
        return mEngine;
    }

    public class SpeakTask implements Runnable {
        private final String LOG_SPEAK_TASK_TAG = "Simaromur_" + SpeakTask.class.getSimpleName();
        private final TTSRequest ttsRequest;
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
         * @param ttsRequest    request to be used for the synthesis
         */
        public SpeakTask(String itemUuid, float speed, float pitch, int sampleRate,
                         TTSAudioControl.AudioFinishedObserver audioObserver, DeviceVoice voice,
                         TTSRequest ttsRequest) {
            this.ttsRequest = ttsRequest;
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
         * @param ttsRequest    Request to be synthesized
         * @param voice         Voice to be used for synthesis
         */
        public SpeakTask(TTSObserver observer, TTSRequest ttsRequest, DeviceVoice voice) {
            this.ttsRequest = ttsRequest;
            Optional<CacheItem> optItem = App.getAppRepository().getUtteranceCache().findItemByUuid(ttsRequest.getCacheItemUuid());
            this.item = optItem.orElse(null);
            this.audioObserver = null;
            this.observer = observer;
            this.speed = observer.getSpeed();
            this.pitch = observer.getPitch();
            this.sampleRate = mEngine.GetNativeSampleRate();
            this.voice = voice;
        }

        /**
         * This will run the synthesis and call either a given callback or use the AudioController
         * to directly play the synthesized voice.
         * TODO: unify observers
         */
        public void run() {
            Log.v(LOG_SPEAK_TASK_TAG, "run() called");
            assert(sampleRate == mEngine.GetNativeSampleRate());

            if (shouldStop())  {
                Log.v(LOG_SPEAK_TASK_TAG, "run(): shouldStop(1): true");
                return;
            }

            Utterance utterance = item.getUtterance();
            if (utterance.getPhonemesCount() == 0) {
                Log.e(LOG_SPEAK_TASK_TAG, "run(): No phonemes found in cache item ?!");
                return;
            }

            // retrieve audio from utterance cache, if available
            UtteranceCacheManager ucm =  App.getAppRepository().getUtteranceCache();
            final List<byte[]> audioBuffers =
                    ucm.getAudioForUtterance(item.getUtterance(), mCurrentVoice.InternalName, voice.Version);
            if (shouldStop()) {
                Log.v(LOG_SPEAK_TASK_TAG, "run(): shouldStop(2): true");
                return;
            }

            byte[] audioData;
            if (!audioBuffers.isEmpty()) {
                audioData = audioBuffers.get(0);
            } else {
                // no audio for utterance yet
                PhonemeEntry phonemeEntry = utterance.getPhonemesList().get(0);
                audioData = synthesizeSpeech(phonemeEntry);
            }

            if ((audioData == null) || (audioData.length == 0)) {
                Log.w(LOG_SPEAK_TASK_TAG, "run(): No audio generated ?!");
                return;
            }
            if (shouldStop()) {
                Log.v(LOG_SPEAK_TASK_TAG, "run(): shouldStop(3): true");
                return;
            }

            if (observer == null) {
                // TODO: also the media players should stop, if item has changed:
                //       - pass the cache item along
                byte[] processedAudio = AudioManager.applyPitchAndSpeed(audioData, sampleRate, pitch, speed);
                if (sampleRate == AudioManager.SAMPLE_RATE_FLITE) {
                    mTTSAudioControl16khz.play(new TTSAudioControl.AudioEntry(processedAudio, audioObserver));
                } else {
                    mTTSAudioControl22khz.play(new TTSAudioControl.AudioEntry(processedAudio, audioObserver));
                }
            } else {
                observer.update(audioData, ttsRequest);
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
            Log.v(LOG_SPEAK_TASK_TAG, "synthesizeSpeech() :" + phonemeEntry.getSymbols());
            byte[] bytes;
            if (mEngine == null) {
                Log.e(LOG_SPEAK_TASK_TAG, "synthesizeSpeech(): mEngine is null");
                return null;
            }
            bytes = mEngine.SpeakToPCM(phonemeEntry.getSymbols());

            SampleRate sampleRate;
            if (mEngine.GetNativeSampleRate() == 22050) {
                sampleRate = SAMPLE_RATE_22KHZ;
            } else if (mEngine.GetNativeSampleRate() == 16000) {
                sampleRate = SAMPLE_RATE_16KHZ;
            } else {
                throw new IllegalStateException("Unknown sample rate: " + mEngine.GetNativeSampleRate());
            }
            final VoiceAudioDescription vad = UtteranceCacheManager.newAudioDescription(AUDIO_FMT_PCM,
                    sampleRate, bytes.length, mCurrentVoice.InternalName, mCurrentVoice.Version);
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
         * stopped via calling method stopSynthesis() or by setting the global tts request to
         * a different value than the one to be used here.
         *
         * @return  true in case the task should stop, false otherwise
         */
        synchronized
        private boolean shouldStop() {
            TTSRequest currentTTsRequest = App.getAppRepository().getCurrentTTsRequest();
            if (currentTTsRequest == null) {
                return true;
            }
            boolean shouldBeStopped = isStopped || (item == null) || (ttsRequest != currentTTsRequest);
            if (shouldBeStopped && (item != null)) {
                Log.v(LOG_SPEAK_TASK_TAG, "stopping: " + item.getUuid());
            }
            return shouldBeStopped;
        }

        /**
         * Stops synthesis of a SpeakTask. Stop criteria is checked multiple times, not only before
         * it's been queued, but also after each atomic step.
         */
        synchronized
        public void stopSynthesis() {
            Log.d(LOG_SPEAK_TASK_TAG, "stopSynthesis()");
            isStopped = true;
        }
    }
}
