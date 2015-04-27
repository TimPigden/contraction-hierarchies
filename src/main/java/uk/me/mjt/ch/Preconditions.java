
package uk.me.mjt.ch;


public class Preconditions {
    public static void checkNoneNull(Object... args) {
        for (int i=0 ; i<args.length ; i++) {
            if (args[i] == null) {
                throw new IllegalArgumentException("Argument index " + i + " was null?");
            }
        }
    }
    
    public static void checkTrue(boolean... args) {
        for (int i=0 ; i<args.length ; i++) {
            if (args[i] != true) {
                throw new IllegalArgumentException("Argument index " + i + " was false?");
            }
        }
    }

}
