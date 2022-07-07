package com.grammatek.simaromur.cache;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.datastore.core.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import kotlin.Unit;
import kotlin.coroutines.Continuation;

public class UtteranceCacheSerializer implements Serializer<UtteranceCache> {
    private final static String LOG_TAG = "Simaromur_" + UtteranceCacheSerializer.class.getSimpleName();

    @Override
    public UtteranceCache getDefaultValue() {
        return UtteranceCache.getDefaultInstance();
    }

    @Nullable
    @Override
    public Object readFrom(@NonNull InputStream inputStream, @NonNull Continuation<? super UtteranceCache> continuation) {
        try {
            return UtteranceCache.parseFrom(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return getDefaultValue();
        }
    }

    @Nullable
    @Override
    public Object writeTo(UtteranceCache utteranceCache, @NonNull OutputStream outputStream, @NonNull Continuation<? super Unit> continuation) {
        try {
            utteranceCache.writeTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
