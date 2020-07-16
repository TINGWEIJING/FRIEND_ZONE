package friendzone;

public class Debug {

    // mode true means debug mode is on
    private static boolean mode = false;

    /**
     * Similar to System.out.println().<br>
     * With "DEBUG: " prefix.
     * 
     * @param msg debug message
     */
    public static void println(Object msg) {
        if(mode) {
            System.out.println("DEBUG: " + msg);
        }
    }

    /**
     * Similar to System.out.print().<br>
     * Without "DEBUG: " prefix.
     * 
     * @param msg debug message
     */
    public static void print(Object msg) {
        if(mode) {
            System.out.print(msg);
        }
    }

    /**
     *
     * @param mode set false to turn off debug mode
     */
    public static void debugMode(boolean mode) {
        Debug.mode = mode;
    }

}
