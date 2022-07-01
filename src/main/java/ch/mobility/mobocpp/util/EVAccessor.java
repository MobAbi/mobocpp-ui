package ch.mobility.mobocpp.util;

import ch.mobility.mobocpp.stammdaten.StammdatenFahrzeug;
import ch.mobility.mobocpp.ui.DateTimeHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EVAccessor {
    private static final String CREATED_AT = "CreatedAt=";
    private static final String SOC = "SoC=";
    private static final String CHARGE_STATUS = "ChargeStatus=";

    private static final String EVSOC_URL = "http://upload.homelinux.com:8089/evsoc/";

    public List<EvMitLaufendenLadevorgang> getChargingEVsNearGeolocation(
            double longitudeStandort,
            double latitudeStandort,
            double evMaxMeterDistance2Standort,
            long valueMaxSecondsOld) {

        final List<EvMitLaufendenLadevorgang> result = new ArrayList<>();

        final double meters = GPSDistance.distance(
                latitudeStandort, longitudeStandort,
                EGolfBills.EGOLF_LATITUDE, EGolfBills.EGOLF_LONGITUDE);

        if (meters < evMaxMeterDistance2Standort) {
            try {
                final String soCEGolfBills = fetchSoC(EGolfBills.EGOLF_VIN);
                //System.out.println("soCEGolfBills: " + soCEGolfBills);

                if (soCEGolfBills != null) {
                    String[] split = soCEGolfBills.split(";");
                    if (split.length == 3) {
                        final String createdAt = split[0].substring(CREATED_AT.length());
                        final Instant createdAtInstant = DateTimeHelper.parse(createdAt);
                        final long secondsSinceCreated = DateTimeHelper.getSecondsSince(createdAtInstant);
                        final String SoC = split[1].substring(SOC.length());
                        final String chargingStatus = split[2].substring(CHARGE_STATUS.length());

//                        if (secondsSinceCreated < valueMaxSecondsOld && (!"NA".equals(SoC))) {
                        if (!"NA".equals(SoC)) {
                            final int SoCint = Integer.valueOf(SoC).intValue();
                            result.add(new EvMitLaufendenLadevorgang() {
                                @Override
                                public StammdatenFahrzeug getStammdatenFahrzeug() {
                                    return EGolfBills.getStammdatenEGolfBills();
                                }

                                @Override
                                public Instant getZeitpunktLadevorgangStart() {
                                    return createdAtInstant;
                                }

                                @Override
                                public int getSoC() {
                                    return SoCint;
                                }

                                @Override
                                public double getDistanzZumGegebenenStandort() {
                                    return round(meters, 1);
                                }

                                @Override
                                public Optional<Instant> getZeitpunktKabelEingesteckt() {
                                    return Optional.empty();
                                }

                                @Override
                                public Optional<Integer> getLadestromAmpereL1() {
                                    return Optional.empty();
                                }

                                @Override
                                public Optional<Integer> getLadestromAmpereL2() {
                                    return Optional.empty();
                                }

                                @Override
                                public Optional<Integer> getLadestromAmpereL3() {
                                    return Optional.empty();
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("getChargingEVsNearGeolocation Error: " + e.getMessage());
            }
        }

        return result;
    }

    private String fetchSoC(String vin) {

        String urlString = EVSOC_URL + vin;

        List<String> result = new ArrayList<>();
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "text/plain");
            con.setUseCaches(false);

            String line = null;

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream responseStream = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                while (reader.ready()) {
                    String read = reader.readLine();
                    result.add(read);
                    line = read;
                }
                reader.close();
            } else {
                System.err.println("getChargerApiCall - Response-Code: " + responseCode);
            }
            con.disconnect();
            return line;
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (ProtocolException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
