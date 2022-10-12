package com.grammatek.simaromur.network.remoteasset;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.grammatek.simaromur.App;

import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRateLimit;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * This class represents a remote voice repository. It is used to download and cache remote voice
 * data & metadata.
 */
public class VoiceRepo {
    private final static String LOG_TAG = "Simaromur_" + VoiceRepo.class.getSimpleName();
    private final String mAssetRepoUrl;
    private final GitHub mGithub;
    private final GHRepository mRepo;
    private final OkHttpClient mHttpClient = new OkHttpClient();
    // mind off: with underscore, we also have a voice-info.json in the assets folder with a minus (!)
    private final String VOICE_INFO_FILE = "voice_info.json";

    // This exception is thrown when we have exceed our rate limit at GitHub
    public static class LimitExceededException extends Exception {
        public LimitExceededException() {
            super();
        }
        public LimitExceededException(String message) {
            super(message);
        }
    }

    /**
     * Initialize the asset repository and connects to the GitHub API.
     *
     * @param assetRepoUrl  The remote URL to the asset repository. This is a Github repository
     *                      and need to be in the format "owner/repo".
     */
    public VoiceRepo(String assetRepoUrl) throws IOException, LimitExceededException {
        mAssetRepoUrl = assetRepoUrl;
        // your personal access token is required to avoid rate limiting (60 requests per hour)
        final String oAuthToken = App.getAppRepository().getAssetConfigValueFor("github_auth0_token");
        if (oAuthToken.isEmpty()) {
            mGithub = GitHub.connectAnonymously();
        } else {
            // this sets the rate limit to 5000 requests per hour
            mGithub = GitHub.connectUsingOAuth(oAuthToken);
        }
        GHRateLimit rateLimit = mGithub.getRateLimit();
        Log.v(LOG_TAG, "GitHub rate limit: remaining " + rateLimit.getRemaining() + " accesses, reset at " + rateLimit.getResetDate());
        if (rateLimit.getRemaining() == 0) {
            throw(new LimitExceededException("Github rate limit exceeded!"));
        }
        mRepo = mGithub.getRepository(mAssetRepoUrl);
    }

