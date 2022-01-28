package com.grammatek.simaromur.device;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class TTSAudioControl {
    private final static String LOG_TAG = "Simaromur_" + com.grammatek.simaromur.device.TTSAudioControl.class.getSimpleName();

    final ExecutorService mExecutorService;
    private final LinkedBlockingQueue<AudioEntry> mQueue = new LinkedBlockingQueue<>();
    private AudioEntry mAudioEntry;
    private final AudioTrack mTrack;
    private boolean isStopped;

    /**
     * Interface for an observer that is called when finished with playing audio.
     */
    public interface AudioFinishedObserver {
        /**
         * This method is called whenever a queued element is finished playing.
         */
        void update();
    }

    /**
     * Creates an object suitable for 16Bit PCM mono audio playback. This object creates a thread
     * which waits on a queue to receive audio for playback via the @ref play() method. Playback
     * can be stopped via stop().
     *
     * @param sampleRate    sample Rate to be used for playback
     */
    TTSAudioControl(int sampleRate, int channels, int encoding) {
        mExecutorService = Executors.newSingleThreadExecutor();
        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channels, encoding);
        mTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                new AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(encoding)
                        .setChannelMask(channels)
                        .build(),
                minBufferSize,
                AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
        );

        mExecutorService.execute(() -> {
            // noinspection InfiniteLoopStatement
            while (true) {
                try {
                    mAudioEntry = mQueue.take();
                    Log.d(LOG_TAG, "now playing " + mAudioEntry.audio.length + " bytes");
                    int bufPos = 0;
                    while (bufPos < mAudioEntry.audio.length && !isStopped) {
                        int nBytes = Math.min(minBufferSize, mAudioEntry.audio.length - bufPos);
                        int written = mTrack.write(mAudioEntry.audio, bufPos, nBytes, AudioTrack.WRITE_BLOCKING);
                        if ((written > 0) && (!isStopped) && (mTrack.getState() != AudioTrack.PLAYSTATE_PLAYING)) {
                            mTrack.play();
                        }
                        bufPos += written;
                    }
                    // finished playing a queue element
                    if (!isStopped) {
                        mAudioEntry.observer.update();
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception: ", e);
                }
            }
        });
    }

    /**
     * Play given audio entry. In case a callback is given inside the entry, it's called after
     * successful playing.
     *
     * @param audioEntry    Audio entry with pcm audio, sample rate of provided audio and a callback
     *                      that should be called after successful execution.
     */
    void play(AudioEntry audioEntry) {
        Log.d(LOG_TAG, "add " + audioEntry.audio.length + " bytes to queue");
        isStopped = false;
        mQueue.offer(audioEntry);
    }

    void stop() {
        mQueue.clear();
        isStopped = true;
        mTrack.pause();
        mTrack.flush();
    }

    public static class AudioEntry {
        final private byte[] audio;
        AudioFinishedObserver observer;

        public AudioEntry(byte[] audio, AudioFinishedObserver observer) {
            this.audio = audio;
            this.observer = observer;
        }
    }
}
