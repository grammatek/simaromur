package com.grammatek.simaromur.db;

import androidx.room.TypeConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

// s. https://stackoverflow.com/questions/54593366/how-to-use-datetime-datatype-in-sqlite-using-room

public class TimestampConverter {
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    static {
        // we want UTC normalized entries, these are easy to convert into any other time zone
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // convert String to Date
    @TypeConverter
    public static Date fromTimestampString(String value) {
        if (value != null) {
            try {
                return df.parse(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // return current date, in case no valid date string has been provided
        return new Date();
    }

    // convert Date to String
    @TypeConverter
    public static  String toTimestampString(Date value) {
        if (value != null) {
            return df.format(value);
        } else {
            return null;
        }
    }
}
