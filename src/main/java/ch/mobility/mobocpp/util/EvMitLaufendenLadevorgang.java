package ch.mobility.mobocpp.util;

import ch.mobility.mobocpp.stammdaten.StammdatenFahrzeug;

import java.time.Instant;
import java.util.Optional;

public interface EvMitLaufendenLadevorgang {
    /**
     * @return Stammdaten vom Fahrzeug
     */
    StammdatenFahrzeug getStammdatenFahrzeug();

    /**
     * @return UTC-Zeitpunkt als der Ladevorgang gestartet hat
     */
    Instant getZeitpunktLadevorgangStart();

    /**
     * @return Ladezustand in Prozent (SoC = 0-100%)
     */
    int getSoC();

    /**
     * @return Anzahl Meter die das EV from gegebenen Standort entfernt ist
     */
    double getDistanzZumGegebenenStandort();

    /**
     * @return Falls verfügbar: Der Zeitpunkt als das Ladekabel verbunden wurde.
     */
    Optional<Instant> getZeitpunktKabelEingesteckt();

    /**
     * @return Falls verfügbar: Der Ladestrom in Ampere für Phase L1
     */
    Optional<Integer> getLadestromAmpereL1();

    /**
     * @return Falls verfügbar: Der Ladestrom in Ampere für Phase L2
     */
    Optional<Integer> getLadestromAmpereL2();

    /**
     * @return Falls verfügbar: Der Ladestrom in Ampere für Phase L3
     */
    Optional<Integer> getLadestromAmpereL3();
}
