package com.grammatek.simaromur;

import android.content.Context;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.grammatek.simaromur.frontend.g2p.CompoundTree;
import com.grammatek.simaromur.frontend.g2p.PronDictEntry;
import com.grammatek.simaromur.frontend.g2p.Pronunciation;
import com.grammatek.simaromur.frontend.g2p.Syllabification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
// we need this to run the tests explicitly against sdk 28, sdk 29 and 30 need Java 9, we are
// using Java 8
@Config(sdk = {Build.VERSION_CODES.P})

public class SyllabificationTest {
    private final Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void testSyllabify() {
        Pronunciation pron = new Pronunciation(context);
        PronDictEntry entry1 = new PronDictEntry("strigaskór", "s t r I: G a s k ou r");
        CompoundTree tree1 = CompoundTree.buildTree(entry1);
        PronDictEntry entry2 = new PronDictEntry("flugvallarstarfsmaður",
                "f l Y: G v a t l a r s t a r f s m a D Y r");
        CompoundTree tree2 = CompoundTree.buildTree(entry2);
        List<CompoundTree> treeList = new ArrayList<>();
        treeList.add(tree1);
        treeList.add(tree2);
        Syllabification syllabification = new Syllabification();
        List<PronDictEntry> results = syllabification.syllabifyCompoundTrees(treeList);
        assertEquals("s t r I: G.a.s k ou r", results.get(0).syllableDotFormat());
        assertEquals("f l Y: G v.a t l.a r.s t a r f s.m a D.Y r",
                results.get(1).syllableDotFormat());
    }
}

