package ch.mobility.mobocpp.stammdaten;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StammdatenLoaderTest {

    @Test
    public void testLeerMitHeader() {
        StammdatenLoader testee = createTestee(getEmptyStandorteList(true), getEmptyLadestationenList(true));
        StammdatenAccessor accessor = testee.load();
        Assert.assertTrue(accessor.getStandorte().size() == 0);
        Assert.assertTrue(accessor.getLadestationen().size() == 0);
    }

    @Test
    public void testLeerOhneHeader() {
        StammdatenLoader testee = createTestee(getEmptyStandorteList(false), getEmptyLadestationenList(false));
        StammdatenAccessor accessor = testee.load();
        Assert.assertTrue(accessor.getStandorte().size() == 0);
        Assert.assertTrue(accessor.getLadestationen().size() == 0);
    }

    @Test
    public void testLadestation2Gueltig2UngueligOhneQM() {
        final boolean qm = false;
        final List<String> standorteList = getEmptyStandorteList(false);
        standorteList.add(getStandortLine(qm, "StandortId1", "bezeichnung1", "strasse1",  "plz1", "ort1", "K1", "lon1", "lat1"));
        standorteList.add(getStandortLine(qm, "StandortId2", "bezeichnung2", "strasse2",  "plz2", "ort2", "K2", "lon2", "lat2"));

        final List<String> ladestationenList = getEmptyLadestationenList(false);
        ladestationenList.add(getLadestationLine(qm, "LadestationID1", "StandortId1"));
        ladestationenList.add("Invalid1");
        ladestationenList.add("");
        ladestationenList.add(getLadestationLine(qm, "LadestationID2", "StandortId2"));
        StammdatenLoader testee = createTestee(standorteList, ladestationenList);

        { // Ohne Header
            StammdatenAccessor accessor = testee.load();
            Assert.assertTrue(accessor.getLadestationen().size() == 2);
            Assert.assertTrue(accessor.getLadestationenUngueltigeZeilen().size() == 2);
            Assert.assertEquals("LadestationID1", accessor.getLadestationen().get(0).getLadestationId());
            Assert.assertEquals("StandortId2", accessor.getLadestationen().get(1).getStandortId());
        }

        { // Mit Header
            addLadestationHeader(ladestationenList);
            StammdatenAccessor accessor = testee.load();
            Assert.assertTrue(accessor.getLadestationen().size() == 2);
            Assert.assertTrue(accessor.getLadestationenUngueltigeZeilen().size() == 2);
            Assert.assertEquals("LadestationID2", accessor.getLadestationen().get(1).getLadestationId());
            Assert.assertEquals("StandortId1", accessor.getLadestationen().get(0).getStandortId());
        }
    }

    @Test
    public void testLadestation2Gueltig2UngueligMitQM() {
        final boolean qm = true;
        final List<String> standorteList = getEmptyStandorteList(false);
        standorteList.add(getStandortLine(qm, "StandortId1", "bezeichnung1", "strasse1",  "plz1", "ort1", "K1", "lon1", "lat1"));
        standorteList.add(getStandortLine(qm, "StandortId2", "bezeichnung2", "strasse2",  "plz2", "ort2", "K2", "lon2", "lat2"));

        final List<String> ladestationenList = getEmptyLadestationenList(false);
        ladestationenList.add(getLadestationLine(qm, "LadestationID1", "StandortId1"));
        ladestationenList.add("Invalid1");
        ladestationenList.add("");
        ladestationenList.add(getLadestationLine(qm, "LadestationID2", "StandortId2"));
        StammdatenLoader testee = createTestee(standorteList, ladestationenList);

        { // Ohne Header
            StammdatenAccessor accessor = testee.load();
            Assert.assertTrue(accessor.getLadestationen().size() == 2);
            Assert.assertTrue(accessor.getLadestationenUngueltigeZeilen().size() == 2);
        }

        { // Mit Header
            addLadestationHeader(ladestationenList);
            StammdatenAccessor accessor = testee.load();
            Assert.assertTrue(accessor.getLadestationen().size() == 2);
            Assert.assertTrue(accessor.getLadestationenUngueltigeZeilen().size() == 2);
        }
    }

    @Test
    public void testLadestationDuplicate() {
        final boolean qm = false;
        final List<String> standorteList = getEmptyStandorteList(false);
        standorteList.add(getStandortLine(qm, "keep", "bezeichnung1", "strasse1",  "plz1", "ort1", "K1", "lon1", "lat1"));
        standorteList.add(getStandortLine(qm, "name2", "bezeichnung2", "strasse2",  "plz2", "ort2", "K2", "lon2", "lat2"));
        standorteList.add(getStandortLine(qm, "name3", "bezeichnung2", "strasse2",  "plz2", "ort2", "K2", "lon2", "lat2"));

        final List<String> ladestationenList = getEmptyLadestationenList(false);
        ladestationenList.add(getLadestationLine(qm, "DUPLICATE1", "keep"));
        ladestationenList.add(getLadestationLine(qm, "ID2", "name2"));
        ladestationenList.add(getLadestationLine(qm, "DUPLICATE1", "removed"));
        ladestationenList.add(getLadestationLine(qm, "DUPLICATE2", "keep"));
        ladestationenList.add(getLadestationLine(qm, "DUPLICATE1", "removed"));
        ladestationenList.add(getLadestationLine(qm, "DUPLICATE2", "removed"));
        ladestationenList.add(getLadestationLine(qm, "ID3", "name3"));
        StammdatenLoader testee = createTestee(standorteList, ladestationenList);

        StammdatenAccessor accessor = testee.load();
        Assert.assertTrue(accessor.getLadestationen().size() == 4);
        Assert.assertTrue(accessor.getLadestationenUngueltigeZeilen().size() == 0);
        Assert.assertTrue(accessor.getLadestationenDuplicateIds().size() == 2);
        Assert.assertTrue(accessor.getLadestationenDuplicateIds().contains("DUPLICATE1"));
        Assert.assertTrue(accessor.getLadestationenDuplicateIds().contains("DUPLICATE2"));
    }

    @Test(expected = IllegalStateException.class)
    public void testIOExceptionCatchedAndThrownAsIllegalStateException() {
        StammdatenLoader testee = new StammdatenLoader() {
            @Override
            protected Stream<String> getStringStreamLadestationen() throws IOException, URISyntaxException {
                throw new IOException("Test");
            }
        };
        StammdatenAccessor accessor = testee.load();
        Assert.assertTrue(accessor.getLadestationen().size() == 0);
        Assert.assertTrue(accessor.getLadestationenUngueltigeZeilen().size() == 0);
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalStateExceptionWhenInconsistenData() {
        final boolean qm = false;
        final List<String> standorteList = getEmptyStandorteList(false);
        standorteList.add(getStandortLine(qm, "name2", "bezeichnung2", "strasse2",  "plz2", "ort2", "K2", "lon2", "lat2"));
        standorteList.add(getStandortLine(qm, "name3", "bezeichnung2", "strasse2",  "plz2", "ort2", "K2", "lon2", "lat2"));

        final List<String> ladestationenList = getEmptyLadestationenList(false);
        ladestationenList.add(getLadestationLine(qm, "ID1", "name1"));
        ladestationenList.add(getLadestationLine(qm, "ID2", "name2"));
        ladestationenList.add(getLadestationLine(qm, "ID3", "name3"));
        StammdatenLoader testee = createTestee(standorteList, ladestationenList);
        testee.load();
    }

    private StammdatenLoader createTestee(List<String> standortList, List<String> ladestationenList) {
        return new StammdatenLoader() {

            @Override
            protected Stream<String> getStringStreamStandorte() throws IOException, URISyntaxException {
                return standortList.stream();
            }

            @Override
            protected Stream<String> getStringStreamLadestationen() throws IOException, URISyntaxException {
                return ladestationenList.stream();
            }
        };
    }

    private List<String> getEmptyStandorteList(boolean withColumnHeader) {
        final List<String> result = new ArrayList<>();
        if (withColumnHeader) {
            result.add(StammdatenLoader.HEADER_LINE_STANDORT);
        }
        return result;
    }

    private List<String> getEmptyLadestationenList(boolean withColumnHeader) {
        final List<String> result = new ArrayList<>();
        if (withColumnHeader) {
            result.add(StammdatenLoader.HEADER_LINE_LADESTATIONEN);
        }
        return result;
    }

    private void addLadestationHeader(List<String> list) {
        list.add(0, StammdatenLoader.HEADER_LINE_LADESTATIONEN);
    }

    private static String getStandortLine(boolean withQM, String standortId, String bezeichnung, String strasse, String plz, String ort, String kanton, String lan, String lon) {
        return qm(withQM, standortId) + StammdatenLoader.DELIMITER +
               qm(withQM, bezeichnung) + StammdatenLoader.DELIMITER +
               qm(withQM, strasse) + StammdatenLoader.DELIMITER +
               qm(withQM, plz) + StammdatenLoader.DELIMITER +
               qm(withQM, ort) + StammdatenLoader.DELIMITER +
               qm(withQM, kanton) + StammdatenLoader.DELIMITER +
               qm(withQM, lan) + StammdatenLoader.DELIMITER +
               qm(withQM, lon) + StammdatenLoader.DELIMITER;
    }

    private static String getLadestationLine(boolean withQM, String ladestationId, String standortId) {
        return qm(withQM, ladestationId) + StammdatenLoader.DELIMITER +
                qm(withQM, standortId) + StammdatenLoader.DELIMITER;
    }

    private static String qm(boolean withQM, String value) {
        String result = value;
        if (withQM) {
            result = "\"" + result + "\"";
        }
        return result;
    }
}