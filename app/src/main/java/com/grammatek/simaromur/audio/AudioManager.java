package com.grammatek.simaromur.audio;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import thirdparty.Sonic;

public class AudioManager {
    private final static String LOG_TAG = "Simaromur_" + AudioManager.class.getSimpleName();

    // Some constants used throughout audio conversion
    public static final int SAMPLE_RATE_WAV = 16000;
    public static final int SAMPLE_RATE_MP3 = 22050;
    public static final int N_CHANNELS = 1;

    /**
     * Either apply pitch and speed to ttsData, resulting in a potentially differently sized output
     * buffer, or simply copy ttsData to the new output buffer, if no changes of speed or pitch
     * are requested.
     * Return the newly created output buffer.
     *
     * @param pcmData byte array of PCM data to be used as input data
     * @param pitch   pitch to be applied. 1.0f means no pitch change, values > 1.0 mean higher
     *                pitch, values < 1.0 mean lower pitch than in given pcmData
     * @param speed   speed to be applied. 1.0f means no speed change, values > 1.0 mean higher
     *                speed, values < 1.0 mean lower speed than in given pcmData. This parameter
     *                produces either more data for values >1.0, less data for values < 1.0, or
     *                no data change for a value of 1.0
     * @return new byte array with converted PCM data
     */
    static public byte[] applyPitchAndSpeed(final byte[] pcmData, float pitch, float speed) {
        ByteArrayOutputStream outputConversionStream = new ByteArrayOutputStream();
        if (pitch == 1.0 && speed == 1.0) {
            outputConversionStream.write(pcmData, 0, pcmData.length);
        } else {
            Log.i(LOG_TAG, "Applying pitch " + pitch + ", speed " + speed);
            Sonic sonic = new Sonic(SAMPLE_RATE_WAV, N_CHANNELS);
            int bufferSize = 8192;  // some typical buffer size, could also be 16K, 32K, ...
            int numRead = 0, numWritten;
            byte[] inBuffer = new byte[bufferSize];
            byte[] outBuffer = new byte[bufferSize];
            sonic.setSpeed(speed);
            sonic.setPitch(pitch);
            sonic.setRate(1.0f);
            sonic.setVolume(1.0f);
            sonic.setChordPitch(false);
            sonic.setQuality(0);    // is much faster without sacrificing quality ...
            InputStream inputStream = new ByteArrayInputStream(pcmData);
            do {
                try {
                    numRead = inputStream.read(inBuffer, 0, bufferSize);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (numRead <= 0) {
                    sonic.flushStream();
                } else {
                    sonic.writeBytesToStream(inBuffer, numRead);
                }
                do {
                    numWritten = sonic.readBytesFromStream(outBuffer, bufferSize);
                    if (numWritten > 0) {
                        outputConversionStream.write(outBuffer, 0, numWritten);
                    }
                } while (numWritten > 0);
            } while (numRead > 0);
        }
        return outputConversionStream.toByteArray();
    }
}
