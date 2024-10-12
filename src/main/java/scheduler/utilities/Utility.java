package scheduler.utilities;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.Random;

public class Utility {
    private static final Random random = new Random();

    private static float cpuUsage;

    private static int ramUsage;

    public static int getRandomInteger(int minimum, int maximum) {
        return random.nextInt((maximum - minimum) + 1) + minimum;
    }

    public static double getRandomDouble(double minimum, double maximum) {
        return random.nextDouble() * (maximum - minimum) + minimum;
    }

    public static double getRandomPercentage(double minimum, double maximum) {
        return getRandomDouble(minimum, maximum);
    }

    public static boolean getRandomBoolean() {
        return random.nextBoolean();
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

    public static int getRamUsage() {
        Runtime runtime = Runtime.getRuntime();

        long ramUsageRaw = runtime.totalMemory() - runtime.freeMemory();

        ramUsage = (int) (ramUsageRaw / 1024 / 1024);

        return ramUsage;
    }

    public static float getCpuUsage() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        cpuUsage = (osBean == null) ? -1.0f : (float) (osBean.getCpuLoad() * 100.0);

        if (Float.isNaN(cpuUsage)) {
            return 0.0f;
        }

        return cpuUsage;
    }

    // TODO
    public static void displayCpuAndRamUsage() {
        System.out.println("\n CPU and RAM Usage:");
        System.out.printf("  %-25s %.3f%%%n", "CPU Usage:", cpuUsage);
        System.out.printf("  %-25s %dMB%n", "RAM Usage:", ramUsage);
    }

    public static int[] getRandomRgb() {
        return new int[]{getRandomInteger(0, 255), getRandomInteger(0, 255), getRandomInteger(0, 255)};
    }
}
