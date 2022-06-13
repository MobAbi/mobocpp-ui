package ch.mobility.mobocpp.stammdaten;


public class StammdatenStandort {
    private final String standortId;
    private final String bezeichnung;
    private final String strasse;
    private final String plz;
    private final String ort;
    private final String kanton;
    private final String latitude;
    private final String longitude;

    public static StammdatenStandort of(String standortId, String bezeichnung, String strasse, String plz, String ort, String kanton, String latitude, String longitude) {
        return new StammdatenStandort(standortId, bezeichnung, strasse, plz, ort, kanton, latitude, longitude);
    }

    private StammdatenStandort(String standortId, String bezeichnung, String strasse, String plz, String ort, String kanton, String latitude, String longitude) {
        checkValues(standortId, bezeichnung, strasse, plz, ort, kanton, latitude, longitude);
        this.standortId = standortId;
        this.bezeichnung = bezeichnung;
        this.strasse = strasse;
        this.plz = plz;
        this.ort = ort;
        this.kanton = kanton;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private void checkValues(String standortId, String bezeichnung, String strasse, String plz, String ort, String kanton, String latitude, String longitude) {
        checkNullValue(standortId, "standortId");
        checkNullValue(bezeichnung, "bezeichnung");
        checkNullValue(strasse, "strasse");
        checkNullValue(plz, "plz");
        checkNullValue(ort, "ort");
        checkNullValue(kanton, "kanton");
        if (kanton.length() != 2) {
            throw new IllegalArgumentException("Kanton muss 2 Zeichen lang sein: " + kanton);
        }
        checkNullValue(latitude, "latitude");
        checkNullValue(longitude, "longitude");
    }

    private void checkNullValue(String value, String info) {
        if (value == null || "".equals(value)) {
            throw new IllegalArgumentException("Parameter " + info + " darf nicht <null> oder <leer> sein");
        }
    }

    public String getStandortId() {
        return standortId;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public String getStrasse() {
        return strasse;
    }

    public String getPlz() {
        return plz;
    }

    public String getOrt() {
        return ort;
    }

    public String getKanton() {
        return kanton;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" +
                "Standort-ID: '" + getStandortId() +
                "', Bezeichnung: '" + getBezeichnung() +
                "', Strasse: '" + getStrasse() +
                "', PLZ: '" + getPlz() +
                "', Ort: '" + getOrt() +
                "', Kanton: '" + getKanton() +
                "', Lon: '" + getLongitude() +
                "', Lat: '" + getLatitude() +
                "']";
    }
}
