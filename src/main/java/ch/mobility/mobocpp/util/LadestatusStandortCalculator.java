package ch.mobility.mobocpp.util;

import ch.mobility.mobocpp.stammdaten.StammdatenAccessor;
import ch.mobility.mobocpp.stammdaten.StammdatenLadestation;
import ch.mobility.mobocpp.stammdaten.StammdatenStandort;

import java.time.Instant;
import java.util.*;

public class LadestatusStandortCalculator implements LadestationMitLaufendenLadevorgangListener {

    private static LadestatusStandortCalculator INSTANCE = null;

    public static synchronized LadestatusStandortCalculator get() {
        if (INSTANCE == null) {
            INSTANCE = new LadestatusStandortCalculator();
        }
        return INSTANCE;
    }

    private LadestatusStandortCalculator() {}

    private static double evMaxMeterDistance2Standort = 50;

    private List<LadestatusStandortBerechnungsergebnis> berechnungsergebnis = new ArrayList<>();
    private EVAccessor evAccessor = new EVAccessor();

    public void notify(List<LadestationMitLaufendenLadevorgang> ladestationenMitLaufendenLadevorgang) {
        final long start = System.currentTimeMillis();

        final Map<StammdatenStandort, List<LadestationMitLaufendenLadevorgang>> map =
                toMap(ladestationenMitLaufendenLadevorgang);

        final List<LadestatusStandortBerechnungsergebnis> neuesBerechnungsergebnis = new ArrayList<>();
        for (Map.Entry<StammdatenStandort, List<LadestationMitLaufendenLadevorgang>> entry : map.entrySet()) {
            neuesBerechnungsergebnis.add(berechneFuerStandort(entry.getKey(), entry.getValue()));
        }

        synchronized (berechnungsergebnis) {
            berechnungsergebnis.clear();
            berechnungsergebnis.addAll(neuesBerechnungsergebnis);
        }
        final long calculationTimeMS = System.currentTimeMillis() - start;
        System.out.println("LadestatusStandortCalculator Dauer: " + calculationTimeMS + ", Anzahl: " + neuesBerechnungsergebnis.size());
    }

    public Optional<EvMitLaufendenLadevorgang> getEvMitLaufendenLadevorgangForLadestation(String ladestationId) {
        final StammdatenLadestation stammdatenLadestation = StammdatenAccessor.get().getStammdatenLadestationById(ladestationId);
        final LadestatusStandortBerechnungsergebnis berechnungFuerStandort = getBerechnungFuerStandort(stammdatenLadestation.getStandortId());
        if (berechnungFuerStandort != null) {
            return berechnungFuerStandort.getEvMitLaufendenLadevorgangForLadestation(ladestationId);
        }
        return Optional.empty();
    }

    private LadestatusStandortBerechnungsergebnis getBerechnungFuerStandort(String standortId) {
        for (LadestatusStandortBerechnungsergebnis ladestatusStandortBerechnungsergebnis : berechnungsergebnis) {
            if (ladestatusStandortBerechnungsergebnis.getStammdatenStandort().getStandortId().equals(standortId)) {
                return ladestatusStandortBerechnungsergebnis;
            }
        }
        return null;
    }

    private Map<StammdatenStandort, List<LadestationMitLaufendenLadevorgang>> toMap(
            List<LadestationMitLaufendenLadevorgang> ladestationenMitLaufendenLadevorgang) {
        final Map<StammdatenStandort, List<LadestationMitLaufendenLadevorgang>> map = new HashMap<>();

        for (LadestationMitLaufendenLadevorgang ladestationMitLaufendenLadevorgang : ladestationenMitLaufendenLadevorgang) {
            final StammdatenStandort stammdatenStandort =
                    StammdatenAccessor.get().getStammdatenStandortForLadestation(
                            ladestationMitLaufendenLadevorgang.getStammdatenLadestation());
            List<LadestationMitLaufendenLadevorgang> list = map.get(stammdatenStandort);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(ladestationMitLaufendenLadevorgang);
            map.put(stammdatenStandort, list);
        }

        return map;
    }

