package com.grammatek.simaromur.device;

import android.util.Log;

import androidx.annotation.Nullable;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final String mVoiceDescriptionServerCachePath;
    // TODO: this is hardcoded for now, but should be dynamic in the future, when we implement
    //       the update mechanism
    private static final String sReleaseName = "0.2 release";
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
    public interface Observer {
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
        mVoiceDescriptionServerCachePath = mVoiceDownloadPath + "/voice-info-server-cache.json";
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
     * Deletes the voice information from disk.
     *
     * @param voiceInfo the voice information to delete
     * @return true if the voice information was deleted successfully, false otherwise
     */
    private boolean deleteVoiceDescriptionFromDisk(DeviceVoice voiceInfo) {
        Log.v(LOG_TAG, "deleteVoiceDescriptionFromDisk(" + voiceInfo.Name + ")");
        boolean rv = false;
        if (mVoicesOnDisk == null || mVoicesOnDisk.Voices == null) {
            Log.e(LOG_TAG, "deleteVoiceDescriptionFromDisk: no voice description found");
            return false;
        }
        mVoicesOnDisk.Voices.remove(voiceInfo);
        if (cacheVoiceDescriptionToDisk(mVoiceDownloadPath + "/voice-info.json", mVoicesOnDisk)) {
            rv = true;
        } else {
            Log.e(LOG_TAG, "Could not cache voice description to disk");
        }
        return rv;
    }

    /**
     * Deletes the cached server voice description from disk.
     *
     * @return true if the cached server voice description was deleted successfully, false otherwise.
     */
    private boolean deleteCachedServerVoiceDescriptionFromDisk() {
        Log.v(LOG_TAG, "deleteCachedServerVoiceDescriptionFromDisk()");
        boolean rv = false;
        if (FileUtils.delete(mVoiceDescriptionServerCachePath)) {
            rv = true;
        } else {
            Log.e(LOG_TAG, "Could not delete cached voice server description from disk");
        }
        mVoicesOnServer = null;
        return rv;
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
                // TODO: hack ! We only support onnx files for now
                if (! file.type.equals("onnx")) continue;

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
                Log.v(LOG_TAG, "lazyInitVoiceRepo: initializing voice repo");
                mVoiceRepo = new VoiceRepo("grammatek/simaromur_voices");
                rv = true;
            } catch (IOException | VoiceRepo.LimitExceededException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Could not access voice repository at " + "grammatek/simaromur_voices"
                        + ": " + e.getMessage());
            }
        } else {
            Log.v(LOG_TAG, "lazyInitVoiceRepo: already initialized");
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
    synchronized
    public void readVoiceDescription(boolean includeServer) {
        Log.v(LOG_TAG, "readVoiceDescription(" + includeServer + ")");
        final String voiceDescriptionDiskPath = mVoiceDownloadPath + "/voice-info.json";

        try {
            mVoicesOnDisk = readVoiceDescriptionFromDisk(voiceDescriptionDiskPath);

            DeviceVoices voicesOnServer = readVoiceServerCacheIfNotExpired(mVoiceDescriptionServerCachePath, 60);
            if (voicesOnServer == null && includeServer) {
                voicesOnServer = readVoiceDescriptionFromServer();
                if (voicesOnServer != null) {
                    mVoicesOnServer = voicesOnServer;
                    if (! cacheVoiceDescriptionToDisk(mVoiceDescriptionServerCachePath, mVoicesOnServer)) {
                        Log.e(LOG_TAG, "Could not cache server voice description!");
                    }
                } else {
                    Log.e(LOG_TAG, "Could not read voice description from server!");
                }
            } else if (!includeServer && voicesOnServer == null) {
                // indirect recursion: we need to update the voice description from the server
                App.getAppRepository().triggerServerVoiceUpdate();
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
                // iterate over all voices from voiceInfo and test the release name against the
                // current release name. If it does not match, we need to update the voice
                for (DeviceVoice voice : voiceInfo.Voices) {
                    if (!voice.Release.equals(sReleaseName)) {
                        Log.v(LOG_TAG, "readVoiceDescription: server voice description cache is outdated, updating");
                        voiceInfo = null;
                        break;
                    }
                }
            }
        }
        return voiceInfo;
    }

    /**
     * Caches the given voice description to disk. The list of voices is made unique before
     * being written out.
     * If the file already exists, it is overwritten.
     *
     * @param filePath path where the voice description is stored
     *
     * @return true if the voice description was cached successfully.
     *         If the file cannot be written, false is returned.
     */
    private boolean cacheVoiceDescriptionToDisk(String filePath, DeviceVoices voices) {
        // make voices unique
        Log.v(LOG_TAG, "cacheVoiceDescriptionToDisk(" + filePath + ")");
        final Set<DeviceVoice> uniqueVoiceSet = new HashSet<>(voices.Voices);
        voices.Voices = new ArrayList<>(uniqueVoiceSet);
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
    synchronized
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
    synchronized
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
                Log.v(LOG_TAG, "getVoiceDbList: adding device voice " + aVoice.name + " " + aVoice.version);
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
    synchronized
    public DeviceVoice getInfoForVoice(String internalName) throws IOException {
        List<DeviceVoice> voices = getVoiceList().Voices;
        if (voices == null) {
            throw new IOException("No voices available");
        }
        for (DeviceVoice voice:voices) {
            if (voice.InternalName.equals(internalName)) {
                return voice;
            }
        }
        throw new IOException("No such voice: " + internalName);
    }

    /**
     * Delete voice from disk and updates db. Voice needs to be a voice managed by DownloadVoiceManager.
     * @param voice                 Voice to delete
     * @param deleteVoiceObserver   Observer to notify when voice is deleted
     * @param voiceDao  Dao for voice table
     */
    synchronized
    public void deleteVoice(Voice voice, com.grammatek.simaromur.VoiceInfo.DeleteVoiceObserver deleteVoiceObserver,
                            VoiceDao voiceDao) {
        Log.v(LOG_TAG, "deleteVoice(" + voice.internalName + ") ...");
        mAsyncThread = new AsyncThread() {
            final String anInternalName = voice.internalName;
            boolean voiceRepoOk = false;

            @Override
            public void onPreExecute() {
                Log.v(LOG_TAG, "UninstallVoiceAsync: starting uninstallation of " + anInternalName);
            }

            @Override
            public void doInBackground() {
                deleteVoiceObserver.updateProgress(0);      // show spinner
                voiceRepoOk = lazyInitVoiceRepo();
                if (!voiceRepoOk) {
                    Log.e(LOG_TAG, "Voice repository not available !");
                    deleteVoiceObserver.hasError("Voice repository not available !");
                    deleteVoiceObserver.hasFinished(false);
                    return;
                }
                DeviceVoice voiceInfo = searchVoiceInfo(anInternalName, mVoicesOnDisk);
                if (voiceInfo == null) {
                    final String errMsg = "deleteVoice: voiceInfo is null for voice " + anInternalName;
                    Log.e(LOG_TAG, errMsg);
                    deleteVoiceObserver.hasError(errMsg);
                    deleteVoiceObserver.hasFinished(false);
                    return;
                }

                // remove voice from list of voices on disk
                if (!deleteVoiceDescriptionFromDisk(voiceInfo)) {
                    final String errMsg = "deleteVoice: could not delete voice description from disk for voice " + anInternalName;
                    Log.e(LOG_TAG, errMsg);
                    deleteVoiceObserver.hasError(errMsg);
                    deleteVoiceObserver.hasFinished(false);
                    return;
                }
                // this causes an update of related meta data
                if (!deleteCachedServerVoiceDescriptionFromDisk()) {
                    final String errMsg = "deleteVoice: could not delete voice server description from disk";
                    Log.e(LOG_TAG, errMsg);
                    deleteVoiceObserver.hasError(errMsg);
                    deleteVoiceObserver.hasFinished(false);
                    return;
                }

                boolean delSuccess = FileUtils.delete(voice.downloadPath);
                if (delSuccess) {
                    Log.v(LOG_TAG, "deleteVoice: deleted " + voice.downloadPath);
                } else {
                    final String errMsg = "Failed to delete " + voice.downloadPath;
                    Log.e(LOG_TAG, "deleteVoice: " + errMsg);
                    deleteVoiceObserver.hasError(errMsg);
                    deleteVoiceObserver.hasFinished(false);
                    return;
                }
                voiceDao.deleteVoices(voice);
                // notify observer
                deleteVoiceObserver.hasFinished(true);
            }

            @Override
            public void onPostExecute() {
                Log.v(LOG_TAG, "voice deletion complete");
            }

            @Nullable
            private DeviceVoice searchVoiceInfo(String internalVoiceName, DeviceVoices voicesOnDisk) {
                // Search the VoiceInfo inside mVoicesOnServer
                DeviceVoice voiceInfo = null;
                if (voicesOnDisk == null || voicesOnDisk.Voices == null) {
                    Log.e(LOG_TAG, "deleteVoice: no voices on Disk ?");
                    return null;
                }
                for (DeviceVoice aVoiceInfo : voicesOnDisk.Voices) {
                    Log.v(LOG_TAG, "" + aVoiceInfo);
                    if (aVoiceInfo.InternalName.equals(internalVoiceName) &&
                            aVoiceInfo.Version.equals(voice.version)) {
                        voiceInfo = aVoiceInfo;
                        break;
                    }
                }
                if (voiceInfo == null) {
                    Log.e(LOG_TAG, "Could not find voice info on disk for voice "
                            + internalVoiceName);
                    return null;
                }
                return voiceInfo;
            }
        };

        mAsyncThread.execute("DeleteVoiceThread");
    }

    /**
     * Downloads a voice from the server and feed progress updates to the callback.
     *
     * @param voice        Voice to download
     * @param downloadObserver    Voice information
     * @param voiceDao    VoiceDao to update the voice information
     */
    public void downloadVoiceAsync(Voice voice, Observer downloadObserver, VoiceDao voiceDao) {
        mAsyncThread = new AsyncThread() {
            final String anInternalName = voice.internalName;
            final Observer aDownloadObserver = downloadObserver;
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
                Log.v(LOG_TAG, "downloadVoiceAsync: starting download of " + anInternalName);
            }

            @Override
            public void doInBackground() {
                Log.v(LOG_TAG, "downloadVoiceAsync: downloading voice " + anInternalName);
                voiceRepoOk = lazyInitVoiceRepo();
                if (!voiceRepoOk) {
                    final String msg = "Voice repository not available !";
                    Log.e(LOG_TAG, msg);
                    aDownloadObserver.hasError(msg);
                    aDownloadObserver.hasFinished(false);
                    return;
                }
                if (mVoicesOnServer == null) {
                    final String msg = "Server Voice description not available !";
                    Log.e(LOG_TAG, msg);
                    aDownloadObserver.hasError(msg);
                    aDownloadObserver.hasFinished(false);
                    return;
                }

                DeviceVoice voiceInfo = searchVoiceInfo(anInternalName, mVoicesOnServer);
                if (voiceInfo == null) {
                    final String msg = "Voice description not available inside server voice description ?!";
                    Log.e(LOG_TAG, msg);
                    aDownloadObserver.hasError(msg);
                    aDownloadObserver.hasFinished(false);
                    return;
                }

                // get the voice file download url
                String voiceUrl = mVoiceRepo.getDownloadUrlForVoice(sReleaseName,
                        anInternalName, SystemUtils.androidArchName());
                if (voiceUrl == null) {
                    final String msg = "downloadVoiceAsync: no download url for voice " + anInternalName;
                    Log.e(LOG_TAG, msg);
                    aDownloadObserver.hasError(msg);
                    aDownloadObserver.hasFinished(false);
                    return;
                }

                final String tmpFolder = App.getContext().getCacheDir().getAbsolutePath();
                final String baseNameCompressedFile = voiceUrl.substring(voiceUrl.lastIndexOf('/') + 1);
                final String fileName = tmpFolder + "/" + baseNameCompressedFile;
                FileUtils.delete(fileName);

                CleanupStack downloadedFiles = new CleanupStack();
                downloadedFiles.addFile(fileName);

                // start download
                listener = new ProgressListener(fileName, aDownloadObserver);
                if (mAsyncThread.isShutdown()) {
                    final String msg = "downloadVoiceAsync: download of " + anInternalName + " cancelled";
                    Log.v(LOG_TAG, msg);
                    aDownloadObserver.hasFinished(true);
                    return;
                }
                mCallDownloadVoice = mVoiceRepo.downloadVoiceFileAsync(sReleaseName, baseNameCompressedFile,
                        new ProgressObserver(listener, 1024*1024));
                if (mCallDownloadVoice == null) {
                    final String msg = "downloadVoiceAsync: download of " + anInternalName + " failed";
                    Log.e(LOG_TAG, msg);
                    aDownloadObserver.hasError(msg);
                    aDownloadObserver.hasFinished(false);
                    return;
                }
                // 10 Minutes timeout, this should suffice even for slow internet connections
                if (!waitForCompletion(10*60)) {
                    final String msg = "Download of voice file " + fileName + " failed";
                    Log.w(LOG_TAG, msg);
                    downloadedFiles.cleanup();
                    aDownloadObserver.hasFinished(false);
                    return;
                }

                // download successful, extract voice files
                final String voiceFolder = App.getContext().getFilesDir().getAbsolutePath() + "/voices";
                FileUtils.mkdir(voiceFolder);
                final List<String> ignoreList = List.of("voice_driver.h",
                        "Makefile.aarch64-linux-android", "Makefile.armv7a-linux-androideabi",
                        "Makefile.i686-linux-android", "Makefile.x86_64-linux-android");
                Decompress decompress = new Decompress(fileName,
                        App.getContext().getFilesDir().getPath() + "/voices", ignoreList);

                CleanupStack decompressedFiles = new CleanupStack(decompress.getDestinationPathEntries());
                if (mAsyncThread.isShutdown()) {
                    final String msg = "unzipping: cancelled";
                    Log.v(LOG_TAG, msg);
                    downloadedFiles.cleanup();
                    aDownloadObserver.hasFinished(true);
                    return;
                }
                if (!decompress.unzip()) {
                    final String msg = "Could not unzip voice file " + fileName;
                    Log.e(LOG_TAG, msg);
                    downloadedFiles.cleanup();
                    aDownloadObserver.hasError(msg);
                    aDownloadObserver.hasFinished(false);
                } else {
                    if (mAsyncThread.isShutdown()) {
                        Log.v(LOG_TAG, "md5 sum checking cancelled");
                        return;
                    }
                    try {
                        String decompressedFileName = decompress.getLastEntry();
                        String md5sum = getMd5sum(voiceInfo, decompressedFileName);
                        if (md5sum == null) return;

                        // update voice information
                        Log.v(LOG_TAG, "Updating voice information");
                        voiceInfo.Residence = "disk";
                        if (persistVoiceDescription(voiceInfo)) {
                            // update the voice information in the database
                            Voice dbVoice = voiceInfo.convertToDbVoice();
                            if (dbVoice != null) {
                                dbVoice.downloadPath = decompressedFileName;
                                dbVoice.md5Sum = md5sum;
                                dbVoice.size = decompress.getLastFileSize();
                                Voice existingVoice = voiceDao.findVoice(dbVoice.name, dbVoice.internalName, dbVoice.languageCode, dbVoice.languageName, dbVoice.variant, voice.version);
                                if (existingVoice != null) {
                                    dbVoice.voiceId = existingVoice.voiceId;
                                    Log.v(LOG_TAG, "Updating voice information in database: " + dbVoice);
                                    voiceDao.updateVoices(dbVoice);
                                } else {
                                    Log.v(LOG_TAG, "Inserting voice information in database: " + dbVoice);
                                    voiceDao.insertVoice(dbVoice);
                                }
                                if (mAsyncThread.isShutdown()) {
                                    Log.v(LOG_TAG, "voice registration cancelled");
                                    return;
                                }
                                downloadOk = true;
                            } else {
                                Log.e(LOG_TAG, "Could not convert voice info to DB voice");
                            }
                        } else {
                            Log.e(LOG_TAG, "Could not persist voice info");
                        }
                    } finally {
                        if (!downloadOk) {
                            decompressedFiles.cleanup();
                        }
                        downloadedFiles.cleanup();
                    }
                }
            }

            /**
             * Saves the voice information to disk.
             * This method is a no-op in case it's already present.
             *
             * @param voiceInfo the voice information to save
             * @return true if the voice information was saved successfully, false otherwise
             */
            private boolean persistVoiceDescription(DeviceVoice voiceInfo) {
                boolean rv = false;
                if (mVoicesOnDisk == null) {
                    // lazy init the voice info on disk
                    mVoicesOnDisk = new DeviceVoices("On-device voices"
                            , new ArrayList<>(List.of(voiceInfo)));
                }

                // Check if the voice is already in the list
                if (!isVoiceInfoAlreadyCached(voiceInfo)) {
                    // append the new voice to the list of voices on disk
                    mVoicesOnDisk.Voices.add(voiceInfo);
                }
                if (cacheVoiceDescriptionToDisk(mVoiceDownloadPath + "/voice-info.json", mVoicesOnDisk)) {
                    rv = true;
                } else {
                    Log.e(LOG_TAG, "Could not cache voice description to disk");
                }
                return rv;
            }

            /**
             * Check if the voice is already in the list of voices on disk
             *
             * @param voiceInfo the voice to check
             *
             * @return true if the voice is already in the list, false otherwise
             */
            private boolean isVoiceInfoAlreadyCached(DeviceVoice voiceInfo) {
                boolean found = false;
                for (DeviceVoice aVoiceInfo : mVoicesOnDisk.Voices) {
                    if (aVoiceInfo.InternalName.equals(anInternalName) &&
                            aVoiceInfo.Version.equals(voiceInfo.Version)) {
                        found = true;
                        break;
                    }
                }
                return found;
            }

            /**
             * Search given internal voice name in given device voice list
             *
             * @param internalVoiceName the internal voice name to search for
             * @param devVoices the list of device voices
             * @return  the device voice or null if not found
             */
            @Nullable
            private DeviceVoice searchVoiceInfo(String internalVoiceName, DeviceVoices devVoices) {
                DeviceVoice voiceInfo = null;
                Log.v(LOG_TAG, "downloadVoiceAsync: checking given voice in mVoicesOnServer");
                for (DeviceVoice aVoiceInfo : devVoices.Voices) {
                    Log.v(LOG_TAG, "" + aVoiceInfo);
                    if (aVoiceInfo.InternalName.equals(internalVoiceName) &&
                            aVoiceInfo.Version.equals(voice.version)) {
                        voiceInfo = aVoiceInfo;
                        break;
                    }
                }
                if (voiceInfo == null) {
                    Log.e(LOG_TAG, "Could not find voice info in given repository for voice "
                            + internalVoiceName);
                    return null;
                }
                return voiceInfo;
            }

            /**
             * Wait for the download to complete
             */
            private boolean waitForCompletion(int timeoutInSecs) {
                // measure current time and loop until timeout reached
                long startTime = System.currentTimeMillis();
                long currentTime = startTime;

                while (!mCallDownloadVoice.isExecuted() && (currentTime - startTime < timeoutInSecs * 1000L)) {
                    try {
                        Thread.sleep(100);
                        if (mAsyncThread.isShutdown()) {
                            Log.v(LOG_TAG, "waitForCompletion: cancelled");
                            return false;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    currentTime = System.currentTimeMillis();
                }
                if (!mCallDownloadVoice.isExecuted()) {
                    Log.e(LOG_TAG, "Download of voice file failed");
                    return false;
                }
                while (!mCallDownloadVoice.isCanceled() && !listener.isComplete() &&
                        (currentTime - startTime < timeoutInSecs * 1000L)) {
                    try {
                        Thread.sleep(100);
                        if (mAsyncThread.isShutdown()) {
                            Log.v(LOG_TAG, "waitForCompletion: cancelled");
                            return false;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    currentTime = System.currentTimeMillis();
                }
                return listener.isComplete();
            }

            @Override
            public void onPostExecute() {
                if (!voiceRepoOk) {
                    Log.w(LOG_TAG, "Voice repository not available, probable rate limit exceeded");
                    Log.w(LOG_TAG, "Try again later");
                    // showTryAgainDialog();
                }
                // reset spinner
                aDownloadObserver.hasFinished(isDownloadOk());
            }
        };
        mAsyncThread.execute("DownloadVoiceThread");
        // async execution, return immediately
    }

    @Nullable
    private String getMd5sum(DeviceVoice voiceInfo, String decompressedFileName) {
        Log.v(LOG_TAG, "Checking MD5sum of " + decompressedFileName);
        String md5sum = FileUtils.getMD5SumOfFile(decompressedFileName);
        if (md5sum == null ) {
            Log.e(LOG_TAG, "No MD5sum available for voice file " + decompressedFileName);
            return null;
        }
        Log.v(LOG_TAG, "md5sum: " + md5sum);
        boolean md5sumOk = compareMd5Sum(voiceInfo, md5sum);
        if (!md5sumOk) {
            Log.e(LOG_TAG, "MD5sum does not match any of the voice files");
            return null;
        }
        return md5sum;
    }

    private boolean compareMd5Sum(DeviceVoice voiceInfo, String md5sum) {
        boolean md5sumOk = false;
        // this really only tests the md5sum of one of the voice files,
        // TODO: if there are more than one voice files, we should test all of them
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
        private final Observer mDownloadObserver;
        private int mProgressPercent = 0;

        ProgressListener(String fileName, Observer finishedObserver) {
            mFileName = fileName;
            mDownloadObserver = finishedObserver;
        }

        @Override
        public void onStarted(long totalBytes) {
            Log.v(LOG_TAG, "onStarted: downloading " + mFileName + "(" + totalBytes + " bytes) ...");
            mBytesDownloaded = 0;
            mProgressPercent = 0;
            mFileSize = totalBytes;
            File aFile = new File(mFileName);
            try {
                mFs = new FileOutputStream(aFile, false);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            Log.v(LOG_TAG, "onStarted: downloading overall " + mFileSize + " bytes ...");
        }

        @Override
        public boolean onProgress(byte[] buffer, long numBytes) {
            mBytesDownloaded += numBytes;
            int percent = (int) (100 * mBytesDownloaded / mFileSize);
            if (percent % 10 == 0) {
                if (mProgressPercent != percent) {
                    Log.v(LOG_TAG, "onProgress: " + mBytesDownloaded + " of " + mFileSize + " downloaded");
                    Log.v(LOG_TAG, "onProgress: " + percent + "%");
                    mProgressPercent = percent;
                }
            }

            if (mBytesDownloaded > mFileSize) {
                Log.e(LOG_TAG,"onProgress: " + mBytesDownloaded + " > " + mFileSize + " !");
                return false;
            }
            try {
                mFs.write(buffer, 0, (int) numBytes);
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

    /**
     * This class is used for cleaning up files that are not needed anymore.
     */
    static class CleanupStack {
        static final String LOG_TAG = "DownloadManager.CleanupStack";
        final List<String> mFiles = new ArrayList<>();

        /**
         * Constructor
         */
        public CleanupStack() {
        }

        /**
         * Constructor for multiple files
         * @param files     list of files
         */
        public CleanupStack(ArrayList<String> files) {
            addFiles(files);
        }

        /**
         * Adds a file to the cleanup stack.
         * @param fileName  The file to be added.
         */
        public void addFile(String fileName) {
            mFiles.add(fileName);
        }

        /**
         * Add given list of files to the cleanup stack.
         *
         * @param fileNames     The list of files to be added.
         */
        public void addFiles(ArrayList<String> fileNames) {
            mFiles.addAll(fileNames);
        }

        /**
         * Remove all files from the cleanup stack.
         */
        public void cleanup() {
            for (String fileName: mFiles) {
                Log.v(LOG_TAG, "Cleanup: " + fileName);
                FileUtils.delete(fileName);
            }
        }

    }
}
