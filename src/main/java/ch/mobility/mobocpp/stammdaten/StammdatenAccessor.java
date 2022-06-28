package ch.mobility.mobocpp.stammdaten;

import java.util.ArrayList;
import java.util.List;

public class StammdatenAccessor {

    private static final String UNBEKANNT = "Unbekannt";
    private static final String EGOLF_VIN = "WVWZZZAUZLW908093";
    private static final String EGOLF_KENNZEICHEN = "NW-19561";
    private static final String EGOLF_HERSTELLER = "VW";
    private static final String EGOLF_MODELL = "e-Golf 300";

    private static StammdatenAccessor INSTANCE = null;

    public static synchronized StammdatenAccessor get() {
        if (INSTANCE == null) {
            INSTANCE = new StammdatenLoader().load();
        }
        return INSTANCE;
    }

    private final List<StammdatenStandort> standorteList;
    private final List<Integer> standorteUngueltigeZeilen;
    private final List<String> standorteDuplicateIds;

    private final List<StammdatenLadestation> ladestationenList;
    private final List<Integer> ladestationenUngueltigeZeilen;
    private final List<String> ladestationenDuplicateIds;

    private final List<StammdatenFahrzeug> fahrzeugeList;

    StammdatenAccessor(
            List<StammdatenStandort> standorteList,
            List<Integer> standorteUngueltigeZeilen,
            List<String> standorteDuplicateIds,
            List<StammdatenLadestation> ladestationenList,
            List<Integer> ladestationenUngueltigeZeilen,
            List<String> ladestationenDuplicateIds) {
        this.standorteList = standorteList;
        this.standorteUngueltigeZeilen = standorteUngueltigeZeilen;
        this.standorteDuplicateIds = standorteDuplicateIds;
        this.ladestationenList = ladestationenList;
        this.ladestationenUngueltigeZeilen = ladestationenUngueltigeZeilen;
        this.ladestationenDuplicateIds = ladestationenDuplicateIds;
        this.fahrzeugeList = new ArrayList<>();
        this.fahrzeugeList.add(StammdatenFahrzeug.of(EGOLF_VIN, EGOLF_KENNZEICHEN, EGOLF_HERSTELLER, EGOLF_MODELL));
    }

    public List<StammdatenStandort> getStandorte() {
        return standorteList;
    }
    
    public List<StammdatenLadestation> getLadestationen() {
        return ladestationenList;
    }

    public List<Integer> getLadestationenUngueltigeZeilen() {
        return ladestationenUngueltigeZeilen;
    }

    public List<String> getLadestationenDuplicateIds() {
        return ladestationenDuplicateIds;
    }

    public StammdatenLadestation getStammdatenLadestationById(String ladestationId) {
        if (ladestationId == null) throw new IllegalStateException("ladestationId is null");

        for (StammdatenLadestation stammdatenLadestation : ladestationenList) {
            if (stammdatenLadestation.getLadestationId().equals(ladestationId)) {
                return stammdatenLadestation;
            }
        }
        throw new IllegalStateException("StammdatenLadestation not found: " + ladestationId);
    }

    public StammdatenStandort getStammdatenStandortById(String standortId) {
        if (standortId == null) throw new IllegalStateException("standortId is null");

        for (StammdatenStandort stammdatenStandort : standorteList) {
            if (stammdatenStandort.getStandortId().equals(standortId)) {
                return stammdatenStandort;
            }
        }
        return createDummyStammdatenStandort(standortId);
    }

    public StammdatenStandort getStammdatenStandortForLadestation(StammdatenLadestation stammdatenLadestation) {
        if (stammdatenLadestation == null) throw new IllegalStateException("stammdatenLadestation is null");

        for (StammdatenStandort stammdatenStandort : standorteList) {
            if (stammdatenStandort.getStandortId().equals(stammdatenLadestation.getStandortId())) {
                return stammdatenStandort;
            }
        }
        return createDummyStammdatenStandort(stammdatenLadestation.getStandortId());
    }

    private StammdatenStandort createDummyStammdatenStandort(String standortId) {
        return StammdatenStandort.of(standortId, UNBEKANNT, UNBEKANNT, UNBEKANNT, UNBEKANNT, "NA", UNBEKANNT, UNBEKANNT);
    }
}
