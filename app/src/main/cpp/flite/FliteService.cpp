/**
 * Created by Daniel Schnell.
 * Copyright (c) 2022 Grammatek ehf. All rights reserved.
 */

#include <string>
#include "FliteDriver.h"
#include "Logger.h"
#include <jni.h>

namespace {

volatile jfieldID FIELD_mNativeData;

class FliteServiceJNIData {
public:
    Grammatek::FliteDriver*         fliteDriverEngine;
    voice_drv_info_t                voiceInfo{};

    FliteServiceJNIData(): fliteDriverEngine(nullptr)
    { }

    ~FliteServiceJNIData()
    {
        if (fliteDriverEngine)
        {
            delete fliteDriverEngine;
            fliteDriverEngine = nullptr;
        }
    }
};

}

extern "C"
JNIEXPORT jint
JNI_OnLoad(JavaVM* pVM, void* reserved) {
    JNIEnv *env;
    if (pVM->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK) {
        abort();
    }
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_grammatek_simaromur_device_flite_NativeFliteVoice_nativeClassInit(JNIEnv *env, jclass clazz)
{
    FIELD_mNativeData = env->GetFieldID(clazz, "mNativeData", "J");
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_grammatek_simaromur_device_flite_NativeFliteVoice_nativeCreate(JNIEnv *env, jobject thiz, jstring path)
{
    const char *path_string = env->GetStringUTFChars(path, nullptr);

    auto* jniData = new FliteServiceJNIData();
    auto* logger = new grammatek::Logger("FliteService: nativeCreate");
    jniData->fliteDriverEngine = new Grammatek::FliteDriver(path_string);
    jniData->voiceInfo = jniData->fliteDriverEngine->getVoiceInfo();
    delete(logger);    // we don't need the logger anymore, so we can delete it
    env->SetLongField(thiz, FIELD_mNativeData, reinterpret_cast<jlong>(jniData));
    env->ReleaseStringUTFChars(path, path_string);
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_grammatek_simaromur_device_flite_NativeFliteVoice_nativeDestroy(JNIEnv *env, jobject thiz)
{
    auto jniDataHandle = env->GetLongField(thiz, FIELD_mNativeData);
    auto* jniData = reinterpret_cast<FliteServiceJNIData*>(jniDataHandle);
    auto* logger = new grammatek::Logger("FliteService: nativeDestroy");
    delete(jniData);
    delete(logger);
    env->SetLongField(thiz, FIELD_mNativeData, reinterpret_cast<jlong>(nullptr));
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_grammatek_simaromur_device_flite_NativeFliteVoice_nativeSynthesize(JNIEnv *env, jobject thiz, jstring aPhonemeString, jobject voiceData)
{
    auto jniDataHandle = env->GetLongField(thiz, FIELD_mNativeData);
    auto* jniData = reinterpret_cast<FliteServiceJNIData*>(jniDataHandle);
    auto* logger = new grammatek::Logger("FliteService: nativeSynthesize");
    auto fliteDriverEngine = jniData->fliteDriverEngine;
    const char *text = env->GetStringUTFChars(aPhonemeString, nullptr);
    auto* audioData = env->GetDirectBufferAddress(voiceData);
    auto audioDataSize = env->GetDirectBufferCapacity(voiceData);

    auto nBytes = fliteDriverEngine->synthesize(text, static_cast<char *>(audioData), audioDataSize);
    delete(logger);
    env->ReleaseStringUTFChars(aPhonemeString, text);
    return nBytes;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_grammatek_simaromur_device_flite_NativeFliteVoice_nativeGetSampleRate(JNIEnv *env,
                                                                               jobject thiz)
{
    auto jniDataHandle = env->GetLongField(thiz, FIELD_mNativeData);
    auto* jniData = reinterpret_cast<FliteServiceJNIData*>(jniDataHandle);
    return (jint) jniData->voiceInfo.sample_rate;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_grammatek_simaromur_device_flite_NativeFliteVoice_nativeGetBitsPerSample(JNIEnv *env, jobject thiz)
{
    auto jniDataHandle = env->GetLongField(thiz, FIELD_mNativeData);
    auto* jniData = reinterpret_cast<FliteServiceJNIData*>(jniDataHandle);
    return jniData->voiceInfo.bit_depth;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_grammatek_simaromur_device_flite_NativeFliteVoice_nativeGetVersion(JNIEnv *env, jobject thiz)
{
    auto jniDataHandle = env->GetLongField(thiz, FIELD_mNativeData);
    auto* jniData = reinterpret_cast<FliteServiceJNIData*>(jniDataHandle);
    auto encodedVersion = (jint) jniData->voiceInfo.voice_version;
    int versionBytes[3];
    // top most byte is never set: (encodedVersion >> 24) & 0xFF
    versionBytes[0] = (encodedVersion >> 16) & 0xFF;    // major
    versionBytes[1] = (encodedVersion >> 8) & 0xFF;     // minor
    versionBytes[2] = encodedVersion  & 0xFF;           // patch
    char versionString[12];
    snprintf(versionString, sizeof(versionString), "%d.%d.%d", versionBytes[0],
             versionBytes[1], versionBytes[2]);
    std::string version(versionString, sizeof(versionString));
    return env->NewStringUTF(version.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_grammatek_simaromur_device_flite_NativeFliteVoice_nativeGetDescription(JNIEnv *env, jobject thiz)
{
    auto jniDataHandle = env->GetLongField(thiz, FIELD_mNativeData);
    auto* jniData = reinterpret_cast<FliteServiceJNIData*>(jniDataHandle);
    std::string description = jniData->voiceInfo.description;
    return env->NewStringUTF(description.c_str());
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_grammatek_simaromur_device_flite_NativeFliteVoice_nativeGetLastDuration(JNIEnv *env, jobject thiz)
{
    auto jniDataHandle = env->GetLongField(thiz, FIELD_mNativeData);
    auto* jniData = reinterpret_cast<FliteServiceJNIData*>(jniDataHandle);
    return jniData->fliteDriverEngine->getLastDuration();
}
