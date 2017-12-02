import java.util.concurrent.ThreadLocalRandom;

public class RNGesus {
    public static int randInRange(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
