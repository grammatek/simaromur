package com.grammatek.simaromur;

import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.util.Log;

import com.grammatek.simaromur.audio.AudioObserver;

import java.io.IOException;

public class MediaPlayObserver implements AudioObserver {
    private final static String LOG_TAG = "Simaromur_" + MediaPlayObserver.class.getSimpleName();
    private final MediaPlayer mMediaPlayer;

    public MediaPlayObserver() {
        mMediaPlayer = new MediaPlayer();
    }

    /**
     * This class transforms a byte array into a MediaDataSource consumable by the Media Player
     */
    public static class ByteArrayMediaDataSource extends MediaDataSource {
        private final byte[] data;

        public ByteArrayMediaDataSource(byte []data) {
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
            System.arraycopy(data, (int)position, buffer, offset, adaptedSize);
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

    // interface implementation

    public void update(byte[] audioData) {
        ByteArrayMediaDataSource dataSource = new ByteArrayMediaDataSource(audioData);
        try {
            // resetting mediaplayer instance to evade problems
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(dataSource);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            // @todo: implement MediaPlayer completion callbacks for visual feedback
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }
    public void error(String errorMsg) {
        Log.e(LOG_TAG, "MediaPlayObserver()::error: " + errorMsg);
    }

    public void stop() {
        Log.v(LOG_TAG, "MediaPlayObserver::stop()");
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
    }
}
