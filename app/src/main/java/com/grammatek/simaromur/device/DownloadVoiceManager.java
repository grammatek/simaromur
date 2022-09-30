package com.grammatek.simaromur.device;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.grammatek.simaromur.App;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.db.VoiceDao;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.device.pojo.DeviceVoiceFile;
import com.grammatek.simaromur.device.pojo.DeviceVoices;
import com.grammatek.simaromur.network.remoteasset.ProgressObserver;
import com.grammatek.simaromur.network.remoteasset.ReleaseInfo;
import com.grammatek.simaromur.network.remoteasset.VoiceFile;
import com.grammatek.simaromur.network.remoteasset.VoiceInfo;
import com.grammatek.simaromur.network.remoteasset.VoiceRepo;
import com.grammatek.simaromur.utils.AsyncThread;
import com.grammatek.simaromur.utils.Decompress;
import com.grammatek.simaromur.utils.FileUtils;
import com.grammatek.simaromur.utils.SystemUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * This class handles voices that can be downloaded from the Github Símarómur voices repository.
 * It fetches the current available voice descriptions and updates the voice model with the appropriate info.
 * It also is used to check, if voices are already downloaded to the data directory and provides
 * accessor methods for these.
 *
 * The ultimate source of truth isn't the DB, but the voice descriptions fetched from the Github repo
 * and the voice description file in the data directory. If a voice is downloaded, it is added to the
 * voice description file in the data directory. If a voice is deleted, it is removed from the voice
 * description file in the data directory. The DB is updated accordingly.
 */
public class DownloadVoiceManager {
    private static final String LOG_TAG = "Simarómur_Java_" + DownloadVoiceManager.class.getSimpleName();
    private static final String sReleaseName = "0.1 release";
    private static final String sVoiceInternalName = "Alfur_flite";
    // All device voices available on disk
    private DeviceVoices mVoicesOnDisk;
    // All device voices available on server
    private DeviceVoices mVoicesOnServer;
    // this is the path where disk voices are stored
    private final String mVoiceDownloadPath;
    private VoiceRepo mVoiceRepo = null;
    private Call mCallDownloadVoice = null;
    private AsyncThread mAsyncThread = null;

    /**
     * Interface for an observer that is called when finished with playing audio.
     */
    public interface DownloadObserver {
        /**
         * This method is called whenever a queued element is finished playing.
         */
        void hasFinished(boolean success);

        void updateProgress(int progress);

        void hasError(String error);
    }

    /**
     * Constructor. Reads voice description from disk if available
     */
    public DownloadVoiceManager() throws IOException {
        mVoiceDownloadPath = new File(App.getDataPath()).getParent() + "/simaromur_voices";
        if (!FileUtils.mkdir(mVoiceDownloadPath)) {
            throw new IOException("Could not create voice download directory " + mVoiceDownloadPath);
        }
    }

    /**
     * Get the download path for all voices
     *
     * @return  the download path
     */
    public String getVoiceDownloadPath() {
        return mVoiceDownloadPath;
    }

    /**
     * Reads the voice description from disk and parses it.
     *
     * @param voiceDownloadPath path where the voice description is stored
     *
     * @return the parsed voice description
     *
     * @throws IOException if the voice description cannot be read
     * @note    don't call this from the main UI thread !
     */
    private DeviceVoices readVoiceDescriptionFromDisk(String voiceDownloadPath) throws IOException {
        DeviceVoices deviceVoices = null;
        Gson voiceDescriptionGson = new GsonBuilder().create();
        byte[] voiceDescriptionBuf = FileUtils.readFileFromStorage(voiceDownloadPath);
        if (voiceDescriptionBuf != null) {
            String voiceDescriptionJson = new String(voiceDescriptionBuf, StandardCharsets.UTF_8);
            deviceVoices = voiceDescriptionGson.fromJson(voiceDescriptionJson, DeviceVoices.class);
            Log.v(LOG_TAG, "readVoiceDescriptionFromDisk: " + deviceVoices.toString());
        } else {
            Log.v(LOG_TAG, "readVoiceDescriptionFromDisk: no voice description found");
        }
        return deviceVoices;
    }

