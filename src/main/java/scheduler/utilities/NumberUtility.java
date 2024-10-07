package scheduler.utilities;

import java.util.Random;

public class NumberUtility {
    private static final Random random = new Random();

    public static int getRandomInteger(int minimum, int maximum) {
        return random.nextInt((maximum - minimum) + 1) + minimum;
    }

    public static double getRandomDouble(double minimum, double maximum) {
        return random.nextDouble() * (maximum - minimum) + minimum;
    }

    public static double getRandomPercentage(double minimum, double maximum) {
        return getRandomDouble(minimum, maximum);
    }
}
