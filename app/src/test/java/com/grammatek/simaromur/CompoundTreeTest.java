package com.grammatek.simaromur;

import android.content.Context;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.grammatek.simaromur.frontend.g2p.CompoundTree;
import com.grammatek.simaromur.frontend.g2p.PronDictEntry;
import com.grammatek.simaromur.frontend.g2p.Pronunciation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
// we need this to run the tests explicitly against sdk 28, sdk 29 and 30 need Java 9, we are
// using Java 8
@Config(sdk = {Build.VERSION_CODES.P})

public class CompoundTreeTest {
    private final Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void testSimpleCompound() {
        Pronunciation pron = new Pronunciation(context);
        PronDictEntry entry = new PronDictEntry("strigaskór", "s t r I: G a s k ou r");
        CompoundTree segmented = CompoundTree.buildTree(entry);
        assertEquals("strigaskór", segmented.getElem().getWord());
        assertEquals("striga", segmented.getLeft().getElem().getWord());
        assertEquals("skór", segmented.getRight().getElem().getWord());
    }

    @Test
    public void testExtractComponents() {
        Pronunciation pron = new Pronunciation(context);
        PronDictEntry entry = new PronDictEntry("flugvallarstarfsmaður",
                "f l Y: G v a t l a r s t a r f s m a D Y r");
        CompoundTree segmented = CompoundTree.buildTree(entry);

        assertEquals("flugvallarstarfsmaður", segmented.getElem().getWord());
        assertEquals("flugvallar", segmented.getLeft().getElem().getWord());
        assertEquals("starfsmaður", segmented.getRight().getElem().getWord());
        assertEquals("starfs", segmented.getRight().getLeft().getElem().getWord());
        assertEquals("maður", segmented.getRight().getRight().getElem().getWord());
    }

}
