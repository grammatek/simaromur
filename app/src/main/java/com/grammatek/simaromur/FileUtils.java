package com.grammatek.simaromur;

import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;

public class FileUtils {
    private final static String LOG_TAG = "Simaromur_" + FileUtils.class.getSimpleName();

    /**
     * Calculate MD5 message digest of given file and return it as string
     */
    public static String getMD5SumOfFile(String filePath) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(filePath);
        }
        catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "File not found: " + filePath);
            return null;
        }
        return getMD5SumOfInputStream(fis);
    }

    /**
     * Calculate MD5 message digest of given file input stream and return it as string
     */
    public static String getMD5SumOfInputStream(InputStream fis) {
        byte[] dataBytes = new byte[1024];
        int nread;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_TAG, "MD5 could not be computed");
            return null;
        }
        try {
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not read from input stream");
            return null;
        }
        finally {
            try {
                fis.close();
            } catch (IOException e) {
                // Ignoring this exception.
            }
        }

        StringBuilder sb = new StringBuilder();
        byte[] mdBytes = md.digest();
        for (byte mdByte : mdBytes) {
            sb.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }


    /**
     * Reads a file from assets and returns it as a byte array.
     *
     * @param assetManager  reference to Asset Manager
     * @param filePath      the path inside /assets of the file to load
     * @return  read-in file as a byte-array
     *
     * @throws IOException  Thrown, if there was a problem reading the asset file/date list
     */
    @NonNull
    public static byte[] readFileFromAssets(AssetManager assetManager, String filePath) throws IOException {
        InputStream iStream = assetManager.open(filePath);
        int bytesToRead = iStream.available();
        byte[] voiceBytes = new byte[bytesToRead];
        while (bytesToRead > 0) {
            int nRead = iStream.read(voiceBytes);
            if (nRead == -1) {
                break;
            }
            bytesToRead -= nRead;
        }

        iStream.close();
        return voiceBytes;
    }

    /**
     * Reads the file "lastModified.txt" which contains for each file inside assets the last
     * modification timestamp before packaging it into assets.
     *
     * This method returns a mapping of all asset file names to their last modification time.
     *
     * @param assetManager      The asset manager
     * @return                  mapping of asset filename to modification DateTime object as UTC
     *
     * @throws IOException      Thrown, if there was a problem reading the asset file/date list
     */
    public static HashMap<String, LocalDateTime> readAssetDateList(AssetManager assetManager) throws IOException {
        String lastModified = new String(readFileFromAssets(assetManager, "lastModified.txt"));
        final HashMap<String, LocalDateTime> map = new HashMap<>();
        for (String line : lastModified.split("\\n")) {
            String[] kv = line.split(" ");
            long epoch = Long.parseLong(kv[0], 10);
            map.put(kv[1], Instant.ofEpochMilli(epoch).atZone(ZoneOffset.UTC).toLocalDateTime());
        }

        return map;
    }

    /**
     * Returns the last modification DateTime object of a given asset file in UTC.
     *
     * @param assetManager      The asset manager
     * @param assetFileName     The asset file name
     * @return                  Modification DateTime object in UTC of asset file
     *
     * @throws IOException      Thrown, if given file name not found
     */
    public static LocalDateTime getAssetDate(AssetManager assetManager, String assetFileName) throws IOException {
        HashMap<String, LocalDateTime> map = readAssetDateList(assetManager);
        for (HashMap.Entry<String, LocalDateTime> entry : map.entrySet()) {
            String fullFileName = "assets/" + assetFileName;
            if (fullFileName.equals(entry.getKey())) {
                return entry.getValue();
            }
        }

        throw new IOException("No such file: " + assetFileName);
    }
}
