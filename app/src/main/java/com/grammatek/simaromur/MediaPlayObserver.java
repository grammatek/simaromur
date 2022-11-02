package com.grammatek.simaromur;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.util.Log;

import com.grammatek.simaromur.audio.AudioObserver;
import com.grammatek.simaromur.device.TTSAudioControl;

import java.io.IOException;

public class MediaPlayObserver implements AudioObserver {
    private final static String LOG_TAG = "Simaromur_" + MediaPlayObserver.class.getSimpleName();
    private final MediaPlayer mMediaPlayer;

    public MediaPlayObserver() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(new MPOnErrorListener());
    }

    /**
     * This class transforms a byte array into a MediaDataSource consumable by the Media Player
     */
    private static class ByteArrayMediaDataSource extends MediaDataSource {
        private final byte[] data;

        public ByteArrayMediaDataSource(byte[] data) {
            assert data != null;
            this.data = data;
        }
        @Override
        public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
            if (position > getSize()) {
                return 0;
            }
            int adaptedSize = size;
            if (position + (long) size > getSize()) {
                adaptedSize = (int) getSize() - (int) position;
            }
            System.arraycopy(data, (int) position, buffer, offset, adaptedSize);
            return adaptedSize;
        }

        @Override
        public long getSize() throws IOException {
            return data.length;
        }

        @Override
        public void close() throws IOException {
            // Nothing to do here
        }
    }

    /**
     * Media Player Error listener
     */
    private static class MPOnErrorListener implements MediaPlayer.OnErrorListener {
        public MPOnErrorListener() {
            // TODO: set some callback destinations ?
        }
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            String whatVerbose;
            switch(what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    whatVerbose = "Mediaplayer server died";
                    break;
                case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                default:    // FALLTHROUGH
                    whatVerbose = "Unknown media error";
                    break;
            }
            String extraVerbose;
            final int MEDIA_ERROR_SYSTEM = -2147483648;
            switch(extra) {
                case MediaPlayer.MEDIA_ERROR_IO:
                    extraVerbose = "I/O error";
                    break;
                case MediaPlayer.MEDIA_ERROR_MALFORMED:
                    extraVerbose = "malformed media";
                    break;
                case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                    extraVerbose = "unsupported media";
                    break;
                case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                    extraVerbose = "timeout error";
                    break;
                case MEDIA_ERROR_SYSTEM:
                    extraVerbose = "system error";
                    break;
                default:
                    extraVerbose = "unknwon reason";
                    break;
            }
            Log.e(LOG_TAG, "What: " + whatVerbose + "Info: " + extraVerbose);
            return false;   // this triggers the onCompletionListener to be called
        }
    }

    /**
     * Media Player Completion listener
     */
    public static class MPOnCompleteListener implements MediaPlayer.OnCompletionListener {
        TTSAudioControl.AudioFinishedObserver mAudioFinishedObserver;
        public MPOnCompleteListener(TTSAudioControl.AudioFinishedObserver audioFinishedObserver) {
            mAudioFinishedObserver = audioFinishedObserver;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mAudioFinishedObserver != null) {
                mAudioFinishedObserver.hasFinished();
            }
        }
    }

    // interface implementation

    @Override
    public void update(byte[] audioData, TTSRequest ttsRequest) {
        ByteArrayMediaDataSource dataSource = new ByteArrayMediaDataSource(audioData);
        try {
            // resetting mediaplayer instance to evade problems
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(dataSource);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            // @todo: implement MediaPlayer completion callbacks for visual feedback
        } catch (IOException ex) {
            Log.e(LOG_TAG, "Exception caught: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Update MediaPlayer with audio asset file.
     *
     * @param context           Context for the MediaPlayer to be associated with
     * @param assetFilename     Asset filename to play. This can have any format the MediaPlayer
     *                          supports.
     */
    @Override
    public void update(Context context, String assetFilename) {
        try {
            AssetFileDescriptor descriptor = context.getAssets().openFd(assetFilename);
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            mMediaPlayer.prepare();
            mMediaPlayer.setLooping(false);
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void error(String errorMsg, TTSRequest ttsRequest) {
        Log.e(LOG_TAG, "MediaPlayObserver()::error: " + errorMsg +
                "for " + ttsRequest.serialize());
    }

    public void stop(TTSRequest ttsRequest) {
        Log.v(LOG_TAG, "MediaPlayObserver()::stop: (" + ttsRequest.serialize() + ")");
    }

    @Override
    public float getPitch() {
        // we don't know the current pitch, so return 1.0
        return 1.0f;
    }

    @Override
    public float getSpeed() {
        // we don't know the current speed, so return 1.0
        return 1.0f;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }
    public void stop() {
        Log.v(LOG_TAG, "MediaPlayObserver::stop()");
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
    }
}
