package com.csc483.assignment2.sorting;

import java.util.Arrays;
import java.util.Random;

/**
 * Empirical Benchmark: compares Insertion Sort, Merge Sort, Quick Sort, and Heap Sort
 * across five dataset types at four input sizes, including statistical analysis.
 *
 * <p>Each (algorithm, dataset, size) combination is run 5 times; mean and standard
 * deviation of wall-clock time are reported.
 *
 * @author [Your Name]
 * @version 1.0
 */
public class SortingBenchmark {

    private static final int[]    SIZES       = {100, 1_000, 10_000, 100_000};
    private static final int      RUNS        = 5;
    private static final int      WARMUP      = 3;
    private static final long     SEED        = 42L;
    private static final int      DISTINCT    = 10;  // For "many duplicates" dataset
    private static final double   NEAR_SORTED = 0.1; // 10% of elements are swapped

    // Dataset type labels
    private static final String[] TYPES = {
        "Random", "Sorted (Asc)", "Sorted (Desc)", "Nearly Sorted", "Many Duplicates"
    };

    // Algorithm names
    private static final String[] ALGOS = {"Insertion Sort", "Merge Sort", "Quick Sort", "Heap Sort"};

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("       SORTING ALGORITHMS EMPIRICAL COMPARISON");
        System.out.println("================================================================\n");

        // JVM warm-up to eliminate JIT-compilation bias
        System.out.println("Warming up JVM...");
        warmup();
        System.out.println("Warm-up complete.\n");

        // Benchmark each dataset type
        for (String dataType : TYPES) {
            System.out.println("================================================================");
            System.out.printf("  DATASET TYPE: %-20s%n", dataType.toUpperCase());
            System.out.println("================================================================");
            printHeader();

            for (int size : SIZES) {
                runForDataType(dataType, size);
            }
            System.out.println();
        }

