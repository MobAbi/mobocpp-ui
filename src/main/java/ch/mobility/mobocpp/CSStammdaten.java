package ch.mobility.mobocpp;

public class CSStammdaten {
    private final String id;
    private final String name;
    private final String plz;
    private final String ort;
    private final String longitude;
    private final String latitude;

    public static CSStammdaten of(String id, String name, String plz, String ort, String longitude, String latitude) {
        return new CSStammdaten(id, name, plz, ort, longitude, latitude);
    }

    private CSStammdaten(String id, String name, String plz, String ort, String longitude, String latitude) {
        this.id = id;
        this.name = name;
        this.plz = plz;
        this.ort = ort;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPlz() {
        return plz;
    }

    public String getOrt() {
        return ort;
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
                "ID: '" + getId() +
                "', Name: '" + getName() +
                "', PLZ: '" + getPlz() +
                "', Ort: '" + getOrt() +
                "', Lon: '" + getLongitude() +
                "', Lat: '" + getLatitude() +
                "']";
    }
}
