package ch.mobility.mobocpp;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CSStammdatanLoaderTest {

    @Test
    public void testLeerMitHeader() {
        CSStammdatanLoader testee = createTestee(getList(true));
        CSStammdatanLoadResult load = testee.load();
        Assert.assertTrue(load.getCSStammdaten().size() == 0);
    }

    @Test
    public void testLeerOhneHeader() {
        CSStammdatanLoader testee = createTestee(getList(false));
        CSStammdatanLoadResult load = testee.load();
        Assert.assertTrue(load.getCSStammdaten().size() == 0);
    }

    @Test
    public void test2Gueltig2UngueligOhneQM() {
        final boolean qm = false;
        final List<String> list = getList(false);
        list.add(getLine(qm, "ID1", "name1", "plz1", "ort1", "lon1", "lat1"));
        list.add("Invalid1");
        list.add("");
        list.add(getLine(qm, "ID2", "name2", "plz2", "ort2", "lon2", "lat2"));
        CSStammdatanLoader testee = createTestee(list);

        { // Ohne Header
            CSStammdatanLoadResult load = testee.load();
            Assert.assertTrue(load.getCSStammdaten().size() == 2);
            Assert.assertTrue(load.getUngueltigeZeilen().size() == 2);
            Assert.assertEquals("ID1", load.getCSStammdaten().get(0).getId());
            Assert.assertEquals("name2", load.getCSStammdaten().get(1).getName());
        }

        { // Mit Header
            addHeader(list);
            CSStammdatanLoadResult load = testee.load();
            Assert.assertTrue(load.getCSStammdaten().size() == 2);
            Assert.assertTrue(load.getUngueltigeZeilen().size() == 2);
            Assert.assertEquals("lon1", load.getCSStammdaten().get(0).getLongitude());
            Assert.assertEquals("lat2", load.getCSStammdaten().get(1).getLatitude());
        }
    }

    @Test
    public void test2Gueltig2UngueligMitQM() {
        final boolean qm = true;
        final List<String> list = getList(false);
        list.add(getLine(qm, "ID1", "name1", "plz1", "ort1", "lon1", "lat1"));
        list.add("Invalid1");
        list.add("");
        list.add(getLine(qm, "ID2", "name2", "plz2", "ort2", "lon2", "lat2"));
        CSStammdatanLoader testee = createTestee(list);

        { // Ohne Header
            CSStammdatanLoadResult load = testee.load();
            Assert.assertTrue(load.getCSStammdaten().size() == 2);
            Assert.assertTrue(load.getUngueltigeZeilen().size() == 2);
        }

        { // Mit Header
            addHeader(list);
            CSStammdatanLoadResult load = testee.load();
            Assert.assertTrue(load.getCSStammdaten().size() == 2);
            Assert.assertTrue(load.getUngueltigeZeilen().size() == 2);
        }
    }

    @Test
    public void testDuplicate() {
        final boolean qm = false;
        final List<String> list = getList(false);
        list.add(getLine(qm, "DUPLICATE1", "keep", "plz1", "ort1", "lon1", "lat1"));
        list.add(getLine(qm, "ID2", "name2", "plz2", "ort2", "lon2", "lat2"));
        list.add(getLine(qm, "DUPLICATE1", "removed", "plzD2", "ortD2", "lonD2", "latD2"));
        list.add(getLine(qm, "DUPLICATE2", "keep", "plz1", "ort1", "lon1", "lat1"));
        list.add(getLine(qm, "DUPLICATE1", "removed", "plzD3", "ortD3", "lonD3", "latD3"));
        list.add(getLine(qm, "DUPLICATE2", "removed", "plzD4", "ortD4", "lonD4", "latD4"));
        list.add(getLine(qm, "ID3", "name3", "plz3", "ort3", "lon3", "lat3"));
        CSStammdatanLoader testee = createTestee(list);

        CSStammdatanLoadResult load = testee.load();
        Assert.assertTrue(load.getCSStammdaten().size() == 4);
        Assert.assertTrue(load.getUngueltigeZeilen().size() == 0);
        Assert.assertTrue(load.getIDsWithDuplicates().size() == 2);
        Assert.assertTrue(load.getIDsWithDuplicates().contains("DUPLICATE1"));
        Assert.assertTrue(load.getIDsWithDuplicates().contains("DUPLICATE2"));
    }

    @Test
    public void test2Exception() {
        CSStammdatanLoader testee = new CSStammdatanLoader() {
            @Override
            protected Stream<String> getStringStream() throws IOException, URISyntaxException {
                throw new IOException("Test");
            }
        };
        CSStammdatanLoadResult load = testee.load();
        Assert.assertTrue(load.getCSStammdaten().size() == 0);
        Assert.assertTrue(load.getUngueltigeZeilen().size() == 0);
    }

    private CSStammdatanLoader createTestee(List<String> list) {
        return new CSStammdatanLoader() {
            @Override
            protected Stream<String> getStringStream() throws IOException, URISyntaxException {
                return list.stream();
            }
        };
    }

    private List<String> getList(boolean withColumnHeader) {
        final List<String> result = new ArrayList<>();
        if (withColumnHeader) {
            result.add(CSStammdatanLoader.HEADER_LINE);
        }
        return result;
    }

    private void addHeader(List<String> list) {
        list.add(0, CSStammdatanLoader.HEADER_LINE);
    }

    private static String getLine(boolean withQM, String id, String name, String plz, String ort, String lan, String lon) {
        return qm(withQM, id) + CSStammdatanLoader.DELIMITER +
               qm(withQM, name) + CSStammdatanLoader.DELIMITER +
               qm(withQM, plz) + CSStammdatanLoader.DELIMITER +
               qm(withQM, ort) + CSStammdatanLoader.DELIMITER +
               qm(withQM, lan) + CSStammdatanLoader.DELIMITER +
               qm(withQM, lon) + CSStammdatanLoader.DELIMITER;
    }

    private static String qm(boolean withQM, String value) {
        String result = value;
        if (withQM) {
            result = "\"" + result + "\"";
        }
        return result;
    }
}