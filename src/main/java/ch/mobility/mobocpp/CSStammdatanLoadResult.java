package ch.mobility.mobocpp;

import java.util.List;

public interface CSStammdatanLoadResult {
    List<Integer> getUngueltigeZeilen();
    List<CSStammdaten> getCSStammdaten();
    List<String> getIDsWithDuplicates();
}
