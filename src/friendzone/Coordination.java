package friendzone;

import friendzone.UserAcc;

/**
 * This class is used to implement "FIND USERS NEAR ME" feature.<br>
 * Class name may be prone to changing.<br>
 * The implemented method may be merged with other class in future.<br>
 *
 * @author CHONG WEI HAO
 */
public class Coordination {

    /**
     * Calculate distance between two user.
     *
     * Unit used:<br>
     * https://astro.unl.edu/naap/motion1/tc_units.html<br>
     * Formula source:<br>
     * https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude<br>
     * Formula evaluation site:<br>
     * https://gps-coordinates.org/distance-between-coordinates.php<br>
     * http://boulter.com/gps/distance/?from=4.187922+100.667832&to=3.139983+101.675308&units=k<br>
     * https://www.nhc.noaa.gov/gccalc.shtml<br>
     *
     * @param user1 logged in user
     * @param user2 target user
     * @param unit "m" for metre, "km" for kilometre
     * @return distance in unit
     */
    public static double calDist(UserAcc user1, UserAcc user2, String unit) {
        final int WorldRadius = 6371; // Radius of the earth

        double latDistance = Math.toRadians(user2.latitude - user1.latitude);
        double lonDistance = Math.toRadians(user2.longitude - user1.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                   + Math.cos(Math.toRadians(user1.latitude)) * Math.cos(Math.toRadians(user2.latitude))
                     * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = 0;
        if(unit.equalsIgnoreCase("m")) {
            distance = WorldRadius * c * 1000; // convert to meters
        }
        else if(unit.equalsIgnoreCase("km")) {
            distance = WorldRadius * c; // convert to km
        }
        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);

    }

    public static String calDist(double lat1, double lon1, double lat2, double lon2) {
        final int WorldRadius = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                   + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                     * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = 0;
        
        distance = WorldRadius * c; // convert to km

        distance = Math.pow(distance, 2);

        return String.format("%.2f km", Math.sqrt(distance));
    }

    /**
     * This is method overloading of calDist(UserAcc user1, UserAcc user2,
     * String unit).<br>
     *
     * @param user1 logged in user
     * @param user2 target user
     * @return distance between 2 user in metre
     */
    public static double calDist(UserAcc user1, UserAcc user2) {
        return calDist(user1, user2, "km");
    }

}
