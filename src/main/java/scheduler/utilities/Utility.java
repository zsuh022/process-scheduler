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

    // Fisher-Yates 2D XOR shuffle variant without creating high overhead of using list
    public static void shuffle2DArray(int[][] array, int rows, int columns) {
        int randomRowIndex;
        int randomColumnIndex;

        for (int rowIndex = rows; rowIndex > 0; rowIndex--) {
            for (int columnIndex = columns; columnIndex > 0; columnIndex--) {
                randomRowIndex = random.nextInt(rowIndex + 1);
                randomColumnIndex = random.nextInt(columnIndex + 1);

                if (randomRowIndex)
            }

            if (randomIndex != index) {
                array[randomIndex] ^= array[index];
                array[index] ^= array[randomIndex];
                array[randomIndex] ^= array[index];
            }
        }
    }
}
