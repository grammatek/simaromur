# Símarómur

![GitHub Super-Linter](https://github.com/grammatek/simaromur/workflows/Lint%20Code%20Base/badge.svg)](<https://github.com/marketplace/actions/super-linter>)


This project is based on [Flite TTS Engine For Android](https://github.com/happyalu/Flite-TTS-Engine-for-Android) and
adapted to current Android platforms. It uses CMake for integrating the C++ part instead of ndk-build and is now able
to run on 64Bit platforms.

Currently, it's only a tech-demo, but provides already an Icelandic G2P module based on Thrax. If you
navigate on your phone to the `TTS Demo` screen and press on an Icelandic text, Logcat will show you
an appropriate G2P output of that text from our G2P Thrax model. We have no Icelandic voice yet,
so don't be surprised to listen to the crappy default voice you have selected in your `Manage Voices`
screen trying to speak Icelandic.

We expect to modify the code considerably and making it more mature. Therefore, we started
with a clean slate and renamed the original project to Símarómur, instead of forking it.

## Prerequisites

This project uses the [FLite library](https://github.com/grammatek/Flite) for TTS. We have adapted it to build
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
