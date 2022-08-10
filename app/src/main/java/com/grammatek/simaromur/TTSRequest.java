package com.grammatek.simaromur;

import java.util.UUID;

/**
 * This is a simple class for storing TTS requests and assign them a unique id. This is used for
 * tracking a request among the different services processing it, e.g. frontend pipeline, voice
 * generation, network api, user interaction, etc.
 */
public class TTSRequest {
    // uuid of the cache item related to the utterance
    String mCacheItemUuid;

    // unique id of the request, so that also multiple requests with the
    // same text can be distinguished
    String mRequestUuid;

    /**
     * Constructor for TTSRequest.
     *
     * @param cacheItemUuid UUID of the cache item related to the utterance
     */
    public TTSRequest(String cacheItemUuid) {
        this.mCacheItemUuid = cacheItemUuid;
        this.mRequestUuid = UUID.randomUUID().toString();
    }

    /**
     * Constructs a new TTSRequest object with the given cache item uuid and request uuid.
     * This should be used when restoring a TTSRequest from a network x-request-id header.
     *
     * @param cacheItemUuid uuid of the cache item related to the utterance
     * @param requestUuid uuid of the request
     */
    public TTSRequest(String cacheItemUuid, String requestUuid) {
        this.mCacheItemUuid = cacheItemUuid;
        this.mRequestUuid = requestUuid;
    }

    /**
     * Returns the uuid of the cache item related to the utterance.
     */
    public String getCacheItemUuid() {
        return mCacheItemUuid;
    }

    /**
     * Returns the unique id of the request.
     */
    public String getRequestUuid() {
        return mRequestUuid;
    }

    /**
     * Returns the serialized version of the TTSRequest as String. This can e.g. be used for
     * storing the TTSRequest in a x-request-id header.
     */
    public String serialize() {
        return mCacheItemUuid + "," + mRequestUuid;
    }

    /**
     * Returns a TTSRequest object from the given serialized version.
     */
    public static TTSRequest restore(String serialized) {
        String[] parts = serialized.split(",");
        return new TTSRequest(parts[0], parts[1]);
    }

    /**
     * Provides equality check for TTSRequest objects.
     * @return true in case of equality, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;
        TTSRequest aRequest = (TTSRequest) o;
        // field comparison
        return aRequest.mRequestUuid.equals(mRequestUuid)
                && aRequest.mCacheItemUuid.equals(mCacheItemUuid);
    }

}
