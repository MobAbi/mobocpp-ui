package ch.mobility.mobocpp.util;

import org.junit.Assert;
import org.junit.Test;

public class GPSDistanceTest {

    @Test
    public void allNull() {
        double lt1 = 0.0;
        double ln1 = 0.0;
        double lt2 = 0.0;
        double ln2 = 0.0;

        double meters = GPSDistance.distance(lt1, ln1, lt2, ln2);
        Assert.assertEquals(0.0, meters, 0.0);
    }

    @Test
    public void test111Meters() {
        double lt1 = 46.974695882686206;
        double ln1 = 8.417824395239812;
        double lt2 = 46.97375740679195;
        double ln2 = 8.418358335965896;

        double meters = GPSDistance.distance(lt1, ln1, lt2, ln2);
        Assert.assertEquals(111.93581091091912, meters, 0.0);
    }

    @Test
    public void test24Meters() {
        double lt1 = 46.97470836909026;
        double ln1 = 8.418352134468194;
        double lt2 = 46.97478660699858;
        double ln2 = 8.418653882963037;

        double meters = GPSDistance.distance(lt1, ln1, lt2, ln2);
        Assert.assertEquals(24.48973669848643, meters, 0.0);
    }

    @Test
    public void test2Meters() {
        double lt1 = 46.958721936822826;
        double ln1 = 8.365988836591379;
        double lt2 = 46.95870225706422;
        double ln2 = 8.365987495486959;

        double meters = GPSDistance.distance(lt1, ln1, lt2, ln2);
        Assert.assertEquals(2.1895891843663375, meters, 0.0);
    }

    @Test
    public void test1Meter() {
        double lt1 = 46.974879028239876;
        double ln1 = 8.418219365130463;
        double lt2 = 46.974869877629025;
        double ln2 = 8.41822540010036;

        double meters = GPSDistance.distance(lt1, ln1, lt2, ln2);
        Assert.assertEquals(1.1192168068278106, meters, 0.0);
    }
}


