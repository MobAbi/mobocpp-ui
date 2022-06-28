package ch.mobility.mobocpp.rs.model;

public enum Chargestatus {
    CHARGING_UNKOWN("CHARGING-UNKNOWN"),
    CHARGING("CHARGING"),
    NOT_CHARGING("NOT-CHARGING");

    private String value;

    Chargestatus(String value) {
        this.value = value;
    }

    public String getValue() { return this.value; }

    public static Chargestatus fromValue(String value) {
        for (Chargestatus chargestatus: Chargestatus.values()) {
            if (chargestatus.value.equals(value)) {
                return chargestatus;
            }
        }
        throw new IllegalArgumentException("Invalid enum value: " + value);
    }
}
