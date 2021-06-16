package com.grammatek.simaromur.frontend;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.grammatek.simaromur.App;
import com.grammatek.simaromur.TTSDemo;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * The NormalizationManager controls the normalization process from raw input text to
 * normalized text. It contains:
 *      - a pre-normalization step to clean unicode, i.e. reduce the number of
 *        characters by deleting irrelevant characters and reducing similar characters to one
 *        (e.g. dash and hyphen variations to hypen-minus (\u202d or 45 decimal))
 *      - a tokenizing step
 *      - the core normalization step composed of pre-normalization, pos-tagging and
 *      post-normalization
 */

public class NormalizationManager {
    private final static boolean DEBUG = false;
    private final static String LOG_TAG = "Simaromur_Java_" + NormalizationManager.class.getSimpleName();
    private static final String POS_MODEL = "is-pos-reduced-maxent.bin";

    private final Context mContext;
    private final POSTaggerME mPosTagger;
    private TTSUnicodeNormalizer mUnicodeNormalizer;
    private Tokenizer mTokenizer;
    private TTSNormalizer mTTSNormalizer;
    private String mAssetsPath;

    public NormalizationManager(Context context) {
        mContext = context;
        mUnicodeNormalizer = new TTSUnicodeNormalizer();
        mTokenizer = new Tokenizer(context);
        mTTSNormalizer = new TTSNormalizer();
        mAssetsPath = new File(App.getDataPath()).getParent();
        copyAssets("pos");
        mPosTagger = initPOSTagger();
    }

    /**
     * Processes the input text according to the defined steps: unicode cleaning,
     * tokenizing, normalizing
     * @param text
     * @return normalized version of 'text'
     */
    public String process(final String text) {
        String cleaned = mUnicodeNormalizer.normalizeEncoding(text);
        List<String> tokenized = mTokenizer.detectSentences(cleaned);
        List<String> normalizedSentences = normalize(tokenized);

        return list2string(normalizedSentences);
    }

    // pre-normalization, tagging and final normalization of the sentences in 'tokenized'
    private List<String> normalize(final List<String> tokenized) {
        String preNormalized;
        List<String> normalized = new ArrayList<>();

        for (String sentence : tokenized) {
            preNormalized = mTTSNormalizer.preNormalize(sentence);
            String[] tags = tagText(preNormalized);
            // preNormalized is tokenized as string, so we know splitting on whitespace will give
            // us the correct tokens according to the tokenizer
            normalized.add(mTTSNormalizer.postNormalize(preNormalized.split(" "), tags));
        }

        return normalized;
    }

    private String list2string(final List<String> normalizedSentences) {
        StringBuilder sb = new StringBuilder();
        for (String sentence : normalizedSentences) {
            sb.append(" ");
            sb.append(sentence);
        }
        return sb.toString().trim();
    }

    private String[] tagText(final String text) {
        String[] tags = {};
        String[] tokens = text.split(" ");
        if (mPosTagger == null) {
            //TODO: proper error handling here - what do we do if the tagger fails?
            Log.e(LOG_TAG, "POSTagger is not initialized!");
        }
        tags = mPosTagger.tag(tokens);

        if (DEBUG)
            printProbabilities(tags, mPosTagger, tokens);

        return tags;
    }

    private POSTaggerME initPOSTagger() {
        POSTaggerME posTagger = null;
        try {
            POSModel posModel = new POSModel(new File(mAssetsPath + "/pos/" + POS_MODEL));
            posTagger = new POSTaggerME(posModel);

        } catch(IOException e) {
            e.printStackTrace();
        }
        return posTagger;
    }

    // Get the probabilities of the tags given to the tokens to inspect
    private void printProbabilities(String[] tags, POSTaggerME posTagger, String[] tokens) {
        double probs[] = posTagger.probs();
        Log.v(LOG_TAG, "Token\t:\tTag\t:\tProbability\n--------------------------");
        for(int i=0;i<tokens.length;i++){
            Log.v(LOG_TAG, tokens[i]+"\t:\t"+tags[i]+"\t:\t"+probs[i]);
        }
    }

    private void copyAssets(String assetSubPath) {
        AssetManager assetManager = mContext.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(assetSubPath);
            // create destination assetSubPath if not exists
            String destDir = mAssetsPath + "/" + assetSubPath + "/";
            try {
                File dir = new File(destDir);
                if (!dir.exists())
                    dir.mkdir();
            } catch(SecurityException e) {
                Log.e(LOG_TAG, "Failed to create directory: " + destDir, e);
            }
            for(String filename : files) {
                try {
                    File outFile = new File(destDir, filename);
                    OutputStream outStream = new FileOutputStream(outFile);
                    InputStream inStream = assetManager.open(assetSubPath + "/" + filename);

                    copyFile(inStream, outStream);
                    inStream.close();
                    outStream.flush();
                    outStream.close();
                    Log.i(LOG_TAG, "Copied " + filename + " to " + outFile.getAbsolutePath());
                } catch(IOException e) {
                    Log.e(LOG_TAG, "Failed to copy asset file: " + filename, e);
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to get asset file list.", e);
        }
    }

    private void copyFile(InputStream inStream, OutputStream outStream) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, read);
        }
    }
}

