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

    TTSAudioControl(int sampleRate) {
        mExecutorService = Executors.newSingleThreadExecutor();
        int channels = AudioFormat.CHANNEL_OUT_MONO;
        int encoding = AudioFormat.ENCODING_PCM_16BIT;
        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channels, encoding);
        mTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        // CONTENT_TYPE_MUSIC ?
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(encoding)
                        .setChannelMask(channels)
                        .build(),
                minBufferSize,
                AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
        );
        mTrack.play();

        mExecutorService.execute(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    mAudioEntry = mQueue.take();
                    Log.d(LOG_TAG, "now playing " + mAudioEntry.audio.length + " samples");
                    int bufPos = 0;
                    while (bufPos < mAudioEntry.audio.length && !mAudioEntry.isStopped) {
                        int buffer = Math.min(minBufferSize, mAudioEntry.audio.length - bufPos);
                        int written = mTrack.write(mAudioEntry.audio, bufPos, buffer, AudioTrack.WRITE_BLOCKING);
                        //index += minBufferSize;
                        bufPos += written;
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception: ", e);
                }
            }
        });
    }

    void play(AudioEntry audioEntry) {
        Log.d(LOG_TAG, "add " + audioEntry.audio.length + " samples to queue");
        mQueue.offer(audioEntry);
    }

    void stop() {
        mQueue.clear();
        if (mAudioEntry != null) {
            mAudioEntry.stop();
        }
    }

    static class AudioEntry {
        final private byte[] audio;
        private boolean isStopped;

        AudioEntry(byte[] audio) {
            this.audio = audio;
        }

        private void stop() {
            isStopped = true;
        }
    }
}
