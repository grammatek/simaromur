/**
 * Created by Daniel Schnell.
 * Copyright (c) 2021 Grammatek ehf. All rights reserved.
 */

#include <jni.h>
#include <string>
#include "G2PService.h"
#include "G2P.h"
#include "Logger.h"

namespace {

jfieldID FIELD_mNativeData;

class G2PJNIData {
public:
    JNIEnv*                         env_;
    jobject                         tts_ref_;
    grammatek::G2P*                 g2p_engine;
    grammatek::Logger*              logger;

    G2PJNIData(): env_(nullptr), tts_ref_(nullptr), g2p_engine(nullptr), logger(nullptr)
    { }

    ~G2PJNIData()
    {
        if (g2p_engine)
        {
            delete g2p_engine;
            g2p_engine = nullptr;
        }
        if (logger)
        {
            delete logger;
            logger = nullptr;
        }
    }
};
}

#ifdef __cplusplus
extern "C" {
#endif  // __cplusplus
JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;

    if (vm->GetEnv(reinterpret_cast<void **>(&env),
                   JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    return JNI_VERSION_1_6;
}

JNIEXPORT jboolean
JNICALL Java_com_grammatek_simaromur_device_NativeG2P_nativeClassInit(
        JNIEnv * env, jclass cls) {
    FIELD_mNativeData = env->GetFieldID(cls, "mNativeData", "J");
    return JNI_TRUE;
}

JNIEXPORT jboolean
JNICALL Java_com_grammatek_simaromur_device_NativeG2P_nativeCreate(
        JNIEnv *env, jobject object, jstring path) {

    const char *path_string = env->GetStringUTFChars(path, 0);

    auto* jni_data = new G2PJNIData();
    jni_data->logger = new grammatek::Logger("G2PService");
    jni_data->g2p_engine = new grammatek::G2P(path_string, "G2P");

    env->SetLongField(object, FIELD_mNativeData, reinterpret_cast<jlong>(jni_data));

    env->ReleaseStringUTFChars(path, path_string);
    return JNI_TRUE;
}

JNIEXPORT jboolean
JNICALL Java_com_grammatek_simaromur_device_NativeG2P_nativeDestroy(
        JNIEnv *env, jobject object) {
    uint64_t jni_data_address = env->GetLongField(object, FIELD_mNativeData);
    auto* jni_data = reinterpret_cast<G2PJNIData*>(jni_data_address);
    return JNI_TRUE;
}

JNIEXPORT jstring
JNICALL Java_com_grammatek_simaromur_device_NativeG2P_nativeProcess(
        JNIEnv *env, jobject object, jstring aText) {
    uint64_t jni_data_address = env->GetLongField(object, FIELD_mNativeData);
    auto* jni_data = reinterpret_cast<G2PJNIData*>(jni_data_address);
    auto g2p_engine = jni_data->g2p_engine;

    const char *text = env->GetStringUTFChars(aText, 0);
    auto phonemes = g2p_engine->process(text);
    env->ReleaseStringUTFChars(aText, text);
    jstring result = env->NewStringUTF(phonemes.c_str());
    return result;
}

#ifdef __cplusplus
}
#endif  // __cplusplus
