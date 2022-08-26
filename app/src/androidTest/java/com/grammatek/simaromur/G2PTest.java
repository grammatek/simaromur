package com.grammatek.simaromur;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.grammatek.simaromur.frontend.Pronunciation;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class G2PTest {

    private final static Context context = ApplicationProvider.getApplicationContext();

    private final String INPUT_TEXT = "hlíðin er pökkuð";
    private final String SAMPA_TRANSCRIPT = "l_0 i: D I n E r p_h 9 h k Y D";
    private final String FLITE_TRANSCRIPT = "lz ii D I n E r ph oe h k Y D";
    private final String IPA_TRANSCRIPT = "l̥ iː ð ɪ n ɛ r pʰ œ h k ʏ ð";

    @Test
    public void transcribeTest() {
        Pronunciation transcriber = new Pronunciation(context);
        String transcribed = transcriber.transcribe(INPUT_TEXT);
        assertEquals(SAMPA_TRANSCRIPT, transcribed);
    }

    @Test
    public void convertTest() {
        Pronunciation transcriber = new Pronunciation(context);
        String converted = transcriber.convert(SAMPA_TRANSCRIPT,
                "SAMPA", "FLITE");
        assertEquals(FLITE_TRANSCRIPT, converted);

        converted = transcriber.convert(SAMPA_TRANSCRIPT,
                "SAMPA", "IPA");
        assertEquals(IPA_TRANSCRIPT, converted);

        converted = transcriber.convert(IPA_TRANSCRIPT,
                "IPA", "SAMPA");
        assertEquals(SAMPA_TRANSCRIPT, converted);
    }
}

