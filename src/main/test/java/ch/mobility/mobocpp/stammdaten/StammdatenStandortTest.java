package ch.mobility.mobocpp.stammdaten;

import org.junit.Test;

import static org.junit.Assert.*;

public class StammdatenStandortTest {

    @Test
    public void testWithoutGeolocation() {
        StammdatenStandort stammdatenStandort =
                StammdatenStandort.of("id", "bezTest", "strasseTest", "plzTest",
                        "ortTest", "NW", "UNBEKANNT", "UNBEKANNT");
        assertFalse(stammdatenStandort.hasGeolocation());
    }

    @Test
    public void testWithGeolocation() {
        StammdatenStandort stammdatenStandort =
                StammdatenStandort.of("id", "bezTest", "strasseTest", "plzTest",
                        "ortTest", "NW", "46.974695882686206", "8.417824395239812");
        assertTrue(stammdatenStandort.hasGeolocation());
    }
}