package com.grammatek.simaromur.db;

import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.grammatek.simaromur.utils.FileUtils;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.device.pojo.DeviceVoiceFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Create unique index
@Entity(tableName = "voice_table",
        indices = {@Index(value = {"internal_name", "gender", "language_code", "type"}, unique = true)})
public class Voice {
    private final static String LOG_TAG = "Simaromur_Java_" + Voice.class.getSimpleName();
    public final static String TYPE_TIRO = "tiro";
    public final static String TYPE_TORCH = "torchscript";
    public final static String TYPE_TFLOW = "tensorflow";
    public final static String TYPE_FLITE = "flite";

    public static final List<String> Types = Arrays.asList(TYPE_TIRO, TYPE_FLITE, TYPE_TORCH, TYPE_TFLOW);
    static final String SEP = "-";

    @PrimaryKey(autoGenerate = true)
    public long voiceId;

    // Voice name (mandatory)
    //  e.g. Karl, Dóra
    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    // Voice Gender (mandatory)
    //  e.g. Male, Female
    @NonNull
    @ColumnInfo(name = "gender")
    public String gender;

    // Internal voice name (mandatory)
    //  e.g. "other", "Dora",
    @NonNull
    @ColumnInfo(name = "internal_name")
    public String internalName;

    // language code (mandatory)
    //  e.g. "is-IS"
    @NonNull
    @ColumnInfo(name = "language_code")
    public String languageCode;

    // language name (mandatory)
    //  e.g. "Íslenska"
    @NonNull
    @ColumnInfo(name = "language_name")
    public String languageName;

    // language variant, e.g. "north-clear" (optional, not used for network voices)
    @NonNull
    @ColumnInfo(name = "variant")
    public String variant;

    // Voice type (verified)
    // local voices: "clustergen", "clunits", "torchscript", "phoneme"
    // network voices: "tiro"
    @ColumnInfo(name = "type")
    public String type;

    // creation date of DB entry
    @ColumnInfo(name = "update_time")
    @TypeConverters({TimestampConverter.class})
    public Date updateTime;

    // download date/time for non-network downloadable voice
    @ColumnInfo(name = "download_time")
    @TypeConverters({TimestampConverter.class})
    public Date downloadTime;

    // the http URL of a downloadable voice, empty for a network voice or voice packaged via assets
    @ColumnInfo(name = "url")
    public String url;

    // For local voices, the fully qualified local filename, or empty in case it's not yet
    // downloaded. For network voices: empty
    @ColumnInfo(name = "download_path")
    public String downloadPath;

    // Version of the voice, if available. Should be semantically versioned (major, minor, patch)
    @ColumnInfo(name = "version")
    public String version;

    // MD5 checksum of downloaded/packaged voice as String, empty if not yet downloaded or in case
    // of a network voice
    @ColumnInfo(name = "md5_sum")
    public String md5Sum;

    // file size if local voice file, 0 means not yet downloaded or network voice
    @ColumnInfo(name = "local_size")
    public long size;

    // Constructor for NonNull parameters
    public Voice(@NonNull String name,
                 @NonNull String internalName,
                 @NonNull String gender,
                 @NonNull String languageCode,
                 @NonNull String languageName,
                 @NonNull String variant,
                 @NonNull String type,
                 @NonNull String url,
                 @NonNull String downloadPath,
                 @NonNull String version,
                 @NonNull String md5Sum,
                 long size) {
        this.name = name;
        this.internalName = internalName;
        this.gender = gender;
        this.languageCode = languageCode;
        this.languageName = languageName;
        this.variant = variant;
        if (Types.contains(type)) {
            this.type = type;
        }
        else {
            throw new AssertionError("Given voice type (" + type + ") not valid !");
        }
        this.url = url;
        this.downloadPath = downloadPath;
        this.version = version;
        this.md5Sum = md5Sum;
        this.size = size;
        this.updateTime = new Date();
    }

    // Constructor for DeviceVoice parameter
    public Voice(AssetManager assetManager, DeviceVoice voice) throws IOException {
        this.name = voice.Name;
        this.internalName = voice.InternalName;
        this.gender = voice.Gender;
        this.languageCode = voice.Language;
        this.languageName = voice.LanguageName;
        this.variant = voice.Name;
        if (Types.contains(voice.Type)) {
            this.type = voice.Type;
        }
        else {
            throw new AssertionError("Given voice type (" + voice.Type + ") not valid !");
        }
        this.url = "assets";    // => maintained by AssetVoiceManager
        this.version = voice.Version;

        // collect info from the related asset files
        LocalDateTime latestUpdateTime = Instant.ofEpochMilli(0).atZone(ZoneOffset.UTC).toLocalDateTime();
        this.downloadPath = "";
        this.md5Sum = "";

        for (DeviceVoiceFile file: voice.Files) {
            final LocalDateTime assetDate = FileUtils.getAssetDate(assetManager, "voices/" + file.Path);
            if (assetDate.compareTo(latestUpdateTime) > 0) {
                latestUpdateTime = assetDate;
            }
            // these Strings need to be split via "\n" to be usable again
            this.downloadPath = this.downloadPath.concat(file.Path + "\n");
            this.md5Sum = this.md5Sum.concat(file.Md5Sum + "\n");
        }
        this.updateTime = Date.from(latestUpdateTime.toInstant(ZoneOffset.UTC));
        this.downloadTime = this.updateTime;
        this.size = 1000000; // dummy size
    }

