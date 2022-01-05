package com.grammatek.simaromur.device;

import android.content.res.AssetManager;
import android.util.Log;

import com.grammatek.simaromur.App;
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
        // only need one concurrent thread
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
                throw new IllegalArgumentException("TYPE_TIRO not supported for on-device TTS engines");
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
    public void StartSpeak(String text, float speed, float pitch, int sampleRate) {
        if (mEngine == null || mCurrentVoice == null) {
            String errorMsg = "No TTS engine loaded !";
            Log.e(LOG_TAG, errorMsg);
            throw new RuntimeException(errorMsg);
        }

        SpeakTask speakTask = new SpeakTask(text, speed, pitch, sampleRate);
        Log.v(LOG_TAG, "StartSpeak: scheduling new SpeakTask");
        mExecutorService.execute(speakTask);
    }

    /**
     * Stop speaking. Ignored in case currently no speak execution is done.
     */
    public void StopSpeak() {
        // TODO: how to stop current ongoing speak task ? Set a few variables ? What is the "official" way ?
    }

    public class SpeakTask implements Runnable {
        private final String LOG_SPEAK_TASK_TAG = "Simaromur_" + SpeakTask.class.getSimpleName();
        String text;
        float speed;
        float pitch;
        int sampleRate;

        public SpeakTask(String text, float speed, float pitch, int sampleRate) {
            this.text = text;
            this.speed = speed;
            this.pitch = pitch;
            this.sampleRate = sampleRate;
        }

        public String getText() {
            return text;
        }

        public void run() {
            Log.v(LOG_SPEAK_TASK_TAG, "run() called");
            // Frontend processing
            String sampas = mFrontend.process(text);
            byte[] bytes = mEngine.SpeakToWav(sampas, speed, pitch, sampleRate);

            // Media player: use wav
        }
    }
}
