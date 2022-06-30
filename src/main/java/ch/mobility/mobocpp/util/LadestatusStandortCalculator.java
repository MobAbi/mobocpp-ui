package ch.mobility.mobocpp.util;

import ch.mobility.mobocpp.stammdaten.StammdatenAccessor;
import ch.mobility.mobocpp.stammdaten.StammdatenStandort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

//    private static final long SLEEP_INTERVALL = 30000L;
//    private boolean run = true;
    private List<LadestatusStandortBerechnungsergebnis> berechnungsergebnis = new ArrayList<>();

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

//        while (run) {
//
//            final List<LadestatusStandortBerechnungsergebnis> neuesBerechnungsergebnis = new ArrayList<>();
//            for (StammdatenStandort stammdatenStandort : StammdatenAccessor.get().getStandorte()) {
//                neuesBerechnungsergebnis.add(berechneFuerStandort(stammdatenStandort));
//                if (!run) {
//                    break;
//                }
//            }
//            synchronized (berechnungsergebnis) {
//                berechnungsergebnis.clear();
//                berechnungsergebnis.addAll(neuesBerechnungsergebnis);
//            }
//            final long calculationTimeMS = System.currentTimeMillis() - start;
//            System.out.println("LadestatusStandortCalculator Dauer: " + calculationTimeMS);
//            try {
//                Thread.sleep(SLEEP_INTERVALL);
//            } catch (InterruptedException e) {}
//        }
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
//        final List<LadestationMitLaufendenLadevorgang> ladestationenMitLaufendenLadevorgang =
//                ermittleLadestationenMitLaufendenLadevorgang(stammdatenStandort);
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

        if (ladestationenMitLaufendenLadevorgang.isEmpty()) {
//            System.out.println("Am Standort " + stammdatenStandort.getStandortId() +
//                    " hat keine der Ladestationen einen laufenden Ladevorgang.");
        } else if (evsMitLaufendenLadevorgang.isEmpty()) {
//            System.out.println("Im Umkreis von " + evMaxMeterDistance2Standort + " Metern von Standort "  +
//                    stammdatenStandort.getStandortId() + " sind keine Mobility-EV's mit laufendem Ladevorgang ermittelt worden");
        } else if (evsMitLaufendenLadevorgang.size() > ladestationenMitLaufendenLadevorgang.size()) {

            System.out.println("Mehr Mobility-EV's als CS am Laden!");

        } else if (ladestationenMitLaufendenLadevorgang.size() == 1 && evsMitLaufendenLadevorgang.size() == 1) {

            System.out.println("Eine CS und EV am Laden => Exakter Match: EV " +
                    evsMitLaufendenLadevorgang.get(0).getStammdatenFahrzeug().getVin() + " laedt an CS " +
                    ladestationenMitLaufendenLadevorgang.get(0).getStammdatenLadestation().getLadestationId());

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
        };
    }

//    private List<LadestationMitLaufendenLadevorgang> ermittleLadestationenMitLaufendenLadevorgang(
//            StammdatenStandort stammdatenStandort) {
//        return new ArrayList<>();
//    }

    private List<EvMitLaufendenLadevorgang> ermittleEVsMitLaufendenLadevorgang(
            StammdatenStandort stammdatenStandort,
            double evMaxMeterDistance2Standort) {
        System.out.println("TODO: ermittleEVsMitLaufendenLadevorgang(Standort=" + stammdatenStandort.getStandortId() +
                ", evMaxMeterDistance2Standort=" + evMaxMeterDistance2Standort + ")");
        return new ArrayList<>();
    }

//    public void stop(String cause) {
//        this.run = false;
//    }
}
