package ch.mobility.mobocpp;

import ch.mobility.mobocpp.rs.model.CarSoC;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CarSoCSingleton {

    private static CarSoCSingleton instance = null;

    public static CarSoCSingleton getInstance() {
        if (instance == null) {
            instance = new CarSoCSingleton();
        }
        return instance;
    }

    private final List<CarSoC> list = new ArrayList<>();

    private CarSoCSingleton() {}

    public void add(CarSoC carSoC) {
        list.add(carSoC);
        System.out.println("Added " + list.size() + " entry: " + carSoC);
    }

    public CarSoC getLatestWithSoCValue(String vin) {
        List<CarSoC> result = list.stream()
                .filter(e -> e.getVin().equals(vin) && e.hasSoc())
                .collect(Collectors.toList());
        if (!result.isEmpty()) {
            return result.get(result.size()-1);
        }
        return null;
    }
}
