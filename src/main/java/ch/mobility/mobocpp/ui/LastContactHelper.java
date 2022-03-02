package ch.mobility.mobocpp.ui;

import java.time.Instant;

public class LastContactHelper {

    public static boolean isLastContactOverLimit(int limitInSeconds, Instant toCheck) {
        if (limitInSeconds < 1) {
            throw new IllegalArgumentException("Limit must be 1 seconds or greater");
        }
        if (toCheck != null) {
            final Instant limitInstant = Instant.now().minusSeconds(limitInSeconds);
            if (toCheck.isBefore(limitInstant)) {
                return true;
            }
        }
        return false;
    }
}
