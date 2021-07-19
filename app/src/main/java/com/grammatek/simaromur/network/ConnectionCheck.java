package com.grammatek.simaromur.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import com.grammatek.simaromur.App;

/**
 * Class checks for network connection changes and saves it in a flag
 */
public class ConnectionCheck {
    private final static String LOG_TAG = "Simaromur_" + ConnectionCheck.class.getSimpleName();
    private final Context context;

    public static boolean isNetworkConnected() {
        return isNetworkConnected;
    }

    private static boolean isNetworkConnected = false;

    // You need to pass the context when creating the class
    public ConnectionCheck(Context context) {
        this.context = context;
    }

    /**
     * Registers network callback to monitor network changes.
     */
    public void registerNetworkCallback()
    {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                       @Override
                       public void onAvailable(Network network) {
                           // network available
                           NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(network);
                           if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                               Log.v(LOG_TAG, "Internet available");
                               isNetworkConnected = true;
                           }
                       }
                       @Override
                       public void onLost(Network network) {
                           Log.v(LOG_TAG, "Internet lost");
                           isNetworkConnected = false;
                       }
                   }
            );
        } catch (Exception e){
            isNetworkConnected = false;
        }
    }
}
