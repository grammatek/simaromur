package com.grammatek.simaromur.device;

import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.util.Log;

import com.grammatek.simaromur.App;
import com.grammatek.simaromur.TTSObserver;
import com.grammatek.simaromur.audio.AudioManager;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.frontend.FrontendManager;

import java.io.IOException;
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
    final FrontendManager mFrontend;
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
        mFrontend = frontend;
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
    public SpeakTask StartSpeak(String text, float speed, float pitch, int sampleRate,
                           TTSAudioControl.AudioFinishedObserver observer) {
        if (mEngine == null || mCurrentVoice == null) {
            String errorMsg = "No TTS engine loaded !";
            Log.e(LOG_TAG, errorMsg);
            throw new RuntimeException(errorMsg);
        }

        SpeakTask speakTask = new SpeakTask(text, speed, pitch, sampleRate, observer);
        Log.v(LOG_TAG, "StartSpeak: scheduling new SpeakTask");
        mExecutorService.execute(speakTask);
        return speakTask;
    }

    /**
     * Start to speak given text with given voice and use given callback for applying the synthesized
     * output.
     */
    public void StartSpeak(TTSObserver observer, String text) {
        if (mEngine == null || mCurrentVoice == null) {
            String errorMsg = "No TTS engine loaded !";
            Log.e(LOG_TAG, errorMsg);
            throw new RuntimeException(errorMsg);
        }

        SpeakTask speakTask = new SpeakTask(observer, text);
        Log.v(LOG_TAG, "StartSpeak: scheduling new SpeakTask");
        mExecutorService.execute(speakTask);
    }

    /**
     * Stop speaking. Ignored in case currently no speak execution is done.
     */
    public void StopSpeak(TTSEngineController.SpeakTask speakTask) {
        mTTSAudioControl.stop();
        if (speakTask != null) {
            speakTask.stop();
        }
    }

    public class SpeakTask implements Runnable {
        private final String LOG_SPEAK_TASK_TAG = "Simaromur_" + SpeakTask.class.getSimpleName();
        String text;
        float speed;
        float pitch;
        int sampleRate;
        TTSObserver observer;
        TTSAudioControl.AudioFinishedObserver audioObserver;
        boolean isStopped = false;

        /**
         * This initializes a SpeakTask for direct speak synthesis.
         *
         * @param text          raw text to be spoken
         * @param speed         speed multiplier, i.e. how many times faster/slower than normal voice
         *                      speed
         * @param pitch         pitch multiplier of voice, how many times higher/lower than normal voice
         *                      pitch
         * @param sampleRate    sample rate to use for the synthesis
         */
        public SpeakTask(String text, float speed, float pitch, int sampleRate,
                         TTSAudioControl.AudioFinishedObserver audioObserver) {
            this.text = text;
            this.speed = speed;
            this.pitch = pitch;
            this.sampleRate = sampleRate;
            this.audioObserver = audioObserver;
        }

        /**
         * This initializes a SpeakTask for use via given observer. The observer needs to be already
         * initialized with pitch, speed & sample rate
         *
         * @param observer      Observer that gets the synthesized PCM data
         * @param text          raw text to be spoken
         */
        public SpeakTask(TTSObserver observer, String text) {
            this.text = text;
            this.audioObserver = null;
            this.observer = observer;
            // pitch & speed & sampleRate is applied by observer
            this.speed = 1.0f;
            this.pitch = 1.0f;
            this.sampleRate = mEngine.GetSampleRate();
        }

        /**
         * Getter for the text given in constructor.
         *
         * @return  Text as given in constructor
         */
        public String getText() {
            return text;
        }

        /**
         * This will run the synthesis and call either a given callback or use the AudioController
         * to directly play the synthesized voice.
         * @TODO: unify observers
         */
        public void run() {
            Log.v(LOG_SPEAK_TASK_TAG, "run() called");
            assert(sampleRate == mEngine.GetSampleRate());

            // Frontend processing
            String sampas = mFrontend.process(text);
            if (isStopped) return;
            byte[] pcmBytes16Bit = mEngine.SpeakToPCM(sampas, sampleRate);
            if (isStopped) return;
            if (observer == null) {
                byte[] audio = AudioManager.applyPitchAndSpeed(pcmBytes16Bit, sampleRate, pitch, speed);
                if (isStopped) return;
                // Media player: use wav
                mTTSAudioControl.play(new TTSAudioControl.AudioEntry(audio, audioObserver));
            } else {
                observer.update(pcmBytes16Bit);
            }
        }

        /**
         * Stops a SpeakTask before it's been queued and the observers are called
         */
        public void stop() {
            isStopped = true;
        }
    }
}
