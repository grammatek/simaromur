package com.grammatek.simaromur.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import com.grammatek.simaromur.network.tiro.TiroAPI;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class checks for network connection changes and saves it in a flag
 */
public class ConnectionCheck {
    private final static String LOG_TAG = "Simaromur_" + ConnectionCheck.class.getSimpleName();
    private final Context context;
    private final ScheduledThreadPoolExecutor sch;
    private ScheduledFuture<?> periodicFuture;
    private static boolean isNetworkConnected = false;
    private static boolean isTTSServiceReachable = false;
    final static DateFormat fmt = DateFormat.getTimeInstance(DateFormat.LONG);

    public static boolean isNetworkConnected() {
        return isNetworkConnected;
    }

    public static boolean isTTSServiceReachable() {
        return isTTSServiceReachable;
    }

    public interface ConnectivityCallback {
        void onDetected(boolean isConnected);
    }

    // You need to pass the context when creating the class
    public ConnectionCheck(Context context) {
        this.context = context;
        sch = (ScheduledThreadPoolExecutor)
                Executors.newScheduledThreadPool(5);
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
                           if (nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                               Log.v(LOG_TAG, "Internet available");
                               isNetworkConnected = true;
                               startTTSServiceHealthCheck();
                           }
                       }
                       @Override
                       public void onLost(Network network) {
                           Log.v(LOG_TAG, "Internet lost");
                           stopTTSServiceHealthCheck();
                           isNetworkConnected = false;
                       }
                   }
            );
        } catch (Exception e){
            stopTTSServiceHealthCheck();
            isNetworkConnected = false;
        }
    }

    /**
     * Start timer that polls regularly TTS service availability
     */
    private void startTTSServiceHealthCheck() {
        // if already started: do nothing
        if (periodicFuture == null || periodicFuture.isCancelled()) {
            periodicFuture = sch.scheduleAtFixedRate(periodicTask, 0, 30, TimeUnit.SECONDS);
        }
    }

    /**
     * Stops timer for TTS service availability
     */
    private void stopTTSServiceHealthCheck() {
        // if timer not running: do nothing
        // stop timer
        periodicFuture.cancel(false);
    }

    /**
     * Check if host is reachable.
     *
     * @param host The host to check for availability. Can either be a machine name, such as "google.com",
     *             or a textual representation of its IP address, such as "8.8.8.8".
     * @param port          The port number.
     * @param timeoutInMs   The timeout in milliseconds.
     * @return True if the host is reachable. False otherwise.
     */
    public static boolean isHostAvailable(final String host, final int port, final int timeoutInMs) {
        try (final Socket socket = new Socket()) {
            final InetAddress inetAddress = InetAddress.getByName(host);
            final InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, port);

            socket.connect(inetSocketAddress, timeoutInMs);
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    // And yet another
    Runnable periodicTask = new Runnable(){
        @Override
        public void run() {
            try{
                if (isHostAvailable(TiroAPI.SERVER, 443, 2000)) {
                    isTTSServiceReachable = true;
                    Log.d(LOG_TAG,"TTS Service available!");
                } else {
                    isTTSServiceReachable = false;
                    Log.d(LOG_TAG, "TTS Service is NOT available !");
                }
            } catch (Exception e){
                Log.w(LOG_TAG, "Exception: " + e.getMessage());
            }
        }
    };
}