    @NonNull
    @Override
    public String toString() {
        return "Voice{" +
                "voiceId=" + voiceId +
                ", name='" + name + '\'' +
                ", internalName='" + internalName + '\'' +
                ", languageCode='" + languageCode + '\'' +
                ", languageName='" + languageName + '\'' +
                ", gender='" + gender + '\'' +
                ", variant='" + variant + '\'' +
                ", type='" + type + '\'' +
                ", updateTime=" + updateTime +
                ", downloadTime=" + downloadTime +
                ", url='" + url + '\'' +
                ", downloadPath='" + downloadPath + '\'' +
                ", version='" + version + '\'' +
                ", md5Sum='" + md5Sum + '\'' +
                ", size=" + size +
                '}';
    }

    /**
     * Returns boolean if voice needs network access / is network voice
     *
     * @return  true in case voice needs network access, false otherwise
     */
    public boolean needsNetwork() {
        return (this.type.equals(Voice.TYPE_TIRO));
    }

    /**
     * Returns boolean if voice is downloadable
     *
     * @return  true in case voice is downloadable, false otherwise
     */
    public boolean needsDownload() {
        return (this.url.startsWith("network:") || this.url.startsWith("http"));
    }

    /**
     * Returns if given voice is available. For network voices, true is given, but no
     * network API query is done. For local voices, the download date, size > 0 and md5Sum
     * is checked.
     *
     * @return  true in case voice is available, false otherwise
     */
    public boolean isAvailable() {
        boolean rv = false;
        if (this.needsNetwork()) {
            // network voices are supposed to be available, because these are already queried
            // at TTS service start
            // @todo: provide an additional Voice-based check in the AppRepository class
            rv = true;
        } else {
            // local voice sanity checks ...
            if ((this.md5Sum != null) && ! this.md5Sum.isEmpty()) {
                // Check download date to be valid
                Calendar cal = Calendar.getInstance();
                cal.setLenient(false);
                cal.setTime(this.downloadTime);
                try {
                    cal.getTime();
                    rv = true;
                } catch (Exception ignored) {
                    Log.w(LOG_TAG, "isAvailable("+this.name+"): invalid downloadTime detected");
                }
            }
        }
        return rv;
    }

    /**
     * Returns Locale of voice with language, country and name as variant.
     *
     * @return  locale of the voice
     */
    public Locale getLocale() {
        final String[] langCountrySplit = this.languageCode.split(SEP);
        return new Locale(langCountrySplit[0], langCountrySplit[1], this.name);
    }

    /**
     * Returns the ISO-3 representation for voice as Language-Country-Name
     *
     * @return  String with ISO-3 encoded Language-Country-Name
     */
    public String iso3LangCountryName() {
        final Locale locale = getLocale();
        return locale.getISO3Language()
                + SEP
                + locale.getISO3Country()
                + SEP
                + this.name;
    }

    /**
     * Returns the ISO-3 representation for voice as Language-Country-Name
     *
     * @return  String with ISO-3 encoded Language-Country-Name
     */
    public String iso2LangCountryName() {
        String[] iso2Languages = Locale.getISOLanguages();
        String[] iso2Countries = Locale.getISOCountries();
        final Locale locale = getLocale();
        return locale.getISO3Language()
                + SEP
                + locale.getISO3Country()
                + SEP
                + this.gender;
    }

    /**
     * Returns the ISO-3 representation for voice as Language-Country-Variant
     *
     * @return  String with ISO-3 encoded Language-Country-Variant
     */
    public String iso3LangCountryVariant() {
        final Locale locale = getLocale();
        return locale.getISO3Language()
                + SEP
                + locale.getISO3Country()
                + SEP
                + this.variant;
    }

    /**
     * Returns the ISO-3 representation for voice as Language-Country-Variant
     *
     * @return  String with ISO-3 encoded Language-Country-Variant
     */
    public String iso2LangCountryVariant() {
        final Locale locale = getLocale();
        return locale.getISO3Language()
                + SEP
                + locale.getISO3Country()
                + SEP
                + this.gender;
    }

    /**
     * Checks, if given ISO-3 language, country and variant matches our voice locale.
     * Parameters country and variant are optional, but if provided, they are used for checking.
     *
     * @param iso3Language  iso-3 language code
     * @param iso3Country   iso-3 country code (optional)
     * @param iso3Variant   language variant (optional)
     *
     * @return true in case given iso3 locale is supported by voice, false otherwise
     */
    public boolean supportsIso3(String iso3Language, String iso3Country, String iso3Variant) {
        Locale givenLocale = new Locale(iso3Language, iso3Country, iso3Variant);
        Locale voiceLocale = getLocale();
        // direct comparison of Locale's not supported, if different ISO code variants are used
        if (iso3Country.isEmpty()) {
            // only compare language
            return (voiceLocale.getISO3Language().equals(givenLocale.getISO3Language()));
        } else if (iso3Variant.isEmpty()) {
            // only compare language, country
            return (voiceLocale.getISO3Language().equals(givenLocale.getISO3Language())
                    && voiceLocale.getISO3Country().equals(givenLocale.getISO3Country()));
        }
        // compare all paramateres
        Log.v(LOG_TAG, "supportsIso3: compare all parameters");
        return (voiceLocale.getISO3Language().equals(givenLocale.getISO3Language())
                && voiceLocale.getISO3Country().equals(givenLocale.getISO3Country())
                && voiceLocale.getVariant().equals(givenLocale.getVariant()));
    }

    /**
     * Returns boolean if we have a fast voice type
     *
     * @return      true in case voice is fast, false otherwise
     */
    public boolean isFast() {
        return (this.type.equals(Voice.TYPE_FLITE));
    }
}
