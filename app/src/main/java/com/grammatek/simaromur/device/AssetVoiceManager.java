package com.grammatek.simaromur.device;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.grammatek.simaromur.App;
import com.grammatek.simaromur.utils.FileUtils;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.device.pojo.DeviceVoiceFile;
import com.grammatek.simaromur.device.pojo.DeviceVoices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles voices bundled inside the assets/ directory. It parses the voice info
 * description and updates the voice model with the appropriate info.
 *
 * TODO: extend to downloaded voices as well. Then we'd need to rename this class to DeviceVoiceManager
 */
public class AssetVoiceManager {
    private final static String LOG_TAG = "Simarómur_Java_" + AssetVoiceManager.class.getSimpleName();

    final private DeviceVoices assetVoices;
    final private AssetManager assetManager;

    /**
     * Constructor. Parses voice description from assets and validates it. Afterwards, voice meta data and
     * corresponding files are retrievable via getters.
     *
     * @param context   Application context to access assets
     */
    public AssetVoiceManager(final Context context) throws IOException {
        assetManager = context.getAssets();
        // parse and validate given JSON string
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String voiceDescriptionJson = new String(FileUtils.readFileFromAssets(assetManager,
                "voices/voice-info.json"), StandardCharsets.UTF_8);
        assetVoices = gson.fromJson(voiceDescriptionJson, DeviceVoices.class);

        if (!checkVoiceFilesInAssets()) {
            throw new RuntimeException("Missing or corrupted voice file detected !");
        }
    }

    /**
     * Checks if all voice files do exist and have the correct MD5sum.
     *
     * @return  true in case all voices are correct, false otherwise
     */
    private boolean checkVoiceFilesInAssets() {
        // Check existence of all voice files
        for (DeviceVoice voice:getVoiceList().Voices) {
            for (DeviceVoiceFile voiceFile:voice.Files) {
                try {
                    String filePath = "voices/" + voiceFile.Path;
                    InputStream iStream = assetManager.open(filePath);
                    String md5sum = FileUtils.getMD5SumOfInputStream(iStream);
                    iStream.close();
                    if ((md5sum == null) || !md5sum.equals(voiceFile.Md5Sum)) {
                        return false;
                    }
                }
                catch (IOException e) {
                    Log.e(LOG_TAG, "Failed to open voice asset file: " + voiceFile.Path, e);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns voice information from assets directory.
     *
     * @return  Voice information about all voices packaged in assets
     */
    public DeviceVoices getVoiceList() {
        return assetVoices;
    }

    /**
     * Converts Asset voices to DB voice
     *
     * @return  Asset voices as list of Db voices
     */
    public List<Voice> getVoiceDbList() {
        List<Voice> voices = new ArrayList<>();
        for (DeviceVoice aDevVoice : assetVoices.Voices) {
            try {
                Voice aVoice = new Voice(assetManager, aDevVoice);
                voices.add(aVoice);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return voices;
    }

    /**
     * Returns voice information for specified voice packaged in assets.
     *
     * @param name  Name of the voice in assets
     *
     * @return  Voice information about specified voice
     *
     * @throws  IllegalArgumentException in case voice is not found
     *
     * TODO: shouldn't we use the internalName here? We are doing this already in
     *       DownloadVoiceManager.getInfoForVoice()
     */
    public DeviceVoice getInfoForVoice(String name) throws IOException {
        for (DeviceVoice voice:getVoiceList().Voices) {
            if (voice.Name.equals(name)) {
                return voice;
            }
        }
        throw new IOException("No such voice: " + name);
    }

    /**
     * Returns boolean, if given voice name exists as a voice inside assets.
     *
     * @param name      Name of the voice
     *
     * @return  true in case a voice with given name exists in assets, false otherwise
     */
    public boolean voiceExistsInAssets(String name) {
        for (DeviceVoice voice:assetVoices.Voices) {
            if (voice.Name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads a specific file from a voice and return it as byte array.
     *
     * @param voiceName name of the voice
     * @param fileName  filename to use
     *
     * @return  byte array of the read voice. In case neither the voiceName or the fileName is correct
     *          or the file could not be read, an exception is thrown
     */
    public byte[] loadAssetVoiceFile(String voiceName, String fileName) throws IOException {
        DeviceVoice voice = getInfoForVoice(voiceName);
        for (DeviceVoiceFile file:voice.Files) {
            if (file.Path.equals(fileName)) {
                String filePath = "voices/" + file.Path;
                return FileUtils.readFileFromAssets(assetManager, filePath);
            }
        }
        throw new IOException("Voice " + voiceName + " provides no such file: " + fileName);
    }

    /**
     * Copies an asset file given by srcAssetFile to a path relative to the data directory path
     * given by destDataSubPath.
     * Creates the destDataSubPath, in case it doesn't already exist
     *
     * @param srcAssetFile    File name  of the asset relative from Assets start
     * @param destDataSubPath Relative path inside the data directory where to copy the asset file
     *                        into
     * @param context         Application context to get hold of the Asset manager
     */
    public static void copyFromAssets(String srcAssetFile, String destDataSubPath, final Context context) {
        final String datapath = new File(App.getDataPath()).getParent();

        try {
            // create destination directory if not exists
            String destDir = datapath + "/" + destDataSubPath + "/";
            try {
                File dir = new File(destDir);
                if (!dir.exists()) {
                    boolean rv = dir.mkdir();
                    if (!rv) {
                        Log.e(LOG_TAG, "Failed to create directory: " + destDir);
                    }
                }
            } catch (SecurityException e) {
                Log.e(LOG_TAG, "Failed to create directory: " + destDir, e);
            }
            try {
                File outFile = new File(destDir, srcAssetFile);
                OutputStream outStream = new FileOutputStream(outFile);
                AssetManager assetManager = context.getAssets();
                InputStream inStream = assetManager.open(srcAssetFile);

                copyFile(inStream, outStream);
                inStream.close();
                outStream.flush();
                outStream.close();
                Log.i(LOG_TAG, "Copied " + srcAssetFile + " to " + outFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to copy asset file: " + srcAssetFile, e);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to get asset file list.", e);
        }
    }

    private static void copyFile(InputStream inStream, OutputStream outStream) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, read);
        }
    }
}
