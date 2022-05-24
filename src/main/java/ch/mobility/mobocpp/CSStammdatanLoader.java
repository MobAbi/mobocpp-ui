package ch.mobility.mobocpp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSStammdatanLoader {

    private static String FILENAME = "stammdatenladestationen.csv";
    protected static String DELIMITER = ";";
    protected static String HEADER_LINE = "ID;NAME;PLZ;ORT;LONGITUDE;LATITUDE";
    private static int NUMBER_OF_COLUMNS = HEADER_LINE.split(DELIMITER).length;

    public CSStammdatanLoadResult load()  {

        try {
            final Statistics statistics = new Statistics();
            final Stream<String> lines = getStringStream();

            final List<CSStammdaten> result = lines//
                    .filter(e -> accept(e, statistics))//
                    .map(e -> line2CSStammdaten(e))//
                    .collect(Collectors.toList());

            if (!statistics.getBadLines().isEmpty()) {
                System.err.println("Ungueltige Zeilen in '" + FILENAME + "': " + statistics.getBadLines());
            }

            final List<String> duplicates = removeDuplicates(result);
            if (!duplicates.isEmpty()) {
                System.err.println("Doppelte Zeilen zu folgenden IDs entfernt: " + duplicates.stream().collect(Collectors.joining(",")));
            }

//        System.out.println("CSStammdaten: " + result);
//        System.out.println("Eingelesen: " + statistics.getTotal() + ", OK: " + statistics.getGood() + ", Fehlerhaft: " + statistics.getBad());

            lines.close();
            return new CSStammdatanLoadResult() {
                @Override
                public List<Integer> getUngueltigeZeilen() {
                    return statistics.getBadLines();
                }

                @Override
                public List<CSStammdaten> getCSStammdaten() {
                    return result;
                }

                @Override
                public List<String> getIDsWithDuplicates() {
                    return duplicates;
                }
            };
        } catch (Exception e) {
            Throwable t = getLastThrowable(e);
            logError("[" + this.getClass().getSimpleName() + "] Fehler beim Laden der '" + FILENAME + "': " +
                    t.getClass().getSimpleName() + (t.getMessage() == null ? "": " => " + t.getMessage()));
//            e.printStackTrace(System.err);
            return new CSStammdatanLoadResult() {
                @Override
                public List<Integer> getUngueltigeZeilen() {
                    return new ArrayList<>();
                }

                @Override
                public List<CSStammdaten> getCSStammdaten() {
                    return new ArrayList<>();
                }

                @Override
                public List<String> getIDsWithDuplicates() {
                    return new ArrayList<>();
                }
            };
        }
    }

    // VisibleForTesting
    protected Stream<String> getStringStream() throws IOException, URISyntaxException {
        final Stream<String> lines;

        final InputStream resourceAsStream = CSStammdatanLoader.class.getResourceAsStream("/" + FILENAME);
        if (resourceAsStream != null) { // via JAR file
            final List<String> list = new ArrayList<>();
            final Scanner scan = new Scanner(resourceAsStream);
            while (scan.hasNextLine()) {
                list.add(scan.nextLine());
            }
            lines = list.stream();
            resourceAsStream.close();
        } else {
            final Path path = Paths.get(getClass().getClassLoader().getResource(FILENAME).toURI());
            lines = Files.lines(path);
        }
        return lines;
    }

    private Throwable getLastThrowable(Throwable t) {
        if (t != null && t.getCause() != null) {
           return getLastThrowable(t.getCause());
        }
        return t;
    }

    private boolean accept(String line, Statistics statistics) {
        if (line == null || line.length() == 0) {
            statistics.bad();
            return false;
        }
        if (HEADER_LINE.equalsIgnoreCase(line.trim())) {
            statistics.setHasHeader();
            return false;
        }
        if (line.split(DELIMITER).length != NUMBER_OF_COLUMNS) {
            statistics.bad();
            return false;
        }
        statistics.good();
        return true;
    }

    private CSStammdaten line2CSStammdaten(String line) {
        final String[] split = line.split(DELIMITER);
        final String id = remQT(split[0]);
        final String name = remQT(split[1]);
        final String plz = remQT(split[2]);
        final String ort = remQT(split[3]);
        final String lat = remQT(split[4]);
        final String lon = remQT(split[5]);
        return CSStammdaten.of(id, name, plz, ort, lat, lon);
    }

    // Remove Quotation marks
    private String remQT(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        if (result.startsWith("\"")) {
            result = result.substring(1, result.length()-1);
        }
        if (result.endsWith("\"")) {
            result = result.substring(0, result.length()-1);
        }
        return result;
    }

    private static class Statistics {
        private boolean hasHeader = false;
        private int good = 0;
        private int bad = 0;
        private List<Integer> badLines = new ArrayList<>();

        void good() {
            good++;
        }

        void bad() {
            bad++;
            // Zeilennummer der fehlerhaften merken:
            badLines.add(Integer.valueOf(getTotal() + (hasHeader ? 1 : 0)));
        }

        int getGood() {
            return good;
        }

        int getBad() {
            return bad;
        }

        int getTotal() {
            return getGood() + getBad();
        }

        List<Integer> getBadLines() {
            return badLines;
        }

        void setHasHeader() {
            this.hasHeader = true;
        }
    }

    private List<String> removeDuplicates(List<CSStammdaten> list) {
        Map<String, List<Integer>> map = new HashMap<>();
        for (int index = 0; index < list.size(); index++) {
            final CSStammdaten csStammdaten = list.get(index);
            List<Integer> indexesForId = map.get(csStammdaten.getId());
            if (indexesForId == null) {
                indexesForId = new ArrayList<>();
                map.put(csStammdaten.getId(), indexesForId);
            }
            indexesForId.add(Integer.valueOf(index));
        }
        final List<Integer> indexesToRemove = new ArrayList<>();
        final List<String> idsWithDuplicates = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                idsWithDuplicates.add(entry.getKey());
                // Alle indexes ausser den ersten merken
                indexesToRemove.addAll(entry.getValue().stream().skip(1L).collect(Collectors.toList()));
            }
        }
        Collections.sort(indexesToRemove);
        Collections.reverse(indexesToRemove);
        for (Integer index : indexesToRemove) {
            list.remove(index.intValue());
        }
        Collections.sort(idsWithDuplicates);
        return idsWithDuplicates;
    }

    private void logError(String msg) {
        System.err.println(msg);
    }
}
