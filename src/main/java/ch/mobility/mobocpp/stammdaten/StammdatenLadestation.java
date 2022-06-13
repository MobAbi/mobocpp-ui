package ch.mobility.mobocpp.stammdaten;

public class StammdatenLadestation {
    private final String ladestationId;
    private final String standortId;

    public static StammdatenLadestation of(String ladestationId, String standortId) {
        return new StammdatenLadestation(ladestationId, standortId);
    }

    private StammdatenLadestation(String ladestationId, String standortId) {
        checkValues(ladestationId, standortId);
        this.ladestationId = ladestationId;
        this.standortId = standortId;
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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" +
                "LadestationId: '" + getLadestationId() +
                "', StandortId: '" + getStandortId() +
                "']";
    }
}