    private LadestatusStandortBerechnungsergebnis berechneFuerStandort(
            StammdatenStandort stammdatenStandort,
            List<LadestationMitLaufendenLadevorgang> ladestationenMitLaufendenLadevorgang) {
        final List<EvMitLaufendenLadevorgang> evsMitLaufendenLadevorgang;
        if (ladestationenMitLaufendenLadevorgang.isEmpty()) {
            evsMitLaufendenLadevorgang = new ArrayList<>();
        } else {
            evsMitLaufendenLadevorgang =
                    ermittleEVsMitLaufendenLadevorgang(stammdatenStandort, evMaxMeterDistance2Standort);
        }

        System.out.println(evMaxMeterDistance2Standort + " Meter um Standort " + stammdatenStandort.getStandortId() +
                ": CS mit Ladevorgang=" + ladestationenMitLaufendenLadevorgang.size() +
                ", EV mit Ladevorgang=" + evsMitLaufendenLadevorgang.size());
        final Map<String, EvMitLaufendenLadevorgang> ladestation2EvMap = new HashMap<>();

        if (ladestationenMitLaufendenLadevorgang.isEmpty()) {
//            System.out.println("Am Standort " + stammdatenStandort.getStandortId() +
//                    " hat keine der Ladestationen einen laufenden Ladevorgang.");
        } else if (evsMitLaufendenLadevorgang.isEmpty()) {
//            System.out.println("Im Umkreis von " + evMaxMeterDistance2Standort + " Metern von Standort "  +
//                    stammdatenStandort.getStandortId() + " sind keine Mobility-EV's mit laufendem Ladevorgang ermittelt worden");
        } else if (evsMitLaufendenLadevorgang.size() > ladestationenMitLaufendenLadevorgang.size()) {

            System.out.println("Mehr Mobility-EV's als CS am Laden!");

        } else if (ladestationenMitLaufendenLadevorgang.size() == 1 && evsMitLaufendenLadevorgang.size() == 1) {

            final Instant startTimeCS = ladestationenMitLaufendenLadevorgang.get(0).getZeitpunktLadevorgangStart();
            final Instant startTimeEV = evsMitLaufendenLadevorgang.get(0).getZeitpunktLadevorgangStart();
            if (startTimeEV.isAfter(startTimeCS)) {
                System.out.println("Eine CS und EV am Laden => Exakter Match: EV " +
                        evsMitLaufendenLadevorgang.get(0).getStammdatenFahrzeug().getVin() + " laedt an CS " +
                        ladestationenMitLaufendenLadevorgang.get(0).getStammdatenLadestation().getLadestationId() +
                        ", Distanz=" + evsMitLaufendenLadevorgang.get(0).getDistanzZumGegebenenStandort() + "m");

                ladestation2EvMap.put(
                        ladestationenMitLaufendenLadevorgang.get(0).getStammdatenLadestation().getLadestationId(),
                        evsMitLaufendenLadevorgang.get(0));
            } else {
                System.out.println("Eine CS und EV am Laden, aber Ladevorgang-Startzeit vom CS " + startTimeCS + " ist nach dem vom EV " + startTimeEV);
            }

        } else if (ladestationenMitLaufendenLadevorgang.size() == evsMitLaufendenLadevorgang.size()) {

            System.out.println("Gleich viele CS und EV's am Laden. TODO: Logik fuer Zuordnung implementieren!");

        } else if (ladestationenMitLaufendenLadevorgang.size() > evsMitLaufendenLadevorgang.size()) {

            System.out.println("Mehr CS als EV's am Laden. TODO: Logik fuer Zuordnung implementieren!");

        }

        return new LadestatusStandortBerechnungsergebnis() {
            @Override
            public Instant getZeitpunktErgebnisErstellt() {
                return Instant.now();
            }

            @Override
            public StammdatenStandort getStammdatenStandort() {
                return stammdatenStandort;
            }

            @Override
            public List<LadestationMitLaufendenLadevorgang> getLadestationenMitLaufendenLadevorgang() {
                return ladestationenMitLaufendenLadevorgang;
            }

            @Override
            public List<EvMitLaufendenLadevorgang> getEvMitLaufendenLadevorgang() {
                return evsMitLaufendenLadevorgang;
            }

            @Override
            public Optional<EvMitLaufendenLadevorgang> getEvMitLaufendenLadevorgangForLadestation(String ladestationId) {
                return Optional.ofNullable(ladestation2EvMap.get(ladestationId));
            }
        };
    }

    private List<EvMitLaufendenLadevorgang> ermittleEVsMitLaufendenLadevorgang(
            StammdatenStandort stammdatenStandort,
            double evMaxMeterDistance2Standort) {

        final List<EvMitLaufendenLadevorgang> result = new ArrayList<>();
        if (stammdatenStandort.hasGeolocation()) {
            final double longituade = Double.valueOf(stammdatenStandort.getLongitude()).doubleValue();
            final double latitude = Double.valueOf(stammdatenStandort.getLatitude()).doubleValue();
            result.addAll(evAccessor.getChargingEVsNearGeolocation(longituade, latitude, evMaxMeterDistance2Standort, 300));
        }
        System.out.println("ermittleEVsMitLaufendenLadevorgang(Standort=" + stammdatenStandort.getStandortId() +
                ", evMaxMeterDistance2Standort=" + evMaxMeterDistance2Standort + "): " + result);
        return result;
    }
}
