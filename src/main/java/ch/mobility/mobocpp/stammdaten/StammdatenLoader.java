package ch.mobility.mobocpp.stammdaten;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StammdatenLoader {

    private static String FILENAME_STANDORTE = "stammdatenstandorte.csv";
    private static String FILENAME_LADESTATIONEN = "stammdatenladestationen.csv";
    protected static String DELIMITER = ";";
    protected static String HEADER_LINE_STANDORT = "STANDORT-ID;BEZEICHNUNG;STRASSE;PLZ;ORT;KANTON;LONGITUDE;LATITUDE";
    protected static String HEADER_LINE_LADESTATIONEN = "LADESTATION-ID;STANDORT-ID;BEZEICHNUNG";
    private static int NUMBER_OF_COLUMNS_STANDORT = HEADER_LINE_STANDORT.split(DELIMITER).length;
    private static int MIN_NUMBER_OF_COLUMNS_LADESTATION = HEADER_LINE_LADESTATIONEN.split(DELIMITER).length - 1;
    private static int MAX_NUMBER_OF_COLUMNS_LADESTATION = HEADER_LINE_LADESTATIONEN.split(DELIMITER).length;

    StammdatenAccessor load()  {

        final Statistics statisticsStandorte = new Statistics();
        final Statistics statisticsLadestationen = new Statistics();

        final List<StammdatenStandort> standorteList;
        final List<StammdatenLadestation> ladestationList;

        final List<String> duplicatesStandorte;
        final List<String> duplicatesLadestationen;

    try {
            {
                final Stream<String> linesStandorte = getStringStreamStandorte();
                standorteList = linesStandorte//
                        .filter(e -> acceptStandortLine(e, statisticsStandorte))//
                        .map(e -> line2StammdatenStandort(e))//
                        .collect(Collectors.toList());

                if (!statisticsStandorte.getBadLines().isEmpty()) {
                    System.err.println("Ungueltige Zeilen in '" + FILENAME_STANDORTE + "': " + statisticsStandorte.getBadLines());
                }

                duplicatesStandorte = removeStandortDuplicates(standorteList);
                if (!duplicatesStandorte.isEmpty()) {
                    System.err.println("Doppelte Zeilen zu folgenden Standort-IDs entfernt: " +
                            duplicatesStandorte.stream().collect(Collectors.joining(",")));
                }
                linesStandorte.close();
            }

            {
                final Stream<String> linesLadestationen = getStringStreamLadestationen();
                ladestationList = linesLadestationen//
                        .filter(e -> acceptLadestationLine(e, statisticsLadestationen))//
                        .map(e -> line2StammdatenLadestation(e))//
                        .collect(Collectors.toList());

                if (!statisticsLadestationen.getBadLines().isEmpty()) {
                    System.err.println("Ungueltige Zeilen in '" + FILENAME_LADESTATIONEN + "': " + statisticsLadestationen.getBadLines());
                }

                duplicatesLadestationen = removeLadestationDuplicates(ladestationList);
                if (!duplicatesLadestationen.isEmpty()) {
                    System.err.println("Doppelte Zeilen zu folgenden Ladestation-IDs entfernt: " +
                            duplicatesLadestationen.stream().collect(Collectors.joining(",")));
                }
                linesLadestationen.close();
            }
        } catch (Exception e) {
            Throwable t = getLastThrowable(e);
            String msg = "[" + this.getClass().getSimpleName() + "] Fehler beim Laden der '" + FILENAME_STANDORTE +
                    " und " + FILENAME_LADESTATIONEN + "': " +
                    t.getClass().getSimpleName() + (t.getMessage() == null ? "": " => " + t.getMessage());
            throw new IllegalStateException(msg);
//        logError(msg);
//            return new StammdatenAccessor(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        final List<String> standortIds = standorteList.stream().map(e -> e.getStandortId()).collect(Collectors.toList());
        final Set<String> missingStandortIds = new HashSet<>();
        for (StammdatenLadestation stammdatenLadestation : ladestationList) {
            if (!standortIds.contains(stammdatenLadestation.getStandortId())) {
                missingStandortIds.add(stammdatenLadestation.getStandortId());
            }
        }
        if (!missingStandortIds.isEmpty()) {
            throw new IllegalStateException("Stammdaten: Folgende StandortIds existieren nicht, werden aber von Ladestationen referenziert: " +
                    missingStandortIds.stream().collect(Collectors.joining(",")));
        }
//        System.out.println("CSStammdaten: " + ladestationList);
//        System.out.println("Eingelesen: " + statisticsLadestationen.getTotal() + ", OK: " + statisticsLadestationen.getGood() + ", Fehlerhaft: " + statisticsLadestationen.getBad());

        return new StammdatenAccessor(//
                standorteList, statisticsStandorte.getBadLines(), duplicatesStandorte,//
                ladestationList, statisticsLadestationen.getBadLines(), duplicatesLadestationen);
    }

    // VisibleForTesting
    protected Stream<String> getStringStreamStandorte() throws IOException, URISyntaxException {
        final Stream<String> lines;

        final InputStream resourceAsStream = StammdatenLoader.class.getResourceAsStream("/" + FILENAME_STANDORTE);
        if (resourceAsStream != null) { // via JAR file
            final List<String> list = new ArrayList<>();
            final Scanner scan = new Scanner(resourceAsStream);
            while (scan.hasNextLine()) {
                list.add(scan.nextLine());
            }
            lines = list.stream();
            resourceAsStream.close();
        } else {
            final Path path = Paths.get(getClass().getClassLoader().getResource(FILENAME_STANDORTE).toURI());
            lines = Files.lines(path);
        }
        return lines;
    }

    // VisibleForTesting
    protected Stream<String> getStringStreamLadestationen() throws IOException, URISyntaxException {
        final Stream<String> lines;

        final InputStream resourceAsStream = StammdatenLoader.class.getResourceAsStream("/" + FILENAME_LADESTATIONEN);
        if (resourceAsStream != null) { // via JAR file
            final List<String> list = new ArrayList<>();
            final Scanner scan = new Scanner(resourceAsStream);
            while (scan.hasNextLine()) {
                list.add(scan.nextLine());
            }
            lines = list.stream();
            resourceAsStream.close();
        } else {
            final Path path = Paths.get(getClass().getClassLoader().getResource(FILENAME_LADESTATIONEN).toURI());
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

    private boolean acceptStandortLine(String line, Statistics statistics) {
        if (line == null || line.length() == 0) {
            statistics.bad();
            return false;
        }
        if (HEADER_LINE_STANDORT.equalsIgnoreCase(line.trim())) {
            statistics.setHasHeader();
            return false;
        }
        if (line.split(DELIMITER).length != NUMBER_OF_COLUMNS_STANDORT) {
            statistics.bad();
            return false;
        }
        statistics.good();
        return true;
    }

    private boolean acceptLadestationLine(String line, Statistics statistics) {
        if (line == null || line.length() == 0) {
            statistics.bad();
            return false;
        }
        if (HEADER_LINE_LADESTATIONEN.equalsIgnoreCase(line.trim())) {
            statistics.setHasHeader();
            return false;
        }
        final int count = line.split(DELIMITER).length;
        if (count < MIN_NUMBER_OF_COLUMNS_LADESTATION || count > MAX_NUMBER_OF_COLUMNS_LADESTATION) {
            statistics.bad();
            return false;
        }
        statistics.good();
        return true;
    }

    private StammdatenStandort line2StammdatenStandort(String line) {
        final String[] split = line.split(DELIMITER);
        final String standortId = remQT(split[0]);
        final String bezeichnung = remQT(split[1]);
        final String strasse = remQT(split[2]);
        final String plz = remQT(split[3]);
        final String ort = remQT(split[4]);
        final String kanton = remQT(split[5]);
        final String lat = remQT(split[6]);
        final String lon = remQT(split[7]);
        return StammdatenStandort.of(standortId, bezeichnung, strasse, plz, ort, kanton, lat, lon);
    }

    private StammdatenLadestation line2StammdatenLadestation(String line) {
        final String[] split = line.split(DELIMITER);
        final String ladestationId = remQT(split[0]);
        final String standortId = remQT(split[1]);
        String bezeichnung = "";
        if (split.length == MAX_NUMBER_OF_COLUMNS_LADESTATION) {
            bezeichnung = remQT(split[2]);
        }
        return StammdatenLadestation.of(ladestationId, standortId, bezeichnung);
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

    private List<String> removeStandortDuplicates(List<StammdatenStandort> list) {
        Map<String, List<Integer>> map = new HashMap<>();
        for (int index = 0; index < list.size(); index++) {
            final StammdatenStandort stammdatenStandort = list.get(index);
            List<Integer> indexesForId = map.get(stammdatenStandort.getStandortId());
            if (indexesForId == null) {
                indexesForId = new ArrayList<>();
                map.put(stammdatenStandort.getStandortId(), indexesForId);
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

    private List<String> removeLadestationDuplicates(List<StammdatenLadestation> list) {
        Map<String, List<Integer>> map = new HashMap<>();
        for (int index = 0; index < list.size(); index++) {
            final StammdatenLadestation stammdatenLadestation = list.get(index);
            List<Integer> indexesForId = map.get(stammdatenLadestation.getLadestationId());
            if (indexesForId == null) {
                indexesForId = new ArrayList<>();
                map.put(stammdatenLadestation.getLadestationId(), indexesForId);
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
