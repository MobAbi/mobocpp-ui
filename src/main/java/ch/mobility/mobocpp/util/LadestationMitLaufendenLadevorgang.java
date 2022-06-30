package ch.mobility.mobocpp.util;

import ch.mobility.mobocpp.stammdaten.StammdatenLadestation;

import java.time.Instant;
import java.util.Optional;

public interface LadestationMitLaufendenLadevorgang {
    /**
     * @return Stammdaten der Ladestation
     */
    StammdatenLadestation getStammdatenLadestation();

    /**
     * @return UTC-Zeitpunkt als der Ladevorgang gestartet hat
     */
    Instant getZeitpunktLadevorgangStart();

    /**
     * @return Falls verfügbar: Der Zeitpunkt als das Ladekabel verbunden wurde.
     */
    Optional<Instant> getZeitpunktLadekabelEingesteckt();

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
