# Símarómur

This project provides an Icelandic TTS application for the Android TTS service.

## Voices

Símarómur provides access to network voices of the [Tíro TTS API](https://tts.tiro.is/v0/voices) and
[on-device voices](https://github.com/grammatek/simaromur_voices) that are bundled via assets.

Handling of device-local voices started originally based on [Flite TTS Engine For Android](https://github.com/happyalu/Flite-TTS-Engine-for-Android),
but we changed it considerably and started with a clean slate instead of forking the project.
We replaced many deprecated API's and also use current TTS Service Android API's. We also use CMake
for integrating the C++ part instead of ndk-build and adapted the JNI part to be compatible with 64Bit
platforms.

We are using PyTorch mobile for inferencing the on-device neural network voices, but we plan usage of
"old-school" FLite based voices as well. The network voices are currently CPU-only and need a powerful
processor to run with acceptable inference speed.

## Text Normalization & G2P

Icelandic text normalization is always done on the phone itself before the text enters G2P.
Local voice G2P is [rule-based](https://github.com/grammatek/g2p-thrax) and is implemented using the C++
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

Fetch the voice assets subdirectory via

```bash
git submodule update --init
```

Then create the file `local.properties` if it doesn't already exist and add variables `flite.dir` to
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

All new code is Copyright © 2021-2022 Grammatek ehf and licensed under the [Apache License](LICENSE).

Native C++ files starting with `edu_cmu_XXX` and related Java bindings are Copyright 2010-2012 by Carnegie Mellon University and are licensed under [LICENSE-CMU](LICENSE-CMU.txt). 
We use the 3rdparty library Sonic for audio speed and pitch manipulation. Sonic is Copyright 2010, 2011 by Bill Cox and is licensed under the [Apache License](LICENSE)

This software is developed under the auspices of the Icelandic Government 5-Year Language Technology Program, described
[here](https://www.stjornarradid.is/lisalib/getfile.aspx?itemid=56f6368e-54f0-11e7-941a-005056bc530c) and
[here](https://clarin.is/media/uploads/mlt-en.pdf) (English).
