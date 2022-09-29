# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.19)

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

add_subdirectory(g2p)
add_subdirectory(flite)

