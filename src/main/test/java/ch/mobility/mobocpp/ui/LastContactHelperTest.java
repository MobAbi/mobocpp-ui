package ch.mobility.mobocpp.ui;

import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LastContactHelperTest {

    @Test
    public void testNegativeLimit() {
        try {
            LastContactHelper.isLastContactOverLimit(-1, null);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testNullValue() {
        final int limitInSeconds = 5 * 60; // 5 Minuten
        boolean lastContactOverLimit = LastContactHelper.isLastContactOverLimit(limitInSeconds, null);
        assertFalse(lastContactOverLimit);
    }

    @Test
    public void testLimitExactlyReached() {
        final int limitInSeconds = 5 * 60; // 5 Minuten
        final Instant toCheck = Instant.now().minusSeconds(limitInSeconds);
        boolean lastContactOverLimit = LastContactHelper.isLastContactOverLimit(limitInSeconds, toCheck);
        assertTrue(lastContactOverLimit);
    }

    @Test
    public void testLimitNotReachedBy1Second() {
        final int limitInSeconds = 5 * 60; // 5 Minuten
        final Instant toCheck = Instant.now().minusSeconds(limitInSeconds - 1);
        boolean lastContactOverLimit = LastContactHelper.isLastContactOverLimit(limitInSeconds, toCheck);
        assertFalse(lastContactOverLimit);
    }
}
