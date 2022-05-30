package ch.mobility.mobocpp.util;

public class GPSDistance {

    // Credits: https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude (6)
    private static final double r2d = 180.0D / 3.141592653589793D;
    private static final double d2r = 3.141592653589793D / 180.0D;
    private static final double d2km = 111189.57696D * r2d;

    /**
     * Calculate the distance in Meters.
     * @param lt1 Latitude of 1st location
     * @param ln1 Longitude of 1st location
     * @param lt2 Latitude of 2nd location
     * @param ln2 Longitude of 2nd location
     * @return
     */
    public static double distance(double lt1, double ln1, double lt2, double ln2) {
        double x = lt1 * d2r;
        double y = lt2 * d2r;
        return Math.acos( Math.sin(x) * Math.sin(y) + Math.cos(x) * Math.cos(y) * Math.cos(d2r * (ln1 - ln2))) * d2km;
    }
}
