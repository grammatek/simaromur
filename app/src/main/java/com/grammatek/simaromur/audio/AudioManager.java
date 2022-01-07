package com.grammatek.simaromur.audio;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;

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
     * @param monoPcmData byte array of Mono PCM data to be used as input data
     * @param sampleRate  sample Rate of the given pcm buffer
     * @param pitch   pitch to be applied. 1.0f means no pitch change, values > 1.0 mean higher
     *                pitch, values < 1.0 mean lower pitch than in given pcmData
     * @param speed   speed to be applied. 1.0f means no speed change, values > 1.0 mean higher
     *                speed, values < 1.0 mean lower speed than in given pcmData. This parameter
     *                produces either more data for values >1.0, less data for values < 1.0, or
     *                no data change for a value of 1.0
     * @return new byte array with converted PCM data
     */
    static public byte[] applyPitchAndSpeed(final byte[] monoPcmData, int sampleRate, float pitch, float speed) {
        ByteArrayOutputStream outputConversionStream = new ByteArrayOutputStream();
        if (pitch == 1.0 && speed == 1.0) {
            outputConversionStream.write(monoPcmData, 0, monoPcmData.length);
        } else {
            Log.i(LOG_TAG, "Applying pitch " + pitch + ", speed " + speed);
            Sonic sonic = new Sonic(sampleRate, N_CHANNELS);
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
            InputStream inputStream = new ByteArrayInputStream(monoPcmData);
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

    /**
     * Either apply pitch and speed to ttsData, resulting in a potentially differently sized output
     * buffer, or simply copy ttsData to the new output buffer, if no changes of speed or pitch
     * are requested.
     * Return the newly created output buffer.
     *
     * @param monoPcmData byte array of MONO PCM data to be used as input data. 22050 Hz sample rate
     *                    is expected
     * @param pitch   pitch to be applied. 1.0f means no pitch change, values > 1.0 mean higher
     *                pitch, values < 1.0 mean lower pitch than in given pcmData
     * @param speed   speed to be applied. 1.0f means no speed change, values > 1.0 mean higher
     *                speed, values < 1.0 mean lower speed than in given pcmData. This parameter
     *                produces either more data for values >1.0, less data for values < 1.0, or
     *                no data change for a value of 1.0
     * @return new byte array with converted PCM data
     */
    static public byte[] applyPitchAndSpeed(final byte[] monoPcmData, float pitch, float speed) {
        return applyPitchAndSpeed(monoPcmData, SAMPLE_RATE_WAV, pitch, speed);
    }

    /**
     * Converts given float values to 16bits PCM. No resampling or interpolation is done.
     * Floats are rounded to the nearest integer.
     *
     * @param pcmFloats 22kHz pcm floats [-32768.0 .. 32767.0]
     *
     * @return byte array PCM big endian
     */
    static public byte[] pcmFloatMelganTo16BitPCM(float[] pcmFloats) {
        // given bit depth has to be discrete 8..32bit
        final int bitDepth = 16;
        Log.v(LOG_TAG, "Converting " + pcmFloats.length + " float samples to " + bitDepth + " bit wav ...");
        final int ByteRate = bitDepth / 8;
        final float MAX_VAL=32767.0F;
        final float MIN_VAL=-32768.0F;
        byte[] outBuf = new byte[pcmFloats.length * ByteRate];
        int nClipped = 0;

        ByteBuffer buffer = ByteBuffer.allocate(pcmFloats.length * ByteRate);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        float min = MAX_VAL;
        float max = MIN_VAL;
        for (float fSample:pcmFloats) {
            if (fSample > max)
                max = fSample;
            if (fSample < min)
                min = fSample;

            // detect clipping
            if (fSample > MAX_VAL) {
                fSample = MAX_VAL;
                nClipped++;
            } else if (fSample < MIN_VAL) {
                fSample = MIN_VAL;
                nClipped++;
            }

            // Round to nearest short value
            buffer.putShort((short) Math.round(fSample));
        }

        if (nClipped > 0) {
            Log.w(LOG_TAG, "pcmFloat2Wwav: " + nClipped + "clipped samples detected");
        }
        // Log.v(LOG_TAG, "pcmFloat2Wwav: values min/max: " + min + "/" + max);

        buffer.rewind();
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.get(outBuf);
        return outBuf;
    }

    static public byte[] pcmFloatMelganTo16BitPCM2(float[] pcmFloats) {
        for (int i=0; i<pcmFloats.length; ++i) {
            pcmFloats[i] /= 20000.0f;
        }
        return pcmFloatTo16BitPCM(pcmFloats);
    }

    /**
     * Converts given float values to 16bits PCM. No resampling or interpolation is done.
     * Floats are remapped from [-1.0 .. 1.0] => [-32768 .. 32767] by rounding to nearest integer.
     *
     * @param pcmFloats pcm floats [-1.0 .. 1.0]
     *
     * @return byte array PCM big endian
     */
    static public byte[] pcmFloatTo16BitPCM(float[] pcmFloats) {
        final int bitDepth = 16;
        Log.v(LOG_TAG, "pcmFloatTo16BitPCM: Converting " + pcmFloats.length + " float samples to " + bitDepth + " bit wav ...");
        final int ByteRate = bitDepth / 8;
        final int MULT_FACTOR = (int) Math.pow(2, bitDepth-1);
        int nClipped = 0;

        ByteBuffer buffer = ByteBuffer.allocate(pcmFloats.length * ByteRate);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        float min = 0.0F;
        float max = 0.0F;
        for (float fSample:pcmFloats) {
            if (fSample > max)
                max = fSample;
            if (fSample < min)
                min = fSample;

            // detect clipping
            if (fSample > 1.0F) {
                fSample = 1.0F;
                nClipped++;
            } else if (fSample < -1.0) {
                fSample = -1.0F;
                nClipped++;
            }

            // For float => int conversion, we use method 4 (asymmetric) from
            // http://blog.bjornroche.com/2009/12/int-float-int-its-jungle-out-there.html
            int valShort = Math.round(fSample > 0.0 ? fSample * (MULT_FACTOR-1) : fSample * MULT_FACTOR);
            buffer.putShort((short) valShort);
        }

        if (nClipped > 0) {
            Log.w(LOG_TAG, "pcmFloatTo16BitPCM: " + nClipped + " clipped samples detected");
        }
        Log.w(LOG_TAG, "pcmFloatTo16BitPCM: values min/max: " + min + "/" + max);

        buffer.rewind();
        buffer.order(ByteOrder.BIG_ENDIAN);
        byte[] outBuf = new byte[pcmFloats.length * ByteRate];
        buffer.get(outBuf);
        Log.v(LOG_TAG, "Done.");
        return outBuf;
    }


    /**
     * This is a simple implementation of a highpass triangular-PDF dither (a good general-purpose
     * dither) with optional 2nd-order noise shaping (which lowers the noise floor by 11dB below
     * 0.1 Fs) which is applied to the given float values before converting them to 16 bit big
     * endian PCM short values returned as bytes.
     *
     * @param pcmFloats         pcm floats [-normalizedValue .. normalizedValue]
     * @param normalizedValue   normalization value used for the pcmFloats input buffer, e.g. 1.0
     * @param doNoiseShaping    true: apply noise shaping, false: don't apply noise shaping
     *
     * @return  byte array PCM big endian
     *
     * Reference: https://www.musicdsp.org/en/latest/Other/61-dither-code.html
     */
    static public byte[] pcmFloatTo16BitPCMWithDither(float[] pcmFloats, float normalizedValue, boolean doNoiseShaping) {
        final int Bits = 16;
        final int ByteRate = Bits / 8;
        // set to 0.0f for no noise shaping
        final float s = doNoiseShaping ? 0.5f : 0.0f;
        final int maxRange = (int) Math.pow(2, Bits-1) - 1;     // signed number range
        final float w = (float) maxRange - 20;   // conversion value: leave headroom
        final float wi= 1.0f / w;
        final float d = wi / maxRange;  // dither amplitude (2 lsb)
        final float o = wi * 0.5f;      // remove dc offset

        Log.v(LOG_TAG, "Convert & dither " + pcmFloats.length + " float samples to " + Bits + " bit wav ...");
        ByteBuffer buffer = ByteBuffer.allocate(pcmFloats.length * ByteRate);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        Random random = new Random();

        // rectangular-PDF random numbers
        int   r1 = random.nextInt(maxRange), r2;
        float s1 = 0, s2 = 0;                   // error feedback buffers
        int nClipped = 0;
        for (float in : pcmFloats) {
            r2 = r1;                            // can make HP-TRI dither by
            r1 = random.nextInt(maxRange);      // subtracting previous rand()

            if (normalizedValue != 1.0f)
                in /= normalizedValue;          // denormalize to 1.0
            in += s * (s1 + s1 - s2);           // error feedback
            float tmp = in + o + d * (r1 - r2); // dc offset and dither

            // asymmetric conversion according to valid range of a short value
            float res = (tmp > 0.0) ? (w - 1) * tmp : w * tmp;

            // detect clipping
            if (res > maxRange) {
                res = maxRange;
                nClipped++;
            } else if (res < -maxRange-1) {
                res = -maxRange-1;
                nClipped++;
            }
            short out = (short) Math.round(res);
            s2 = s1;
            s1 = in - wi * out;                 // error
            buffer.putShort(out);
        }
        if (nClipped > 0) {
            Log.w(LOG_TAG, "pcmFloatTo16BitPCMWithDither: " + nClipped + " clipped samples detected");
        }
        buffer.rewind();
        buffer.order(ByteOrder.BIG_ENDIAN);
        byte[] outBuf = new byte[pcmFloats.length * ByteRate];
        buffer.get(outBuf);
        Log.v(LOG_TAG, "Done.");
        return outBuf;
    }
}
