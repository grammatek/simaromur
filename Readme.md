# Símarómur

This project provides an Icelandic TTS application for the Android TTS service. The current state
of the project is *production-ready*.

The app is available on the [Google Play Store](https://play.google.com/store/apps/details?id=com.grammatek.simaromur).

## Voices

Símarómur provides access to neural network [on-device voices](https://github.com/grammatek/simaromur_voices)
that are bundled via assets.

Currently, there is one male voice available, named **Steinn**. This voice is not only highly intelligible
but also possesses a pleasant and engaging tone, making it a versatile, general-purpose option that
sets the standard for Icelandic on-device text-to-speech (TTS) technology. It is well-suited for
reading both short and lengthy texts, providing a consistent listening experience.

We are currently developing a multi-speaker model that will include a female voice, slated for
future release.

## User Normalization Dictionary

Users can add normalization entries to accommodate alternative pronunciations of words or tokens.
These alternative pronunciations take precedence over the built-in normalization rules, applying
the specified replacements for any such terms found in the text being read.

To simplify usage, replacements can be made at the grapheme level without the need to understand or
use regular expression syntax. Users can immediately hear how the entered term and its replacement
sound with the current voice by using play buttons.

By default, the user normalization dictionary starts empty. At present, importing or exporting the
dictionary is not supported.

## Text Normalization & G2P

Icelandic text normalization is performed before the text enters G2P.
Local voice G2P is [rule-based](https://github.com/grammatek/g2p-thrax) and is implemented using the C++
frameworks Thrax & OpenFST, which are accessed via JNI.

## New since version 2.x
Deprecated FLite voices and the former neural network voices. Nowadays, Flite voices are obsolete
and we are using purely neural network voices instead. The FLite project is barely maintained, and
the runtime performance of the neural network voices is closing in on the FLite voices rapidly.
We can achieve 25x realtime speed with the neural network model on a Pixel 6 phone.

The neural network model is based on [VITS](https://github.com/jaywalnut310/vits) and trained via
[Piper TTS](https://github.com/rhasspy/piper). 

## Build Prerequisites

This project uses our versions of [OpenFST](https://github.com/grammatek/openfst) &
[Thrax](https://github.com/grammatek/thrax) with the appropriate fixes to build for Android inside
the branch `android`. Please build & install these first, before compiling Símarómur.

### Using prebuilt libraries from github releases

For our CI jobs, we have already prebuilt all dependent libraries and published as Github release
assets at their corresponding project site. You can take advantage of these and install them locally
inside your project directory via the following procedure:

Set environment variables for the used release versions, e.g. :

```bash
export OPENFST_TAG=1.8.1-android
export THRAX_TAG=1.3.6-android
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

Then create the file `local.properties` if it doesn't already exist and add variables `3rdparty.dir`
for the installed OpenFST/Thrax libraries, e.g.

```text
3rdparty.dir=/Users/fred/install-android
```

or in case you have downloaded our releases via `dl_3rdparty.sh`, point these variables into your
project directory `simaromur/3rdparty/ndk`, e.g.:

```text
3rdparty.dir=/Users/fred/projects/simaromur/3rdparty/ndk
```

It might also be necessary, to adapt/uncomment the variable `ndkVersion` inside
[app/build.gradle](app/build.gradle) depending on your installed NDK version. Then build the project
inside Android Studio.

## Contributing

You can contribute to this project by forking it, creating a branch and opening a new
[pull request](https://github.com/grammatek/simaromur/pulls).

## License

All code is Copyright © 2021-2024 Grammatek ehf. This code is licensed under the [Apache License](LICENSE-APACHE2).

## Acknowledgements
We use the 3rdparty libraries [Sonic](https://github.com/waywardgeek/sonic) for audio speed and pitch manipulation.
Sonic is Copyright 2010, 2011 by Bill Cox and is licensed under the [Apache License](LICENSE-APACHE2). Símarómur uses
adapted versions of [Thrax](https://www.openfst.org/twiki/bin/view/GRM/Thrax) and
[OpenFST](https://www.openfst.org/twiki/bin/view/FST/WebHome) for G2P. These are also licensed under the [Apache License](LICENSE-APACHE2).
Furthermore, we use OpenNLP for tokenization and sentence splitting. OpenNLP is licensed under the [Apache License](LICENSE-APACHE2).

### Sponsors
A big part of this software has been developed under the auspices of the Icelandic Government 5-Year Language Technology Program, described
[here](https://www.stjornarradid.is/lisalib/getfile.aspx?itemid=56f6368e-54f0-11e7-941a-005056bc530c) and
[here](https://clarin.is/media/uploads/mlt-en.pdf) (English).
