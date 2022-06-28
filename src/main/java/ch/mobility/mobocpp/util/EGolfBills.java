package ch.mobility.mobocpp.util;

import ch.mobility.mobocpp.stammdaten.StammdatenFahrzeug;

public class EGolfBills {
    public static final String EGOLF_VIN = "WVWZZZAUZLW908093";
    public static final String EGOLF_KENNZEICHEN = "NW-19561";
    public static final String EGOLF_HERSTELLER = "VW";
    public static final String EGOLF_MODELL = "e-Golf 300";
    public static final double EGOLF_LONGITUDE = 46.97473170278975;
    public static final double EGOLF_LATITUDE = 8.418418517873619;

    public static StammdatenFahrzeug getStammdatenEGolfBills() {
        return StammdatenFahrzeug.of(EGOLF_VIN, EGOLF_KENNZEICHEN, EGOLF_HERSTELLER, EGOLF_MODELL);
    }
}
