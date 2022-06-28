package ch.mobility.mobocpp;

import ch.mobility.mobocpp.rs.model.CarSoC;
import ch.mobility.mobocpp.rs.model.Chargestatus;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class CarSoCSingletonTest {

    @Test
    public void test() throws InterruptedException {
        CarSoCSingleton.getInstance().add(new CarSoC(Instant.now(), "vin1", "17", Chargestatus.CHARGING_UNKOWN.getValue()));
        CarSoCSingleton.getInstance().add(new CarSoC(Instant.now(), "vin2", "99", Chargestatus.CHARGING_UNKOWN.getValue()));
        Thread.sleep(1);
        CarSoCSingleton.getInstance().add(new CarSoC(Instant.now(), "vin1", "19", Chargestatus.CHARGING_UNKOWN.getValue()));
        Thread.sleep(1);
        CarSoCSingleton.getInstance().add(new CarSoC(Instant.now(), "vin1", "NA", Chargestatus.CHARGING_UNKOWN.getValue()));
        Thread.sleep(1);
        Instant instant1 = Instant.now();
        int soc1 = 22;
        CarSoCSingleton.getInstance().add(new CarSoC(instant1, "vin1", "" + soc1, Chargestatus.CHARGING_UNKOWN.getValue()));
        Instant instant2 = Instant.now();
        int soc2 = 88;
        CarSoCSingleton.getInstance().add(new CarSoC(instant2, "vin2", "" + soc2, Chargestatus.CHARGING_UNKOWN.getValue()));
        Thread.sleep(1);
        CarSoCSingleton.getInstance().add(new CarSoC(Instant.now(), "vin1", "NA", Chargestatus.CHARGING_UNKOWN.getValue()));

        CarSoC vin1 = CarSoCSingleton.getInstance().getLatestWithSoCValue("vin1");
        assertEquals(vin1.getCreatedAt(), instant1);
        assertEquals(vin1.getSoc(), soc1);

        CarSoC vin2 = CarSoCSingleton.getInstance().getLatestWithSoCValue("vin2");
        assertEquals(vin2.getCreatedAt(), instant2);
        assertEquals(vin2.getSoc(), soc2);

        CarSoC unknown = CarSoCSingleton.getInstance().getLatestWithSoCValue("unknown");
        assertNull(unknown);
    }

}