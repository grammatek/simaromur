# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("simaromur")

set(CMAKE_VERBOSE_MAKEFILE on)

if("${ANDROID_ABI}" STREQUAL "armeabi-v7a")
    set (ARCH armv7a-linux-androideabi)
elseif("${ANDROID_ABI}" STREQUAL "arm64-v8a")
    set (ARCH aarch64-linux-android)
elseif("${ANDROID_ABI}" STREQUAL "x86")
    set (ARCH i686-linux-android)
elseif("${ANDROID_ABI}" STREQUAL "x86_64")
    set (ARCH x86_64-linux-android)
endif()

# Flite libraries
add_library(flite STATIC IMPORTED)
add_library(flite_cmu_indic_lex STATIC IMPORTED)
add_library(flite_cmu_indic_lang STATIC IMPORTED)
add_library(flite_cmulex STATIC IMPORTED)
add_library(flite_usenglish STATIC IMPORTED)

# Variable FLITE_DIR is set by build.gradle which again looks for the entry "flite.dir" in file
# local.properties. That's the way how to pass build variables from outside.

set_target_properties(flite PROPERTIES IMPORTED_LOCATION ${FLITE_DIR}/install/${ARCH}/lib/libflite.a)
set_target_properties(flite_cmu_indic_lex PROPERTIES IMPORTED_LOCATION ${FLITE_DIR}/install/${ARCH}/lib/libflite_cmu_indic_lex.a)
set_target_properties(flite_cmu_indic_lang PROPERTIES IMPORTED_LOCATION ${FLITE_DIR}/install/${ARCH}/lib/libflite_cmu_indic_lang.a)
set_target_properties(flite_cmulex PROPERTIES IMPORTED_LOCATION ${FLITE_DIR}/install/${ARCH}/lib/libflite_cmulex.a)
set_target_properties(flite_usenglish PROPERTIES IMPORTED_LOCATION ${FLITE_DIR}/install/${ARCH}/lib/libflite_usenglish.a)

set_target_properties(flite PROPERTIES INCLUDE_DIRECTORIES ${FLITE_DIR}/include)
include_directories( ${FLITE_DIR}/include )

find_library(
        log-lib
        log
)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( ttsflite
             # Sets the library as a shared library. Consequently,
             # all dependent library objects need to be compile via -fPIC
             SHARED

             # Provides a relative path to your source file(s).
             edu_cmu_cs_speech_tts_flite_engine.cc
             edu_cmu_cs_speech_tts_flite_service.cc
             edu_cmu_cs_speech_tts_flite_voices.cc
             edu_cmu_cs_speech_tts_string.cc
        )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( ttsflite PUBLIC
        flite_cmu_indic_lex
        flite_cmu_indic_lang
        flite_cmulex
        flite_usenglish
        flite
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
       )

add_subdirectory(g2p)

