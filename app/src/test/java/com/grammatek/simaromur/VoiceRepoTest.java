package com.grammatek.simaromur;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.grammatek.simaromur.network.remoteasset.ProgressObserver;
import com.grammatek.simaromur.network.remoteasset.ReleaseInfo;
import com.grammatek.simaromur.network.remoteasset.VoiceFile;
import com.grammatek.simaromur.network.remoteasset.VoiceInfo;
import com.grammatek.simaromur.network.remoteasset.VoiceRepo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import okhttp3.Call;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class VoiceRepoTest {
    private final static String LOG_TAG = "Símarómur_Test_" + VoiceRepoTest.class.getSimpleName();
    private final static String mVoiceRepoUrl = "grammatek/simaromur_voices";
    private final String mReleaseInfo;
    private final Context mContext;

    /**
     * Test construction
     */
    public VoiceRepoTest() {
        mContext = ApplicationProvider.getApplicationContext();
        mReleaseInfo = getReleaseInfo();
        assert(mReleaseInfo != null);
    }

    /**
     * Return voice info from test resource file.
     *
     * @return  String containing voice info or null if not found.
     */
    private String getReleaseInfo() {
        String releaseInfo = null;

        try {
            // load release info from test resource file
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("voice_info.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            int nRead = is.read(buffer);
            if (nRead != size) {
                Log.e(LOG_TAG, "getReleaseInfo(): read " + nRead + " bytes, expected " + size);
            }
            is.close();
            releaseInfo = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            Log.e(LOG_TAG, "getReleaseInfo(): ", e);
        }
        return releaseInfo;
    }

    @Test
    public void Test_convertReleaseInfoToGson() {
        try {
            ReleaseInfo releaseInfo = new Gson().fromJson(mReleaseInfo, ReleaseInfo.class);
            assert(releaseInfo != null);
            assert(releaseInfo.voices.size() > 0);
            assert(releaseInfo.voices.get(0).files != null);
            assert(releaseInfo.voices.get(0).files.size() != 0);
        } catch (JsonSyntaxException e) {
            Log.e(LOG_TAG, "Test_convertReleaseInfoToGson: ", e);
            assert(false);
        }
    }

    @Test
    public void Test_readReleaseInfoFromGithub() {
        VoiceRepo voiceRepo = null;
        try {
            voiceRepo = new VoiceRepo(mVoiceRepoUrl);
        } catch (IOException | VoiceRepo.LimitExceededException e) {
            e.printStackTrace();
            assert(false);
        }
        final ArrayList<String> releases = voiceRepo.queryReleases();
        assert(releases != null);
        assert(releases.size() > 0);
        for (String releaseName : releases) {
            System.out.println("Test_readReleaseInfoFromGithub: release: " + releaseName);
            ReleaseInfo releaseInfo = voiceRepo.getReleaseInfo(releaseName);
            assert(releaseInfo != null);
            assert(releaseInfo.voices.size() > 0);
            assert(releaseInfo.voices.get(0).files.size() > 0);
        }
    }

    @Test
    public void Test_specificVoiceAndPlatformFromSpecificRelease() {
        final String voiceInternalName = "Alfur_flite";
        final String releaseName = "0.1 test release";
        final ArrayList<String> platforms = new ArrayList<>();
        platforms.add("aarch64");
        // it is sufficient to test one platform, since the release info is the same for all platforms
        // platforms.add("armv7a");
        // platforms.add("x86_64");
        // platforms.add("i686");

        VoiceRepo voiceRepo = null;
        try {
            voiceRepo = new VoiceRepo(mVoiceRepoUrl);
        } catch (IOException | VoiceRepo.LimitExceededException e) {
            e.printStackTrace();
            assert(false);
        }
        System.out.println("Test_specificVoiceFromSpecificRelease: release: " + releaseName);
        ReleaseInfo releaseInfo = voiceRepo.getReleaseInfo(releaseName);
        assert(releaseInfo != null);
        assert(releaseInfo.voices.size() > 0);
        assert(releaseInfo.voices.get(0).files.size() > 0);

        for (String platform:platforms) {
            String compressedFileUrl = voiceRepo.getDownloadUrlForVoice(releaseName, voiceInternalName, platform);
            assert(compressedFileUrl != null);
            assert(compressedFileUrl.length() > 0);
        }
    }

    @Test
    public void Test_downloadAllVoicesFromRelease() {
        // @note this test can only be run with a valid github token, as we will hit the rate limit
        //       otherwise
        VoiceRepo voiceRepo = null;
        try {
            voiceRepo = new VoiceRepo(mVoiceRepoUrl);
        } catch (IOException | VoiceRepo.LimitExceededException e) {
            e.printStackTrace();
            assert(false);
        }
        final ArrayList<String> releases = voiceRepo.queryReleases();
        assert(releases != null);
        assert(releases.size() > 0);

        final String tmpFolder = mContext.getCacheDir().getAbsolutePath();
        for (String releaseName : releases) {
            System.out.println("Test_downloadVoicesFromRelease: release: " + releaseName);
            ReleaseInfo releaseInfo = voiceRepo.getReleaseInfo(releaseName);
            assert(releaseInfo != null);
            assert(releaseInfo.voices.size() > 0);
            assert(releaseInfo.voices.get(0).files.size() > 0);
            for (VoiceInfo voiceInfo : releaseInfo.voices) {
                for (VoiceFile file : voiceInfo.files) {
                    assert(file.compressedFile.endsWith(".zip"));
                    final String fileName = tmpFolder + "/" + file.compressedFile;
                    assert (voiceRepo.downloadVoiceFile(releaseName, file.compressedFile, fileName));
                    assert (new java.io.File(fileName).exists());
                }
            }
        }
    }

    static class ProgressListener implements ProgressObserver.Listener {
        private final String mFileName;
        private File mFile = null;
        private FileOutputStream mFs = null;
        private long mFileSize = -1;
        private long mBytesDownloaded = 0;
        ProgressListener(String fileName) {
            mFileName = fileName;
        }

        @Override
        public void onStarted(long totalBytes) {
            System.out.println("onStarted: transferring " + totalBytes + " ...");
            mBytesDownloaded = 0;
            mFileSize = totalBytes;
            mFile = new File(mFileName);
            try {
                mFs = new FileOutputStream(mFile, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean onProgress(byte[] buffer, long numBytes) {
            System.out.print(".");
            mBytesDownloaded += numBytes;
            if (mBytesDownloaded > mFileSize) {
                System.out.println("onProgress: " + mBytesDownloaded + " > " + mFileSize);
                return false;
            }
            try {
                mFs.write(buffer, 0, (int) numBytes);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            return true;
        }

        @Override
        public void onError(String error, int errorCode) {
            System.out.println("onError: " + error + " (" + errorCode + ")");
            assert(false);
        }

        public boolean isComplete() {
            return mBytesDownloaded == mFileSize;
        }
    }

    @Test
    public void Test_downloadSpecificVoiceReleasePlatformFile() {
        VoiceRepo voiceRepo = null;
        try {
            voiceRepo = new VoiceRepo(mVoiceRepoUrl);
        } catch (IOException | VoiceRepo.LimitExceededException e) {
            e.printStackTrace();
            assert(false);
        }

        final String voiceInternalName = "Alfur_flite";
        final String releaseName = "0.1 test release";
        final String platform = "aarch64";

        String compressedFileUrl = voiceRepo.getDownloadUrlForVoice(releaseName, voiceInternalName, platform);
        assert(compressedFileUrl != null);
        final String tmpFolder = mContext.getCacheDir().getAbsolutePath();
        final String baseNameCompressedFile = compressedFileUrl.substring(compressedFileUrl.lastIndexOf('/') + 1);
        final String fileName = tmpFolder + "/" + baseNameCompressedFile;

        ProgressListener listener = new ProgressListener(fileName);
        Call call = voiceRepo.downloadVoiceFileAsync(releaseName, baseNameCompressedFile, new ProgressObserver(listener, 1024*1024));
        assert(call != null);

        while (!call.isExecuted()) {
            try {
                Thread.sleep(1000);
                System.out.println("Test_downloadSpecificVoiceReleasePlatformFile: waiting for download to complete");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assert(call.isExecuted());
        while (!call.isCanceled() && !listener.isComplete()) {
            try {
                Thread.sleep(1000);
                System.out.println("Test_downloadSpecificVoiceReleasePlatformFile: waiting for download to complete");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assert(listener.isComplete());
    }
}
