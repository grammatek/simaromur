package com.grammatek.simaromur.network.remoteasset;

/**
 * This class implements a progress observer for the download of a remote asset.
 */
public class ProgressObserver {
    /**
     * The interface that needs to be implemented by the caller to receive progress updates.
     */
    public interface Listener {
        /**
         * Called when the download starts. This is called before the first progress update.
         *
         * @param totalBytes   The total number of bytes that will be received.
         *                     This is the total size of the file that is being downloaded.
         *                     If the size is not known, this value will be -1.
         */
        void onStarted(long totalBytes);

        /**
         * Called when the download progress changes.
         *
         * @param buffer    The buffer containing data of the download.
         * @param numBytes  The number of bytes that have been written to the buffer.
         * @return  True if the download should continue, false if it should be aborted. The download
         *          will be aborted if this method returns false.
         */
        boolean onProgress(byte[] buffer, long numBytes);

        /**
         * Called in case of an error. The download will be aborted and no further progress updates
         * will be sent.
         *
         * @param error         The error that occurred.
         * @param errorCode     The error code.
         */
        void onError(String error, int errorCode);
    }

    private final Listener mListener;
    final private byte[] mData;

    /**
     * Constructor. The listener is called with the buffer, the offset and the total size of the
     * data.
     *
     * @param listener The listener to be called with progress updates.
     * @param chunkSize The size of the buffer to be used. If the total size is not known, the
     *                  buffer will be filled with the data and the listener will be called with
     *                  the offset set to -1.
     */
    public ProgressObserver(Listener listener, int chunkSize) {
        mListener = listener;
        mData = new byte[chunkSize];
    }

    /**
     * Return the internal buffer allocated for the download.
     *
     * This buffer is passed to the listener when the download progress changes and is reused
     * for the next download progress. The caller of the observer needs to get hold of the
     * buffer and copy the data into it.
     *
     * @return The internal buffer.
     */
    public byte[] getBuffer() {
        return mData;
    }

    /**
     * Called when the download starts.
     *
     * @param totalBytes The total number of bytes that will be received.
     *                   This is the total size of the file that is being downloaded.
     *                   If the size is not known, this value will be -1.
     */
    public void start(long totalBytes) {
        mListener.onStarted(totalBytes);
    }

    /**
     * Called when the download progress changes. To retrieve the data, the caller needs to
     * get hold of the buffer via @ref getBuffer() and copy the data from it.
     *
     * @param numBytes  The number of bytes that have been written to the buffer.
     * @return  true if the download should continue, false if it should be aborted. The download
     *          will be aborted if this method returns false. No further progress updates will be
     *
     */
    public boolean update(long numBytes) {
        return mListener.onProgress(mData, numBytes);
    }

    /**
     * Called in case of an error.
     *
     * @param error         The error that occurred.
     * @param errorCode     The response error code. If -1, the error code is not known.
     */
    public void error(String error, int errorCode) {
        mListener.onError(error, errorCode);
    }
}
