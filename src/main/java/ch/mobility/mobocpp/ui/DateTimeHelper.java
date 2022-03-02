package ch.mobility.mobocpp.ui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

public class DateTimeHelper {

//    final static DateTimeFormatter humanReadableFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    final static DateTimeFormatter humanReadableFormatter =
            DateTimeFormatter.ofLocalizedDateTime( FormatStyle.LONG )
                    .withLocale( Locale.GERMAN )
//                    .withZone(ZoneId.from(ZoneOffset.UTC));
                    .withZone( ZoneId.systemDefault() );
//    private static DateTimeFormatter humanReadableFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));

    private DateTimeHelper() {}

    public static String now() {
        return format(Instant.now());
    }

    public static String format(TemporalAccessor temporal) {
        if (temporal != null) {
            return DateTimeFormatter.ISO_INSTANT.format(temporal);
        }

        return null;
    }

    public static String humanReadable(TemporalAccessor temporal) {
        if (temporal != null) {
            return humanReadableFormatter.format(temporal);
        }

        return "Unbekannt";
    }

    public static Instant parse(String datetime) {
        if (datetime != null) {
            TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(datetime);
            return Instant.from(ta);
        }
        return null;
    }

//    public static String format(DateTime jodaDateTime) {
//        if (jodaDateTime != null) {
//            return format(Instant.ofEpochMilli(jodaDateTime.getMillis()));
//        }
//        return null;
//    }
}
