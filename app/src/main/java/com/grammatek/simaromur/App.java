package com.grammatek.simaromur;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import com.grammatek.simaromur.frontend.NormalizationManager;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    AppRepository mAppRepository;

    private static App sApplication;

    private NormalizationManager mNormalizationManager;

    public static App getApplication() {
        return sApplication;
    }
    public static AppRepository getAppRepository() {
        return sApplication.mAppRepository;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }


    public static String getAbsoluteFilePath(String relativePath) {
        if (Utility.isExternalStorageWritable()) {
            return new File(getContext().getExternalFilesDir(null), relativePath).getPath();
        } else {
            return new File(getContext().getFilesDir(), relativePath).getPath();
        }
    }
    public static String getDataPath() {
        return getAbsoluteDirPath("data")+"/";
    }

    public static String getVoiceDataPath() {
        return getAbsoluteDirPath("voices")+"/";
    }

    public static String getAbsoluteDirPath(String relativePath) {
        if (Utility.isExternalStorageWritable()) {
            return new File(getContext().getExternalFilesDir(null), relativePath).getPath();
        } else {
            return new File(getContext().getFilesDir(), relativePath).getParent();
        }
    }

    public NormalizationManager getNormalizationManager() {
        return mNormalizationManager;
    }

    /**
     * Returns boolean, if we have a network connection.
     *
     * @return  true in case network connection is available (via wifi and/or GSM)
     *          false otherwise
     *
     * @todo    uses deprecated API's
     */
    public boolean hasNetwork() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = false;
        boolean isMobileConn = false;
        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConn |= networkInfo.isConnected();
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                isMobileConn |= networkInfo.isConnected();
            }
        }

        return isWifiConn || isMobileConn;
    }

    /**
     * @todo: Fiddling around with network states, this is not the right implementation, because the
     *        callbacks are registered, but not necessarily fired before the method is finished.
     *        We need to have these started in a class,
     *        not just in a method and the booleans should be class variables ...
     *        Then again, it's not clear, if we can use these API's on older devices ...
     *        And we should ask for NET_CAPABILITY_INTERNET &  NET_CAPABILITY_VALIDATED instead of
     *        wifi & cellular
     */
    public boolean hasNetwork2() {
        final boolean[] isWifiConn = {false};
        final boolean[] isMobileConn = {false};
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // network available
                NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(network);
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    isWifiConn[0] = true;
                }
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    isMobileConn[0] = true;
                }
            }

            @Override
            public void onLost(Network network) {
                // network unavailable
                NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(network);
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    isWifiConn[0] = false;
                }
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    isMobileConn[0] = false;
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
        }
        return isWifiConn[0] || isMobileConn[0];
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        mAppRepository = new AppRepository(this);
        mNormalizationManager = new NormalizationManager(this.getBaseContext());
    }
}
