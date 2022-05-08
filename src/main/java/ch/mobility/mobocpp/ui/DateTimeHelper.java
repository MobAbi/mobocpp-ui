package ch.mobility.mobocpp.ui;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

public class DateTimeHelper {

    final static DateTimeFormatter humanReadableFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
    final static DateTimeFormatter humanReadableFormatterUnused =
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

    public static long getSecondsSince(Instant instant) {
        if (instant != null) {
            final Instant now = Instant.from(Instant.now().atZone(ZoneOffset.UTC));
            if (instant.isBefore(now)) {
                final Duration duration = Duration.between(instant, now);
                return duration.abs().toSeconds();
            }
        }
        return -1L;
    }

//    public static String format(DateTime jodaDateTime) {
//        if (jodaDateTime != null) {
//            return format(Instant.ofEpochMilli(jodaDateTime.getMillis()));
//        }
//        return null;
//    }
}
