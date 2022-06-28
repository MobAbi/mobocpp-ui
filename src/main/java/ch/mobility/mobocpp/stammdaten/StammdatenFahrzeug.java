package ch.mobility.mobocpp.stammdaten;

public class StammdatenFahrzeug {
    private final String vin;
    private final String kennzeichen;
    private final String hersteller;
    private final String modell;

    public static StammdatenFahrzeug of(String vin, String kennzeichen, String hersteller, String modell) {
        return new StammdatenFahrzeug(vin, kennzeichen, hersteller, modell);
    }

    private StammdatenFahrzeug(String vin, String kennzeichen, String hersteller, String modell) {
        checkValues(vin, kennzeichen, hersteller, modell);
        this.vin = vin;
        this.kennzeichen = kennzeichen;
        this.hersteller = hersteller;
        this.modell = modell;
    }

    private void checkValues(String vin, String kennzeichen, String hersteller, String modell) {
        checkNullValue(vin, "vin");
        checkNullValue(kennzeichen, "kennzeichen");
        checkNullValue(hersteller, "hersteller");
        checkNullValue(modell, "modell");
    }

    private void checkNullValue(String value, String info) {
        if (value == null || "".equals(value)) {
            throw new IllegalArgumentException("Parameter " + info + " darf nicht <null> oder <leer> sein");
        }
    }

    public String getVin() {
        return vin;
    }

    public String getKennzeichen() {
        return kennzeichen;
    }

    public String getHersteller() {
        return hersteller;
    }

    public String getModell() {
        return modell;
    }

    @Override
    public String toString() {
        return "StammdatenFahrzeug{" +
                "vin='" + vin + '\'' +
                ", kennzeichen='" + kennzeichen + '\'' +
                ", hersteller='" + hersteller + '\'' +
                ", modell='" + modell + '\'' +
                '}';
    }
}