    /**
     * Reads voice descriptions from all compatible voices from server. This downloads the voice
     * release info and returns a collection of DeviceVoice objects.
     *
     * @note    don't call this from the main thread !
     */
    private DeviceVoices readVoiceDescriptionFromServer() {
        final String voiceRepoUrl = "grammatek/simaromur_voices";
        if (!lazyInitVoiceRepo()) return null;
        // this is the latest downloaded voice release info
        ReleaseInfo releaseInfo = mVoiceRepo.getReleaseInfo(sReleaseName);

        // build list of voices from release info
        final String androidArch = SystemUtils.androidArchName();
        ArrayList<DeviceVoice> voiceList = new ArrayList<>();
        Log.v(LOG_TAG, "readVoiceDescriptionFromServer: searching for voice files for " + androidArch);
        for (VoiceInfo voiceInfo : releaseInfo.voices) {
            for (VoiceFile file : voiceInfo.files) {
                Log.v(LOG_TAG, "readVoiceDescriptionFromServer: " + file.toString());
                // only add voice files that match the Phone's architecture
                if (file.platform.equals(androidArch)
                        && voiceInfo.internalName.equals(sVoiceInternalName)) {
                    DeviceVoice voice = new DeviceVoice(voiceInfo, voiceRepoUrl, sReleaseName, androidArch);
                    voiceList.add(voice);
                }
            }
        }
        return new DeviceVoices("Voices from " + voiceRepoUrl
                + ":" + sReleaseName, voiceList);
    }

