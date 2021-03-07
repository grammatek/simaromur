# Símarómur

This project is based on [Flite TTS Engine For Android](https://github.com/happyalu/Flite-TTS-Engine-for-Android) and
adapted to current Android platforms. It uses CMake for integrating the C++ part instead of ndk-build and is now able
to run on 64Bit platforms.

Currently, it's only a tech-demo and provides the same features as the original code.
We expect to modify the code considerably for adding Icelandic TTS and making it more mature. Therefore, we started
with a clean slate and renamed it to Símarómur, instead of forking the original project.

# Prerequisites

This project uses the [FLite library](https://github.com/grammatek/Flite), we have adapted it to provide
all necessary binaries in the android-grammatek branch. Therefore, you should build this project first and
use the appropriate path in file `local.properties` as explained further down. This is necessary, so that
linking the C++ native library `libttsflite.so` succeeds.

# Installation

Create the file `local.properties` if it doesn't already exist and add a variable `flite.dir` to
point to the FLite root directory, e.g.

```
flite.dir=/Users/fred/projects/flite
```

Then build the project inside Android Studio.

# Contributing

You can contribute to this project by forking it, creating a private branch and opening a new
[pull request](https://github.com/grammatek/simaromur/pulls).

# License

Original Copyright information can be found in [LICENSE-CMU](LICENSE-CMU.txt).

All new code is Copyright © 2021 Grammatek ehf and licensed under the [Apache License](LICENSE).

This software is developed under the auspices of the Icelandic Government 5-Year Language Technology Program, described
[here](https://www.stjornarradid.is/lisalib/getfile.aspx?itemid=56f6368e-54f0-11e7-941a-005056bc530c) and
[here](https://clarin.is/media/uploads/mlt-en.pdf) (English).
