import java.util.concurrent.ThreadLocalRandom;

/**
 * Really just exists to be able to have a random number generator throughout the project
 */
public class RNGesus {
    public static int randInRange(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
