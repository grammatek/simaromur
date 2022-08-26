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

    @Test
    public void transcribeTest() {
        Pronunciation transcriber = new Pronunciation(context);
        String transcribed = transcriber.transcribe("hljóðrita þetta");
        assertEquals("l_0 j ou D r I t a T E h t a", transcribed);
    }

    @Test
    public void convertTest() {
        Pronunciation transcriber = new Pronunciation(context);
        String converted = transcriber.convert("l_0 j ou: D r I t_h a T E h t a",
                "SAMPA", "FLITE");
        assertEquals("lz j ouu D r I th a T E h t a", converted);
    }
}

