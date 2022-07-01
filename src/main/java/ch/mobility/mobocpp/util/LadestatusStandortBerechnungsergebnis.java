package ch.mobility.mobocpp.util;

import ch.mobility.mobocpp.stammdaten.StammdatenStandort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LadestatusStandortBerechnungsergebnis {

    /**
     * @return Zeitstempel wann dieses Ergebnis berechnet wurde.
     */
    Instant getZeitpunktErgebnisErstellt();

    /**
     * @return Stammdaten vom Standort
     */
    StammdatenStandort getStammdatenStandort();

    /**
     * @return List mit Ladestationen vom Standort mit laufenden Ladevorgang
     */
    List<LadestationMitLaufendenLadevorgang> getLadestationenMitLaufendenLadevorgang();

    /**
     * @return List mit EV's in der Nähe Standort mit laufenden Ladevorgang
     */
    List<EvMitLaufendenLadevorgang> getEvMitLaufendenLadevorgang();

    /**
     *
     * @param ladestationId
     * @return
     */
    Optional<EvMitLaufendenLadevorgang> getEvMitLaufendenLadevorgangForLadestation(String ladestationId);
}
