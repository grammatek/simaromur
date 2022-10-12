package com.grammatek.simaromur.utils;

import static java.nio.file.Files.deleteIfExists;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class FileUtils {
    private final static String LOG_TAG = "Simaromur_" + FileUtils.class.getSimpleName();
    private static HashMap<String, LocalDateTime> sAssetDateTimeMap;

    /**
     * Returns boolean, if a file with given file path exists.
     *
     * @param filePath  file path used to test if a file exists
     * @return  true in case file exists, false in case it doesn't exist or the user doesn't have
     *          sufficient permissions
     */
    public static boolean exists(String filePath) {
        return new File(filePath).isFile();
    }

    /**
     * Deletes file from file system.
     *
     * @param filePath  file path used to delete a file
     *
     * @return  true in case file has been successfully deleted, false in case the file doesn't
     *          exist or the user doesn't have sufficient permissions
     */
    public static boolean delete(String filePath) {
        boolean rv = false;
        try {
            rv = deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rv;
    }

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
     * Calculate MD5 message digest of given file input stream and return it as string
     */
    public static String getMD5SumOfString(String aText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(aText.getBytes());
            byte[] mdBytes = md.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte mdByte : mdBytes) hexString.append(Integer.toHexString(0xff & mdByte));

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
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
     * Reads a file from disk storage and returns it as a byte array.
     *
     * @param filePath      the path inside /assets of the file to load
     * @return  read-in file as a byte-array
     *
     * @throws IOException  Thrown, if there was a problem reading the file
     */
    public static byte[] readFileFromStorage(String filePath) throws IOException {
        byte[] voiceBytes = null;
        if (Files.exists(Paths.get(filePath))) {
            InputStream iStream = new FileInputStream(filePath);
            int bytesToRead = iStream.available();
            voiceBytes = new byte[bytesToRead];
            while (bytesToRead > 0) {
                int nRead = iStream.read(voiceBytes);
                if (nRead == -1) {
                    break;
                }
                bytesToRead -= nRead;
            }
            iStream.close();
        }
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

    /**
     * Copy asset files recursively to the data directory of the application.
     *
     * @param assetManager  asset manager of app context
     * @param assetSubPath  Path inside assets where to look for files to copy
     * @param destPath      destination directory to copy files into, if it doesn't exist, it's
     *                      created
     */
    public static void copyAssetFilesRecursive(AssetManager assetManager, String assetSubPath, String destPath) throws IOException {
        String[] files;
        if (sAssetDateTimeMap == null) {
            // lazy load mapping of asset files to DateTime
            sAssetDateTimeMap = FileUtils.readAssetDateList(assetManager);
        }

        try {
            files = assetManager.list(assetSubPath);
            // create destination assetSubPath if not exists
            mkdir(destPath);
            String destDir = destPath + "/";
            for(String filename : files) {
                try {
                    String assetFileName = assetSubPath + "/" + filename;
                    String outputFileName = destDir + filename;
                    if (Files.exists(Paths.get(outputFileName))) {
                        // don't copy if destination file exists and is newer than asset file
                        final LocalDateTime assetFileTime = sAssetDateTimeMap.get("assets/" + assetFileName);
                        final LocalDateTime destFileTime = FileUtils.getModificationDateTimeOfFile(outputFileName);
                        if (assetFileTime != null && destFileTime.isAfter(assetFileTime)) {
                            Log.i(LOG_TAG, "Asset already copied: " + assetFileName + " on: " +
                                    destFileTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy:H:m:s")));
                            continue;
                        }
                    }
                    OutputStream outStream = new FileOutputStream(new File(destDir, filename));
                    InputStream inStream = assetManager.open(assetFileName);

                    copyFile(inStream, outStream);
                    inStream.close();
                    outStream.flush();
                    outStream.close();
                    Log.i(LOG_TAG, "Copied " + filename + " to " + outputFileName);
                } catch(IOException e) {
                    Log.e(LOG_TAG, "Failed to copy asset file: " + filename, e);
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to get asset file list.", e);
        }
    }

    /**
     * Creates a directory if it doesn't already exist. Creates also parent directories, if necessary.
     *
     * @param destPath  destination path to create
     *
     * @return  true in case the directory has been successfully created, false otherwise
     */
    public static boolean mkdir(String destPath) {
        boolean rv = false;
        try {
            File dir = new File(destPath);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new IOException("Couldn't create " + destPath);
                }
            }
            rv = true;
        } catch(SecurityException | IOException e) {
            Log.e(LOG_TAG, "Failed to create directory: " + destPath, e);
        }
        return rv;
    }

    /**
     * Copy input stream to output stream. Both streams need to exist and be open
     *
     * @param inStream      Input stream
     * @param outStream     Output stream
     *
     * @throws IOException  In case any I/O error happens
     */
    public static void copyFile(InputStream inStream, OutputStream outStream) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        while((read = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, read);
        }
    }

    /**
     * Returns the LocalDateTime object of given file as last modification time.
     *
     * @param fileName      Filename to be used for operation
     * @return              LocalDateTime object of modification time of file
     *
     * @throws IOException  In case any I/O error happens
     */
    public static LocalDateTime getModificationDateTimeOfFile(String fileName) throws IOException {
        Path file = Paths.get(fileName);
        long millis = Files.getLastModifiedTime(file).toMillis();
        return  Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDateTime();
    }

    /**
     * Reads a resource file with the given resID, e.g. R.raw.sampa_ipa_single_flite.
     * Returns the file content as a list of strings.
     *
     * @param context  Context of the app
     * @param resID the resource id
     * @return a list of strings representing file content
     */
    public static List<String> readLinesFromResourceFile(Context context, int resID) {
        Resources res = context.getResources();
        List<String> fileContent = new ArrayList<>();
        String line = "";
        try {
            InputStream is = res.openRawResource(resID);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if (is != null) {
                while ((line = reader.readLine()) != null) {
                    fileContent.add(line);
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not read resource " + resID + " : " + e.getMessage());
        }
        return fileContent;
    }

    /**
     * Writes a buffer of bytes to a file.
     *
     * @param filePath      Path to file to be written
     * @param bytes         Buffer of bytes to be written
     * @return             true in case of success, false otherwise
     */
    public static boolean writeFileToStorage(String filePath, byte[] bytes) {
        boolean rv = false;
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(bytes);
            fos.close();
            rv = true;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not write file " + filePath + " to storage: "
                    + e.getMessage());
        }
        return rv;
    }

    /**
     * Reads the config.properties file from the assets directory and returns the contents of the
     * given key
     * @param assetManager      AssetManager to be used for operation
     * @param key               Key to be used for operation
     * @return            Value of key in config.properties
     */
    public static String getAssetConfigProperty(AssetManager assetManager, String key) throws IOException {
        InputStream inputStream = assetManager.open("config.properties");
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();
        return properties.getProperty(key);
    }
}
