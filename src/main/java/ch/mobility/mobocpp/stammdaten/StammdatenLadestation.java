package ch.mobility.mobocpp.stammdaten;

public class StammdatenLadestation {
    private final String ladestationId;
    private final String standortId;
    private final String bezeichnung;

    public static StammdatenLadestation of(String ladestationId, String standortId, String bezeichnung) {
        return new StammdatenLadestation(ladestationId, standortId, bezeichnung);
    }

    private StammdatenLadestation(String ladestationId, String standortId, String bezeichnung) {
        checkValues(ladestationId, standortId);
        this.ladestationId = ladestationId;
        this.standortId = standortId;
        this.bezeichnung = bezeichnung == null ? "" : bezeichnung;
    }

    private void checkValues(String ladestationId, String standortId) {
        checkNullValue(ladestationId, "ladestationId");
        checkNullValue(standortId, "standortId");
    }

    private void checkNullValue(String value, String info) {
        if (value == null || "".equals(value)) {
            throw new IllegalArgumentException("Parameter " + info + " darf nicht <null> oder <leer> sein");
        }
    }

    public String getLadestationId() {
        return ladestationId;
    }

    public String getStandortId() {
        return standortId;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public String getBezeichnungWithSeparator() {
        if (!"".equals(bezeichnung)) {
            return " - " + bezeichnung;
        }
        return "";
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" +
                "LadestationId: '" + getLadestationId() +
                "', StandortId: '" + getStandortId() +
                "', Bezeichnung: '" + getBezeichnung() +
                "']";
    }
}
