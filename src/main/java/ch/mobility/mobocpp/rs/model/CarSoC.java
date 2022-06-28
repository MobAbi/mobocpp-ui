package ch.mobility.mobocpp.rs.model;

//import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class CarSoC {

    private static final String NA = "NA";

//    @JsonProperty("createdAt")
    private final Instant createdAt;
//    @JsonProperty("vin")
    private final String vin;
//    @JsonProperty("soc")
    private final String soc;
//    @JsonProperty("chargestatus")
    private final Chargestatus chargestatus;

    public CarSoC(Instant createdAt, String vin, String soc, String chargestatus) {
        checkParam(createdAt, vin, soc, chargestatus);
        this.createdAt = createdAt;
        this.vin = vin;
        this.soc = soc;
        this.chargestatus = Chargestatus.fromValue(chargestatus);
    }

    private void checkParam(Instant createdAt, String vin, String soc, String chargestatus) {
        if (createdAt == null || "".equals(createdAt)) {
            throw new IllegalArgumentException("Param may not be null or empty: <createdAt>");
        }
        if (vin == null || "".equals(vin)) {
            throw new IllegalArgumentException("Param may not be null or empty: <vin>");
        }
        if (soc == null || "".equals(soc)) {
            throw new IllegalArgumentException("Param may not be null or empty: <soc>");
        }
        if (chargestatus == null || "".equals(chargestatus)) {
            throw new IllegalArgumentException("Param may not be null or empty: <chargestatus>");
        }
        if (!"NA".equals(soc)) {
            try {
                int socInt = Integer.valueOf(soc);
                if (socInt < 0 || socInt > 100) {
                    throw new IllegalArgumentException("SoC value must be between 0 and 100: " + socInt);
                }
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Param <soc> must be 'NA' or an integer value: " + soc);
            }
        }
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getVin() {
        return vin;
    }

    public boolean hasSoc() {
        return !NA.equals(soc);
    }

    public int getSoc() {
        if (hasSoc()) {
            return Integer.valueOf(soc).intValue();
        }
        return -1;
        //throw new IllegalStateException("No SoC available");
    }

    public Chargestatus getChargestatus() {
        return chargestatus;
    }

    @Override
    public String toString() {
        return "CarSoC{" +
                "createdAt=" + createdAt +
                ", vin='" + vin + '\'' +
                ", soc='" + soc + '\'' +
                ", chargestatus='" + chargestatus.getValue() + '\'' +
                '}';
    }
}