        System.out.println("================================================================");
        System.out.println("CONCLUSIONS:");
        System.out.println("  - Insertion Sort is O(n) on sorted/nearly-sorted data — very fast");
        System.out.println("  - Merge Sort is consistently O(n log n) regardless of data order");
        System.out.println("  - Quick Sort (median-of-3) is fastest on random data on average");
        System.out.println("  - Heap Sort has guaranteed O(n log n) but poor cache locality");
        System.out.println("  - For n < 1000, Insertion Sort is competitive with all others");
        System.out.println("================================================================");
    }

    // ─────────────────────────────────────────────────────────────
    // Core Benchmark Runner
    // ─────────────────────────────────────────────────────────────

    private static void runForDataType(String dataType, int size) {
        int[] baseData = generateDataset(dataType, size);

        String[] algoNames = ALGOS;
        for (String algoName : algoNames) {
            double[] times = new double[RUNS];
            long totalComparisons = 0, totalSwaps = 0;

            for (int run = 0; run < RUNS; run++) {
                int[] data = Arrays.copyOf(baseData, baseData.length); // fresh copy each run
                SortingAlgorithms.Stats stats = new SortingAlgorithms.Stats();

                long start = System.nanoTime();
                sort(algoName, data, stats);
                long elapsed = System.nanoTime() - start;

                times[run]      = elapsed / 1_000_000.0; // ns → ms
                totalComparisons += stats.comparisons;
                totalSwaps       += stats.swaps;

                // Verify sort correctness on first run
                if (run == 0) assertSorted(data, algoName, dataType, size);
            }

            double mean   = mean(times);
            double stdDev = stdDev(times, mean);
            long avgComparisons = totalComparisons / RUNS;
            long avgSwaps       = totalSwaps / RUNS;

            System.out.printf("  %-14s | n=%-7d | %8.3f ms ± %6.3f | %,12d cmps | %,12d swaps%n",
                algoName, size, mean, stdDev, avgComparisons, avgSwaps);
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────
    // Sort Dispatcher
    // ─────────────────────────────────────────────────────────────

    private static void sort(String algoName, int[] data, SortingAlgorithms.Stats stats) {
        switch (algoName) {
            case "Insertion Sort": SortingAlgorithms.insertionSort(data, stats); break;
            case "Merge Sort":     SortingAlgorithms.mergeSort(data, stats);     break;
            case "Quick Sort":     SortingAlgorithms.quickSort(data, stats);     break;
            case "Heap Sort":      SortingAlgorithms.heapSort(data, stats);      break;
            default: throw new IllegalArgumentException("Unknown algorithm: " + algoName);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Dataset Generators
    // ─────────────────────────────────────────────────────────────

    /**
     * Generates a dataset of the given type and size.
     */
    private static int[] generateDataset(String type, int size) {
        Random rand = new Random(SEED);
        int[] data  = new int[size];

        switch (type) {
            case "Random":
                for (int i = 0; i < size; i++) data[i] = rand.nextInt(size * 10);
                break;

            case "Sorted (Asc)":
                for (int i = 0; i < size; i++) data[i] = i;
                break;

            case "Sorted (Desc)":
                for (int i = 0; i < size; i++) data[i] = size - i;
                break;

            case "Nearly Sorted":
                for (int i = 0; i < size; i++) data[i] = i; // Start sorted
                int swapCount = (int) (size * NEAR_SORTED);  // Swap 10% of positions
                for (int k = 0; k < swapCount; k++) {
                    int i = rand.nextInt(size);
                    int j = rand.nextInt(size);
                    int tmp = data[i]; data[i] = data[j]; data[j] = tmp;
                }
                break;

            case "Many Duplicates":
                for (int i = 0; i < size; i++) data[i] = rand.nextInt(DISTINCT); // Only 10 distinct values
                break;

            default:
                throw new IllegalArgumentException("Unknown data type: " + type);
        }
        return data;
    }

    // ─────────────────────────────────────────────────────────────
    // Statistical Utilities
    // ─────────────────────────────────────────────────────────────

    private static double mean(double[] values) {
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    private static double stdDev(double[] values, double mean) {
        double sumSq = 0;
        for (double v : values) sumSq += (v - mean) * (v - mean);
        return Math.sqrt(sumSq / values.length);
    }

    /**
     * Performs a two-sample t-test between two runtime arrays.
     * Returns the t-statistic. |t| > 2 suggests significant difference at α ≈ 0.05.
     */
    public static double tTest(double[] a, double[] b) {
        double meanA = mean(a), meanB = mean(b);
        double stdA  = stdDev(a, meanA), stdB = stdDev(b, meanB);
        int nA = a.length, nB = b.length;
        double pooledStdErr = Math.sqrt((stdA * stdA / nA) + (stdB * stdB / nB));
        return (pooledStdErr == 0) ? 0 : (meanA - meanB) / pooledStdErr;
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private static void printHeader() {
        System.out.printf("  %-14s | %-9s | %17s | %19s | %19s%n",
            "Algorithm", "Input n", "Time (ms ± σ)", "Comparisons (avg)", "Swaps (avg)");
        System.out.println("  " + "-".repeat(100));
    }

    private static void assertSorted(int[] arr, String algo, String type, int size) {
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] > arr[i + 1]) {
                throw new RuntimeException(
                    String.format("SORT FAILURE: %s on %s n=%d at index %d", algo, type, size, i));
            }
        }
    }

    private static void warmup() {
        int[] sample = new int[5000];
        Random r = new Random();
        for (int run = 0; run < WARMUP; run++) {
            for (int i = 0; i < sample.length; i++) sample[i] = r.nextInt();
            SortingAlgorithms.insertionSort(Arrays.copyOf(sample, sample.length), null);
            SortingAlgorithms.mergeSort(Arrays.copyOf(sample, sample.length), null);
            SortingAlgorithms.quickSort(Arrays.copyOf(sample, sample.length), null);
            SortingAlgorithms.heapSort(Arrays.copyOf(sample, sample.length), null);
        }
    }
}