    /**
     * Get the list of available releases. The returned values correspond to the release names
     * used on the remote repository. I.e. these don't need to follow a particular naming
     * convention.
     * You can then use @ref getReleaseInfo() to get the details of a particular release.
     *
     * @return  The list of available release names.
     */
    public ArrayList<String> queryReleases() {
        ArrayList<String> releaseList = new ArrayList<>();
        try {
            GHRelease[] releases = mRepo.listReleases().toArray();
            Log.v(LOG_TAG, "Releases in Github repo " + mAssetRepoUrl + ": ");
            for (GHRelease release : releases) {
                Log.v(LOG_TAG, release.getName());
                releaseList.add(release.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return releaseList;
    }


    /**
     * Download a file from given url and return it as a string.
     *
     * @note: you should not use this method for large files.
     *
     * @param url   Url to download from.
     * @return   The file content as a string or empty in case Url doesn't exist
     *           or cannot be accessed.
     */
    private String downloadFileToMemory(String url) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = mHttpClient.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Download a file from given url and save it to the given path.
     *
     * @param url          Url to download from.
     * @param localPath    Path to save the file to.
     * @return             Number of bytes written, -1 on error.
     */
    private long downloadFileToDisk(String url, String localPath) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = mHttpClient.newCall(request).execute()) {
            File downloadedFile = new File(localPath);
            BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
            long bytesExpected = response.body().contentLength();
            Log.v(LOG_TAG, "Downloading " + url + " with size " + bytesExpected);
            BufferedSource source = response.body().source();
            byte[] data = new byte[1024*1024];
            long total = 0;
            int count;
            while ((count = source.read(data)) != -1) {
                sink.write(data, 0, count);
                total += count;
                System.out.println("Downloaded " + total + " bytes");
            }
            sink.close();
            return total;
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Get the list of available assets for specific release.
     *
     * @param releaseName   The release name
     *
     * @return The list of available assets for given release.
     */
    public ReleaseInfo getReleaseInfo(String releaseName) {
        String assetUrl = getUrlForAsset(releaseName, VOICE_INFO_FILE);
        if (assetUrl == null) {
            Log.e(LOG_TAG, "Release " + releaseName + " not found in Github repo " + mAssetRepoUrl);
            return null;
        }
        String releaseInfo = downloadFileToMemory(assetUrl);
        return new Gson().fromJson(releaseInfo, ReleaseInfo.class);
    }

    /**
     * Download the asset from remote repository synchronously. This method will block until the
     * download is complete. Prefer using @ref downloadVoiceFileAsync() if you want to avoid blocking
     * the UI thread.
     *
     * @param releaseName          The release name
     * @param compressedFileName   The compressed file name of the release asset
     * @param localPath            The local file path where the voice file should be stored.
     *
     * @return  true in case of success, false otherwise, e.g. if the asset could not be downloaded.
     */
    public Boolean downloadVoiceFile(String releaseName, String compressedFileName, String localPath) {
        String assetUrl = getUrlForAsset(releaseName, compressedFileName);
        if (assetUrl == null) {
            Log.e(LOG_TAG, "Asset " + compressedFileName + " not found in Release " + releaseName);
            return false;
        }

        Log.v(LOG_TAG, "Downloading " + assetUrl + " for release " + releaseName);
        long bytesWritten = downloadFileToDisk(assetUrl, localPath);
        if (bytesWritten > 0) {
            Log.v(LOG_TAG, "Downloaded " + bytesWritten + " bytes to " + localPath);
            return true;
        }
        return false;
    }

    /**
     * Download given asset from the remote repository asynchronously and pass the repsonse chunk-
     * wise to the given observer.
     * This method is used to download large files and should be preferred over @ref downloadVoiceFile().
     *
     * @param releaseName           The release name
     * @param compressedFilename    The compressed file name of the release asset
     * @param observer              The observer that will receive the downloaded file.
     *
     * @return Call object that can be used to cancel the download or null in case no
     *          download url could be retrieved from Github with the given parameters.
     */
    public Call downloadVoiceFileAsync(String releaseName, String compressedFilename, ProgressObserver observer) {
        if (getReleaseInfo(releaseName) == null) {
            Log.e(LOG_TAG, "Release " + releaseName + " not found in Github repo " + mAssetRepoUrl);
            return null;
        }
        String assetUrl = getUrlForAsset(releaseName, compressedFilename);
        if (assetUrl == null) {
            Log.e(LOG_TAG, "Asset " + compressedFilename + " not found in release " + releaseName);
            return null;
        }
        Log.v(LOG_TAG, "Downloading " + assetUrl + " from release " + releaseName);
        Request request = new Request.Builder().url(assetUrl).build();
        Call call = mHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Downloading failed: " + e.getMessage());
                observer.error(e.getMessage(), -1);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    observer.error(response.message(), response.code());
                    throw new IOException("Unexpected code " + response);
                }
                try (ResponseBody body = response.body()) {
                    long bytesExpected = -1;
                    if (body != null) {
                        bytesExpected = body.contentLength();
                        observer.start(bytesExpected);
                    }
                    Log.v(LOG_TAG, "Downloading " + assetUrl + " with size " + bytesExpected);
                    BufferedSource source = body.source();
                    int count;
                    while ((count = source.read(observer.getBuffer())) != -1) {
                        if (!observer.update(count)) {
                            Log.v(LOG_TAG, "Download cancelled");
                            return;
                        }
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    observer.error(e.getMessage(), -1);
                }
            }
        });
        return call;
    }

    /**
     * Get the URL for a specific asset.
     * @param releaseName           The release name
     * @param compressedFileName    The compressed file name of the release
     * @return  The URL for the asset, or null if the asset could not be found.
     */
    private String getUrlForAsset(String releaseName, String compressedFileName) {
        try {
            GHRelease[] releases = mRepo.listReleases().toArray();
            for (GHRelease release : releases) {
                if (release.getName().equals(releaseName)) {
                    PagedIterable<GHAsset> assets = release.listAssets();
                    for (GHAsset asset : assets) {
                        if (asset.getName().equals(compressedFileName)) {
                            Log.v(LOG_TAG, "Found " + compressedFileName + " for release " + releaseName);
                            return asset.getBrowserDownloadUrl();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get download URL for given voice file for the requested platform.
     *
     * @param releaseName           The release name
     * @param voiceInternalName     The internal name of the voice file
     * @param platform              The platform for which the voice file should be downloaded
     *
     * @return The download URL for the voice file, or null if the voice file could not be found.
     */
    public String getDownloadUrlForVoice(String releaseName, String voiceInternalName, String platform) {
        ReleaseInfo releaseInfo = getReleaseInfo(releaseName);
        if (releaseInfo == null) {
            Log.e(LOG_TAG, "Release " + releaseName + " not found in Github repo " + mAssetRepoUrl);
            return null;
        }
        for (VoiceInfo voiceInfo : releaseInfo.voices) {
            if (voiceInfo.internalName.equals(voiceInternalName)) {
                for (VoiceFile file : voiceInfo.files) {
                    if (file.platform.equals(platform)) {
                        return getUrlForAsset(releaseName, file.compressedFile);
                    }
                }
            }
        }
        Log.e(LOG_TAG, "No compatible voice found for voice " + voiceInternalName
                + " in release " + releaseName);
        return null;
    }
}
