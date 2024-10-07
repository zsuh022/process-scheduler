package scheduler.utilities;

import java.util.Random;

public class Utility {
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

    // Fisher-Yates 2D shuffling without creating high overhead of using list
    public static void shuffle2DArray(int[][] array, int rows) {
        int randomRowIndex;

        for (int rowIndex = rows - 1; rowIndex > 0; rowIndex--) {
            randomRowIndex = random.nextInt(rowIndex + 1);

            swap2DArrayRows(array, rowIndex, randomRowIndex);
        }
    }

    public static void swap2DArrayRows(int[][] array, int row1, int row2) {
        int[] temporaryArray = array[row1];

        array[row1] = array[row2];
        array[row2] = temporaryArray;
    }
}
