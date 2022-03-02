package ch.mobility.mobocpp.ui;

import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertNotNull;

public class DateTimeHelperTest {

    @Test
    public void testFormat() {
        String format = DateTimeHelper.format(Instant.now());
        assertNotNull(format);
    }

    @Test
    public void testHumanReadable() {
        String format = DateTimeHelper.humanReadable(Instant.now());
        assertNotNull(format);
    }

    @Test
    public void testParse() {
        Instant parsed = DateTimeHelper.parse("2022-03-02T09:31:06.410778Z");
        assertNotNull(parsed);
    }

}