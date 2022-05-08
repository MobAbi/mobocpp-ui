package ch.mobility.mobocpp.ui;

public class CSStammdaten {
    private final String id;
    private final String coordinates;
    private final String name;
    private final String plz;
    private final String ort;

    public static CSStammdaten of(String id, String coordinates, String name, String plz, String ort) {
        return new CSStammdaten(id, coordinates, name, plz, ort);
    }

    private CSStammdaten(String id, String coordinates, String name, String plz, String ort) {
        this.id = id;
        this.coordinates = coordinates;
        this.name = name;
        this.plz = plz;
        this.ort = ort;
    }

    public String getId() {
        return id;
    }

    public String getCoordinates() {
        return coordinates;
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
}
