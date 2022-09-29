/**
 * Created by Daniel Schnell.
 * Copyright (c) 2022 Grammatek ehf. All rights reserved.
 */

#include "FliteDriver.h"
#include "voice_driver.h"
#include "Logger.h"

// provides manual linking of shared libraries
#include <dlfcn.h>

namespace Grammatek {

// Logging happens via the global Logger object, that redirects stdout and stderr to the Android log

FliteDriver::FliteDriver(const std::string& voicePath)
{
    if (! loadVoice(voicePath)) {
        m_isZombie = true;
    }
}

FliteDriver::~FliteDriver()
{
    unloadVoice();
}

bool FliteDriver::loadVoice(const std::string& voiceDynLibraryFile)
{
    // call dlerror() to clear any previous error
    dlerror();
    // open the dynamic library
    // TODO: printf's left in for debugging purposes, will be removed in a later cleanup session
    printf("dlopen()ing %s ...\n", voiceDynLibraryFile.c_str());
    m_voice_dl.dl_handle = dlopen(voiceDynLibraryFile.c_str(), RTLD_NOW | RTLD_LOCAL);
    if (m_voice_dl.dl_handle == nullptr)
    {
        fprintf(stderr, "Failed to open dynamic library: %s", dlerror());
        fflush(stderr);
        return false;
    }
    printf("dlsym()ing %s ...\n", "voice_driver_init");
    m_voice_dl.voice_drv_init_p = (void* (*)()) dlsym(m_voice_dl.dl_handle, "voice_driver_init");
    printf("dlsym()ing %s ...\n", "voice_driver_cleanup");
    m_voice_dl.voice_drv_cleanup_p = (void (*)(void*)) dlsym(m_voice_dl.dl_handle, "voice_driver_cleanup");
    printf("dlsym()ing %s ...\n", "voice_driver_speak");
    m_voice_dl.voice_drv_speak_p = (ssize_t (*)(void* , const char* , float*, char*, size_t)) dlsym(m_voice_dl.dl_handle, "voice_driver_speak");
    printf("dlsym()ing %s ...\n", "voice_driver_info");
    m_voice_dl.voice_drv_info_p = (struct voice_drv_info_t (*)(void*)) dlsym(m_voice_dl.dl_handle, "voice_driver_info");

    if ((nullptr == m_voice_dl.voice_drv_init_p) ||
        (nullptr == m_voice_dl.voice_drv_cleanup_p) ||
        (nullptr == m_voice_dl.voice_drv_speak_p) ||
        (nullptr == m_voice_dl.voice_drv_info_p))
    {
        fprintf(stderr, "dlsym returned NULL somewhere");
        fflush(stderr);
        dlclose(m_voice_dl.dl_handle);
        return false;
    }

    printf("calling  voice_drv_init_p ...\n");
    m_voice_dl.drv_handle = m_voice_dl.voice_drv_init_p();
    if (nullptr == m_voice_dl.drv_handle)
    {
        fprintf(stderr, "m_voice_dl.voice_drv_init_p returned nullptr");
        fflush(stderr);
        dlclose(m_voice_dl.dl_handle);
        return false;
    }
    printf("calling  voice_drv_info_p ...\n");
    m_voice_dl.drv_info = m_voice_dl.voice_drv_info_p(m_voice_dl.drv_handle);
    printf("Loaded voice %s: %s, generates audio with %d bits / %d kHz audio\n",
           m_voice_dl.drv_info.name, m_voice_dl.drv_info.description, m_voice_dl.drv_info.bit_depth, m_voice_dl.drv_info.sample_rate);
    return true;
}

bool FliteDriver::unloadVoice()
{
    if (m_voice_dl.dl_handle != nullptr)
    {
        printf("Unloading voice %s ...\n", m_voice_dl.drv_info.name);
        m_voice_dl.voice_drv_cleanup_p(m_voice_dl.drv_handle);
        dlclose(m_voice_dl.dl_handle);
        m_voice_dl.dl_handle = nullptr;
        printf("Unloading voice finished\n");
    }
    return true;
}

ssize_t FliteDriver::synthesize(const std::string& phonemes, char * buffer, size_t bufferSize)
{
    if (m_isZombie)
    {
        fprintf(stderr,"synthesize: called on a zombie object\n");
        return -1;
    }

    ssize_t numBytes = m_voice_dl.voice_drv_speak_p(m_voice_dl.drv_handle, phonemes.c_str(),
                                                    &m_lastDuration, buffer, bufferSize);
    if (numBytes == 0)
    {
        fprintf(stderr,"synthesize phones ... FAIL\n");
        fflush(stderr);
        fflush(stderr);
    }
    else
    {
        printf("synthesize phones ... OK (%zd bytes, %.2f seconds)\n", numBytes, m_lastDuration);
    }
    return numBytes;
}

voice_drv_info_t FliteDriver::getVoiceInfo() const
{
    return m_voice_dl.drv_info;
}

float FliteDriver::getLastDuration() const
{
    return m_lastDuration;
}


} // namespace Grammatek
