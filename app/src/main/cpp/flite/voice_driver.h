// The idea is to provide a few well known functions that encapsulate most of the inner workings
// of the Flite voice and that we dlopen()/dlsym() from a high-level voice loader. This way, all voices integrating
// this interface can be dynamically loaded/unloaded at runtime

#ifndef FLITE_VOICE_DRIVER_H
#define FLITE_VOICE_DRIVER_H

#include <stdint.h>

// v.1.0.1
#define VOICE_DRV_VERSION   (0x010001)

enum voice_drv_bit_depth_e
{
    // self explanatory values ...
    VOICE_DRV_BIT_DEPTH_8 = 8,
    VOICE_DRV_BIT_DEPTH_16 = 16,
    VOICE_DRV_BIT_DEPTH_32 = 32,
};

/**
 * voice driver info struct.
 * @note: It's important, that the size of it never changes !
 */
struct __attribute__((aligned(8))) voice_drv_info_t
{
    // voice version, encoded as hex in major/minor/patch
    // major/minor/patch corresponds to the following 3 bytes:
    //  e.g. 0x010203 ==> v1.2.3
    uint32_t voice_version;
    // sample rate in Hz
    uint32_t sample_rate;
    // bit depth
    enum voice_drv_bit_depth_e bit_depth;
    // number of channels
    uint8_t channels;
    // voice name
    char name[64];
    // voice description, e.g. quality, built by whom and when, etc.
    char description[256];
};

/**
 * Initializes the voice driver and returns a handle for using it via
 * voice_driver_speak().
 *
 * @return  pointer to handle in case voice_driver_init() has been successful,
 *          NULL otherwise.
 */
void* voice_driver_init();

/**
 * Converts given phonemes to audio and returns it into given buffer as PCM format.
 * The exact specification of the audio can be queried via @ref voice_driver_info().
 *
 * @param handle         Handle returned by voice_driver_init()
 * @param phonemes       Phoneme string according to FLITE g2p alphabet
 * @param duration[out]  Returns the duration in seconds of the returned audio
 * @param buf[out]       Returns the audio to the pointer buf as PCM
 * @param buf_size       Size in bytes of given parameter buf
 *
 * @return  size of bytes written into the buffer, or -1 in case of an error
 */
ssize_t voice_driver_speak(void* handle, const char* phonemes, float* duration, char* buf, size_t buf_size);

/**
 * Query meta data about the voice. This function should be called first to examine
 *
 * @param handle    voice handle as returned by voice_driver_init()
 *
 * @return  voice_drv_info_t struct for the voice
 */
struct voice_drv_info_t voice_driver_info(void* handle);

/**
 * Cleans up driver. After calling this function, the given handle
 * is invalidated.
 *
 * @param handle         Handle returned by voice_driver_init()
 */
void voice_driver_cleanup(void* handle);

#endif // FLITE_VOICE_DRIVER_H
