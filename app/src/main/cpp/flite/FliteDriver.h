/**
 * Created by Daniel Schnell.
 * Copyright (c) 2022 Grammatek ehf. All rights reserved.
 */

#ifndef SIMAROMUR_FLITEDRIVER_H
#define SIMAROMUR_FLITEDRIVER_H

#include <string>

#include "voice_driver.h"

namespace Grammatek {

class FliteDriver
{
public:
    /**
     * Constructor
     * @param voicePath     Path to the voice file
     *
     * @todo: add buffer size parameter
     */
    FliteDriver(const std::string& voicePath);

    bool isZombie() const { return m_isZombie; }

    /**
     * Destructor
     */
    ~FliteDriver();

    /**
     * Synthesize given phonemes to audio data.
     *
     * @param phonemes      phonemes to synthesize
     * @param buffer        buffer to store synthesized audio data
     * @param bufferSize    size of buffer
     * @return  number of bytes written to buffer
     */
    ssize_t synthesize(const std::string &phonemes, char * buffer, size_t bufferSize);

    /**
     * Get all voice info from the loaded voice.
     *
     * @return  VoiceInfo struct with all voice info
     */
    voice_drv_info_t getVoiceInfo() const;

    /**
     * Returns the duration of the last synthesized phonemes.
     *
     * @return  duration in seconds of last synthesized phonemes
     */
    float getLastDuration() const;

private:

    /**
     * Dynamically load the voice library and initialize the voice. This must be called before any
     * other method.
     *
     * @param voiceDynLibraryFile path to the voice library
     * @return true if successful, false otherwise
     */
    bool loadVoice(const std::string& voiceDynLibraryFile);

    /**
     * Unload the voice library and clean up the voice. This must be called after all other methods.
     */
    bool unloadVoice();

    /**
     * Function pointer struct to functions provided via voice_driver.h. Please make sure, that the signature
     * of all function pointers matches their corresponding counterpart in voice_driver.h. These pointers should be
     * filled via dlopen()/dlsym() of a dynamic library linked with voice_driver.c
     */
    struct __attribute__((aligned(8))) voice_dl_t
    {
        // these pointers are dlysm()ed from the dynamic library

        // result pointer returned via dlopen()
        void* dl_handle;
        // pointer to voice_driver_init() method in dynamic lib
        void* (*voice_drv_init_p)();
        // pointer to voice_driver_cleanup() method in dynamic lib
        void (*voice_drv_cleanup_p)(void*);
        // pointer to voice_driver_speak() method in dynamic lib
        ssize_t (*voice_drv_speak_p)(void* , const char* , float*, char*, size_t);
        // pointer to voice_driver_info() method in dynamic lib
        struct voice_drv_info_t (*voice_drv_info_p)(void*);

        // these are filled by the flite voice after the driver is initialized

        // pointer to the flite voice internal struct
        void* drv_handle;

        // pointer to the voice info struct
        voice_drv_info_t drv_info;
    };
private:
    voice_dl_t m_voice_dl{};
    float m_lastDuration = 0.0f;
    bool m_isZombie = false;
};


}


#endif //SIMAROMUR_FLITEDRIVER_H