    /**
     * Lazy initialization of the voice repo. This is needed because the voice repo needs to be
     * initialized on a background thread.
     *
     * @return true if the voice repo is initialized or false in case of an error
     *
     * @note   don't call this from the main thread !
     */
    private boolean lazyInitVoiceRepo() {
        boolean rv = false;
        if (mVoiceRepo == null) {
            try {
                mVoiceRepo = new VoiceRepo("grammatek/simaromur_voices");
                rv = true;
            } catch (IOException | VoiceRepo.LimitExceededException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Could not access voice repository at " + "grammatek/simaromur_voices"
                        + ": " + e.getMessage());
            }
        } else {
            rv = true;
        }
        return rv;
    }

    /**
     * Checks all available voice descriptions. These can be either from disk or from server. The
     * server voice descriptions are cached on the filesystem and only loaded from the server, if
     * the cached file age exceeds a certain interval.
     *
     * @param includeServer if true, the server voice descriptions are included, otherwise only
     *                      the voice descriptions from disk are included. This is useful for
     *                      the case we need the current voice descriptions, but must not
     *                      access the network.
     *
     */
    public void readVoiceDescription(boolean includeServer) {
        final String voiceDescriptionDiskPath = mVoiceDownloadPath + "/voice-info.json";
        final String voiceDescriptionServerCachePath = mVoiceDownloadPath + "/voice-info-server-cache.json";
        try {
            mVoicesOnDisk = readVoiceDescriptionFromDisk(voiceDescriptionDiskPath);

            DeviceVoices voicesOnServer = readVoiceServerCacheIfNotExpired(voiceDescriptionServerCachePath, 60);
            if (voicesOnServer == null && includeServer) {
                voicesOnServer = readVoiceDescriptionFromServer();
                if (voicesOnServer != null) {
                    mVoicesOnServer = voicesOnServer;
                    if (! cacheVoiceDescriptionToDisk(voiceDescriptionServerCachePath, mVoicesOnServer)) {
                        Log.e(LOG_TAG, "Could not cache server voice description!");
                    }
                } else {
                    Log.e(LOG_TAG, "Could not read voice description from server!");
                }
            } else {
                mVoicesOnServer = voicesOnServer;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Could not read voice descriptions " + e.getMessage());
        }
    }

    /**
     * Checks if the voice description file on disk is still valid. If it is, it is read and returned.
     * If it is not, null is returned.
     *
     * @param voiceDescriptionServerCachePath path where the voice description is stored
     * @param cacheValidityInMinutes          validity of the cache in minutes
     *
     * @return the parsed voice description or null if the cache is not valid
     *
     * @throws IOException if the voice description cannot be read
     */
    private DeviceVoices readVoiceServerCacheIfNotExpired(String voiceDescriptionServerCachePath, long cacheValidityInMinutes) throws IOException {
        DeviceVoices voiceInfo = null;
        if (FileUtils.exists(voiceDescriptionServerCachePath)) {
            LocalDateTime modificationTime = FileUtils.getModificationDateTimeOfFile(voiceDescriptionServerCachePath);
            if (modificationTime.plusMinutes(cacheValidityInMinutes).isBefore(LocalDateTime.now())) {
                Log.v(LOG_TAG, "readVoiceDescription: server voice description cache is older than " + cacheValidityInMinutes
                        + " minutes, updating");
            } else {
                Log.v(LOG_TAG, "readVoiceDescription: server voice description cache is still valid, using it");
                voiceInfo = readVoiceDescriptionFromDisk(voiceDescriptionServerCachePath);
            }
        }
        return voiceInfo;
    }

    /**
     * Caches the voice description from server to disk
     *
     * @param filePath path where the voice description is stored
     *
     * @return true if the voice description was cached successfully
     */
    private boolean cacheVoiceDescriptionToDisk(String filePath, DeviceVoices voices) {
        // cache the voice description from server
        Gson voiceDescriptionGson = new GsonBuilder().create();
        String voiceDescriptionJson = voiceDescriptionGson.toJson(voices);
        return FileUtils.writeFileToStorage(filePath,
                voiceDescriptionJson.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Returns the combined voice information from disk and repository, if available
     *
     * @return  Voice information about all device voices, whether these are on disk or not yet
     *          downloaded or null if no voice information is available
     */
    public DeviceVoices getVoiceList() {
        // combine voice information from disk and repository
        if (mVoicesOnDisk != null && mVoicesOnServer != null) {
            return DeviceVoices.combine(mVoicesOnDisk, mVoicesOnServer);
        } else if (mVoicesOnDisk != null) {
            return mVoicesOnDisk;
        } else if (mVoicesOnServer != null) {
            return mVoicesOnServer;
        }
        return null;
    }

    /**
     * Converts Device voices to DB voice
     *
     * @return  Device voices as list of Db voices
     */
    public List<Voice> getVoiceDbList() {
        List<Voice> voices = new ArrayList<>();
        for (DeviceVoice aDevVoice : getVoiceList().Voices) {
            // filter platform compatible files from aDevVoice.Files
            List<DeviceVoiceFile> files = new ArrayList<>();
            for (DeviceVoiceFile aFile : aDevVoice.Files) {
                if (aFile.Platform.equals(SystemUtils.androidArchName())) {
                    files.add(aFile);
                }
            }
            aDevVoice.Files = files;
            Voice aVoice = aDevVoice.convertToDbVoice();
            if (aVoice != null) {
                voices.add(aVoice);
            }
        }
        return voices;
    }

    /**
     * Returns voice information for specified voice.
     *
     * @param internalName  Name of the voice
     *
     * @return  Voice information about specified voice
     */
    public DeviceVoice getInfoForVoice(String internalName) throws IOException {
        for (DeviceVoice voice:getVoiceList().Voices) {
            if (voice.InternalName.equals(internalName)) {
                return voice;
            }
        }
        throw new IOException("No such voice: " + internalName);
    }

    /**
     * Downloads a voice from the server and feed progress updates to the callback.
     *
     * @param voice        Voice to download
     * @param downloadObserver    Voice information
     */
    public void downloadVoiceAsync(Voice voice, DownloadObserver downloadObserver, VoiceDao voiceDao) {
        mAsyncThread = new AsyncThread() {
            final String anInternalName = voice.internalName;
            final DownloadObserver aDownloadObserver = downloadObserver;
            // progress listener is set when download starts
            ProgressListener listener = null;
            boolean voiceRepoOk = false;
            boolean downloadOk = false;

            // returns true if the voice has been downloaded successfully
            boolean isDownloadOk() {
                return voiceRepoOk && downloadOk;
            }

            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                voiceRepoOk = lazyInitVoiceRepo();
                if (!voiceRepoOk) {
                    Log.e(LOG_TAG, "Voice repository not available !");
                    return;
                }
                if (mVoicesOnServer == null) {
                    Log.e(LOG_TAG, "Server Voice description not available !");
                    return;
                }

                // Search the VoiceInfo inside mVoicesOnServer
                DeviceVoice voiceInfo = null;
                Log.v(LOG_TAG, "downloadVoiceAsync: checking given voice in mVoicesOnServer");
                for (DeviceVoice aVoiceInfo : mVoicesOnServer.Voices) {
                    Log.v(LOG_TAG, "" + aVoiceInfo);
                    if (aVoiceInfo.InternalName.equals(anInternalName) &&
                            aVoiceInfo.Version.equals(voice.version)) {
                        voiceInfo = aVoiceInfo;
                        break;
                    }
                }
                if (voiceInfo == null) {
                    Log.e(LOG_TAG, "Could not find voice info in repository for voice " + anInternalName);
                    return;
                }
                // get the voice file download url and return it in the member variable
                String voiceUrl = mVoiceRepo.getDownloadUrlForVoice(sReleaseName,
                        anInternalName, SystemUtils.androidArchName());
                assert(voiceUrl != null);

                final String tmpFolder = App.getContext().getCacheDir().getAbsolutePath();
                final String baseNameCompressedFile = voiceUrl.substring(voiceUrl.lastIndexOf('/') + 1);
                final String fileName = tmpFolder + "/" + baseNameCompressedFile;

                listener = new ProgressListener(fileName, aDownloadObserver);

                mCallDownloadVoice = mVoiceRepo.downloadVoiceFileAsync(sReleaseName, baseNameCompressedFile,
                        new ProgressObserver(listener, 1024*1024));

                waitForCompletion();

                if (listener.isComplete()) {
                    // download was successful, now extract the voice file
                    final String voiceFolder = App.getContext().getFilesDir().getAbsolutePath() + "/voices";
                    FileUtils.mkdir(voiceFolder);
                    final List<String> ignoreList = List.of("voice_driver.h",
                            "Makefile.aarch64-linux-android", "Makefile.armv7a-linux-androideabi",
                            "Makefile.i686-linux-android", "Makefile.x86_64-linux-android");
                    Decompress decompress = new Decompress(fileName,
                            App.getContext().getFilesDir().getPath() + "/voices", ignoreList);

                    if (!decompress.unzip()) {
                        Log.e(LOG_TAG, "Could not unzip voice file " + fileName);
                        FileUtils.delete(fileName);
                    } else {
                        // delete the downloaded zip file
                        Log.v(LOG_TAG, "Deleting downloaded voice file " + fileName);
                        FileUtils.delete(fileName);

                        // check MD5sum of the decompressed voice file
                        String decompressedFileName = decompress.getLastEntry();
                        Log.v(LOG_TAG, "Checking MD5sum of " + decompressedFileName);
                        String md5sum = FileUtils.getMD5SumOfFile(decompressedFileName);
                        if (md5sum == null ) {
                            Log.e(LOG_TAG, "No MD5sum available for voice file " + decompressedFileName);
                            // TODO delete unzipped files
                            return;
                        }
                        Log.v(LOG_TAG, "md5sum: " + md5sum);
                        boolean md5sumOk = false;
                        md5sumOk = compareMd5Sum(voiceInfo, md5sum);
                        if (!md5sumOk) {
                            Log.e(LOG_TAG, "MD5sum does not match any of the voice files");
                            // TODO delete unzipped files
                            return;
                        }
                        // update voice information
                        Log.v(LOG_TAG, "Updating voice information");
                        voiceInfo.Residence = "disk";
                        if (mVoicesOnDisk != null) {
                            // append the new voice to the list of voices on disk
                            mVoicesOnDisk.Voices.add(voiceInfo);
                        } else {
                            mVoicesOnDisk = new DeviceVoices("Voices of release "
                                    + sReleaseName, new ArrayList<>(List.of(voiceInfo)));
                        }
                        // save the voice information to disk
                        if (!cacheVoiceDescriptionToDisk(mVoiceDownloadPath + "/voice-info.json", mVoicesOnDisk)) {
                            Log.e(LOG_TAG, "Could not cache voice description to disk");
                        } else {
                            // update the voice information in the database
                            Voice dbVoice = voiceInfo.convertToDbVoice();
                            if (dbVoice != null) {
                                dbVoice.downloadPath = decompressedFileName;
                                dbVoice.md5Sum = md5sum;
                                dbVoice.size = decompress.getLastFileSize();
                                Voice existingVoice = voiceDao.findVoice(dbVoice.name, dbVoice.internalName, dbVoice.languageCode, dbVoice.languageName, dbVoice.variant);
                                if (existingVoice != null) {
                                    dbVoice.voiceId = existingVoice.voiceId;
                                    Log.v(LOG_TAG, "Updating voice information in database: " + dbVoice);
                                    voiceDao.updateVoices(dbVoice);
                                } else {
                                    Log.v(LOG_TAG, "Inserting voice information in database: " + dbVoice);
                                    voiceDao.insertVoice(dbVoice);
                                }
                                // TODO we need to update also the voice information in the
                                //  VoiceInfo object on disk

                                downloadOk = true;
                            } else {
                                Log.e(LOG_TAG, "Could not convert voice info to DB voice");
                            }
                        }
                    }
                }
            }

            /**
             * Wait for the download to complete
             */
            private void waitForCompletion() {
                while (!mCallDownloadVoice.isExecuted()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                assert(mCallDownloadVoice.isExecuted());
                while (!mCallDownloadVoice.isCanceled() && !listener.isComplete()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onPostExecute() {
                // reset spinner
                aDownloadObserver.hasFinished(isDownloadOk());
                if (!voiceRepoOk) {
                    Log.w(LOG_TAG, "Voice repository not available, probable rate limit exceeded");
                    Log.w(LOG_TAG, "Try again later");
                    // showTryAgainDialog();
                }
            }
        };
        mAsyncThread.execute("DownloadVoiceThread");
        // async execution, return immediately
    }

    private boolean compareMd5Sum(DeviceVoice voiceInfo, String md5sum) {
        boolean md5sumOk = false;
        for (DeviceVoiceFile voiceFile: voiceInfo.Files) {
            if (voiceFile.Md5Sum.equals(md5sum)) {
                Log.v(LOG_TAG, "MD5sum matches");
                md5sumOk = true;
                break;
            } else {
                Log.e(LOG_TAG, "MD5sum does not match");
            }
        }
        return md5sumOk;
    }

    /**
     * Cancels a currently ongoing download of a voice.
     */
    public void cancelCurrentDownload() {
        Log.v(LOG_TAG, "cancelCurrentDownload");
        if (mAsyncThread != null) {
            if (! mAsyncThread.isShutdown()) {
                Log.v(LOG_TAG, "cancelCurrentDownload: thread is shutdown");
                mAsyncThread.shutdown();
            }
        }
        if (mCallDownloadVoice != null) {
            Log.v(LOG_TAG, "Download: is shutdown");
            mCallDownloadVoice.cancel();
        }
    }

    // class ProgressListener tracks download progress for method downloadVoiceAsync()
    static class ProgressListener implements ProgressObserver.Listener {
        final static String LOG_TAG = "DownloadManager.ProgressListener";
        private final String mFileName;
        private FileOutputStream mFs = null;
        private long mFileSize = -1;
        private long mBytesDownloaded = 0;
        private final DownloadObserver mDownloadObserver;

        ProgressListener(String fileName, DownloadObserver finishedObserver) {
            mFileName = fileName;
            mDownloadObserver = finishedObserver;
        }

        @Override
        public void onStarted(long totalBytes) {
            Log.v(LOG_TAG, "onStarted: downloading " + totalBytes + " ...");
            mBytesDownloaded = 0;
            mFileSize = totalBytes;
            File aFile = new File(mFileName);
            try {
                mFs = new FileOutputStream(aFile, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            Log.v(LOG_TAG, "onStarted: downloading overall" + mFileSize + " bytes ...");
        }

        @Override
        public boolean onProgress(byte[] buffer, long numBytes) {
            mBytesDownloaded += numBytes;
            Log.v(LOG_TAG, "onProgress: " + mBytesDownloaded + " of " + mFileSize + " downloaded");

            if (mBytesDownloaded > mFileSize) {
                Log.e(LOG_TAG,"onProgress: " + mBytesDownloaded + " > " + mFileSize + " !");
                return false;
            }
            try {
                mFs.write(buffer, 0, (int) numBytes);
                int percent = (int) (100 * mBytesDownloaded / mFileSize);
                Log.v(LOG_TAG, "onProgress: " + percent + "%");
                mDownloadObserver.updateProgress(percent);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            return true;
        }

        @Override
        public void onError(String error, int errorCode) {
            Log.v(LOG_TAG,"onError: " + error + " (" + errorCode + ")");
            mDownloadObserver.hasError(error + " (" + errorCode + ")");
        }

        public boolean isComplete() {
            return mBytesDownloaded == mFileSize;
        }
    }
}
