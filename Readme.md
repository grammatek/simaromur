# Símarómur

This project provides an Icelandic TTS application for the Android TTS service. It is planned to
provide local voices on the device as well as access to network voices over a network API.

Handling of local voices is based on [Flite TTS Engine For Android](https://github.com/happyalu/Flite-TTS-Engine-for-Android) and is
adapted to current Android platforms. It uses CMake for integrating the C++ part instead of ndk-build and is able
to run on 64Bit platforms. We expect to modify the code considerably and making it more mature. Therefore, we started
with a clean slate instead of forking it.

Currently, Símarómur is more a tech-demo, than a fully functional TTS service, but we can already access
and demo Icelandic network voices inside the "SIM Voices" activity.

I. So far, the local voice UI provides nothing more than the original Flite TTS Engine for Android
project - basically English & Indian voices, just in a less buggy way :)

But we are working actively on the implementation of a new device-based Icelandic voice. Stay tuned
for project updates ...

## Text Normalization & G2P

Icelandic text normalization is always done on the Phone itself before the text enters G2P.
Local voice G2P is [rule-based](https://github.com/grammatek/g2p-thrax), implemented using the C++
frameworks Thrax & OpenFST, which are accessed via JNI. The network voices use somewhat better but
computationally more demanding [LSTM g2p-models](https://github.com/grammatek/g2p-lstm).

## Build Prerequisites

This project uses the [FLite library](https://github.com/grammatek/Flite) for local TTS. We have adapted it to build
all necessary binaries in the branch `android-grammatek`. Therefore, you should build this project first and
use the appropriate path in file `local.properties` as explained further down. This is necessary, so that
linking the C++ native library `libttsflite.so` succeeds.

Furthermore, you need our versions of [OpenFST](https://github.com/grammatek/openfst) &
[Thrax](https://github.com/grammatek/thrax) with the appropriate fixes to build for Android inside
the branch `android`. Please build & install these first, before compiling Símarómur.

### Using prebuilt libraries from github releases

For our CI jobs, we have already prebuilt all dependent libraries and published as Github release
assets at their corresponding project site. You can take advantage of these and install them locally
inside your project directory via the following procedure:

Set environment variables for the used release versions, e.g. :

```bash
export OPENFST_VER=1.8.1-android
export THRAX_VER=1.3.6-android
export FLITE_VER=v2.3-pre1-android
```

Then run this script:

```bash
.github/scripts/dl_3rdparty.sh
```

This should download and extract all necessary binaries to the sub-directory `3rdparty/ndk`.

## Configuration & Build

Create the file `local.properties` if it doesn't already exist and add variables `flite.dir` to
point either to the absolute path of your FLite root directory and `3rdparty.dir` for the installed
OpenFST/Thrax libraries, e.g.

```text
flite.dir=/Users/fred/projects/flite
3rdparty.dir=/Users/fred/install-android
```

or in case you have downloaded our releases via `dl_3rdparty.sh`, point these variables into your
project directory `simaromur/3rdparty/ndk`, e.g.:

```text
flite.dir=/Users/fred/projects/simaromur/3rdparty/ndk
3rdparty.dir=/Users/fred/projects/simaromur/3rdparty/ndk
```

It might also be necessary, to adapt/uncomment the variable `ndkVersion` inside
[app/build.gradle](app/build.gradle) depending on your installed NDK version. Then build the project
inside Android Studio.

## Contributing

You can contribute to this project by forking it, creating a branch and opening a new
[pull request](https://github.com/grammatek/simaromur/pulls).

## License

Original Copyright information can be found in [LICENSE-CMU](LICENSE-CMU.txt).

All new code is Copyright © 2021 Grammatek ehf and licensed under the [Apache License](LICENSE).

This software is developed under the auspices of the Icelandic Government 5-Year Language Technology Program, described
[here](https://www.stjornarradid.is/lisalib/getfile.aspx?itemid=56f6368e-54f0-11e7-941a-005056bc530c) and
[here](https://clarin.is/media/uploads/mlt-en.pdf) (English).
