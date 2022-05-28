package ch.mobility.mobocpp.stammdaten;

import java.util.List;

public class StammdatenAccessor {

    private static StammdatenAccessor INSTANCE = null;

    public static synchronized StammdatenAccessor get() {
        if (INSTANCE == null) {
            INSTANCE = new CSStammdatanLoader().load();
        }
        return INSTANCE;
    }

    private final List<CSStammdaten> stammdatenList;
    private final List<Integer> ungueltigeZeilen;
    private final List<String> idsWithDuplicates;

    StammdatenAccessor(
            List<CSStammdaten> stammdatenList,
            List<Integer> ungueltigeZeilen,
            List<String> idsWithDuplicates) {
        this.stammdatenList = stammdatenList;
        this.ungueltigeZeilen = ungueltigeZeilen;
        this.idsWithDuplicates = idsWithDuplicates;
    }

    public List<CSStammdaten> getStammdatenList() {
        return stammdatenList;
    }

    public List<Integer> getUngueltigeZeilen() {
        return ungueltigeZeilen;
    }

    public List<String> getIdsWithDuplicates() {
        return idsWithDuplicates;
    }

    public CSStammdaten forId(String id) {
        for (CSStammdaten csStammdaten : stammdatenList) {
            if (csStammdaten.getId().equals(id)) {
                return csStammdaten;
            }
        }
        return null;
    }
}
