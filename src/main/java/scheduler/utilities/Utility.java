package scheduler.utilities;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.Random;

/**
 * This class contains utility methods that are used throughout the program such as generating random
 * integers, doubles, and booleans, rgb colours, shuffling 2D arrays, and displaying CPU and RAM usage.
 */
public class Utility {
    private static final Random random = new Random();

    private static float cpuUsage;

    private static int ramUsage;

    /**
     * Generates a random integer between the minimum and maximum values.
     *
     * @param minimum the minimum value
     * @param maximum the maximum value
     * @return the random integer
     */
    public static int getRandomInteger(int minimum, int maximum) {
        return random.nextInt((maximum - minimum) + 1) + minimum;
    }

    /**
     * Generates a random double between the minimum and maximum values.
     *
     * @param minimum the minimum value
     * @param maximum the maximum value
     * @return the random double
     */
    public static double getRandomDouble(double minimum, double maximum) {
        return random.nextDouble() * (maximum - minimum) + minimum;
    }

    /**
     * Generates a random percentage between the minimum and maximum values.
     *
     * @param minimum the minimum value
     * @param maximum the maximum value
     * @return the random percentage
     */
    public static double getRandomPercentage(double minimum, double maximum) {
        return getRandomDouble(minimum, maximum);
    }

    /**
     * Generates a random boolean.
     *
     * @return the random boolean
     */
    public static boolean getRandomBoolean() {
        return random.nextBoolean();
    }

    /**
     * Shuffles a 2D array using Fisher-Yates shuffling.
     * This is a Fisher-Yates 2D shuffling without creating high overhead of using list
     * 
     * @param array the 2D array
     * @param rows  the number of rows
     */
    public static void shuffle2DArray(int[][] array, int rows) {
        int randomRowIndex;

        for (int rowIndex = rows - 1; rowIndex > 0; rowIndex--) {
            randomRowIndex = random.nextInt(rowIndex + 1);

            swap2DArrayRows(array, rowIndex, randomRowIndex);
        }
    }

    /**
     * Swaps two rows in a 2D array by using a temporary array.
     *
     * @param array the 2D array
     * @param row1  the first row
     * @param row2  the second row
     */
    public static void swap2DArrayRows(int[][] array, int row1, int row2) {
        int[] temporaryArray = array[row1];

        array[row1] = array[row2];
        array[row2] = temporaryArray;
    }

    /**
     * Gets the RAM usage.
     *
     * @return the RAM usage
     */
    public static int getRamUsage() {
        Runtime runtime = Runtime.getRuntime();

        long ramUsageRaw = runtime.totalMemory() - runtime.freeMemory();

        ramUsage = (int) (ramUsageRaw / 1024 / 1024);

        return ramUsage;
    }

    /**
     * Gets the CPU usage.
     *
     * @return the CPU usage
     */
    public static float getCpuUsage() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        cpuUsage = (osBean == null) ? -1.0f : (float) (osBean.getCpuLoad() * 100.0);

        if (Float.isNaN(cpuUsage)) {
            return 0.0f;
        }

        return cpuUsage;
    }

    /**
     * Displays the CPU and RAM usage.
     */
    public static void displayCpuAndRamUsage() {
        System.out.println("\n CPU and RAM Usage:");
        System.out.printf("  %-25s %.3f%%%n", "CPU Usage:", cpuUsage);
        System.out.printf("  %-25s %dMB%n", "RAM Usage:", ramUsage);
    }

    /**
     * Generates a random RGB colour.
     *
     * @return the random RGB colour
     */
    public static int[] getRandomRgb() {
        return new int[]{getRandomInteger(0, 255), getRandomInteger(0, 255), getRandomInteger(0, 255)};
    }
}
