package com.csc483.assignment2.sorting;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for all four sorting algorithm implementations.
 * Each algorithm is verified against Arrays.sort() as a reference.
 *
 * @author [Your Name]
 */
@DisplayName("Sorting Algorithm Tests")
class SortingAlgorithmsTest {

    // ─────────────────────────────────────────
    // Parameterised: run every test on every algorithm
    // ─────────────────────────────────────────

    enum Algorithm { INSERTION, MERGE, QUICK, HEAP }

    static Stream<Algorithm> allAlgorithms() {
        return Stream.of(Algorithm.values());
    }

    private static void sort(Algorithm algo, int[] arr) {
        switch (algo) {
            case INSERTION: SortingAlgorithms.insertionSort(arr, null); break;
            case MERGE:     SortingAlgorithms.mergeSort(arr, null);     break;
            case QUICK:     SortingAlgorithms.quickSort(arr, null);     break;
            case HEAP:      SortingAlgorithms.heapSort(arr, null);      break;
        }
    }

    private static void assertSorted(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            assertTrue(arr[i] <= arr[i + 1],
                "Array not sorted at index " + i + ": " + arr[i] + " > " + arr[i + 1]);
        }
    }

    // ─────────────────────────────────────────
    // Edge Cases
    // ─────────────────────────────────────────

    @ParameterizedTest(name = "{0} — null input")
    @MethodSource("allAlgorithms")
    @DisplayName("Should handle null gracefully")
    void testNullInput(Algorithm algo) {
        assertDoesNotThrow(() -> sort(algo, null));
    }

    @ParameterizedTest(name = "{0} — empty array")
    @MethodSource("allAlgorithms")
    @DisplayName("Should handle empty array")
    void testEmptyArray(Algorithm algo) {
        int[] arr = {};
        assertDoesNotThrow(() -> sort(algo, arr));
        assertEquals(0, arr.length);
    }

    @ParameterizedTest(name = "{0} — single element")
    @MethodSource("allAlgorithms")
    @DisplayName("Should handle single-element array")
    void testSingleElement(Algorithm algo) {
        int[] arr = {42};
        sort(algo, arr);
        assertArrayEquals(new int[]{42}, arr);
    }

    @ParameterizedTest(name = "{0} — two elements")
    @MethodSource("allAlgorithms")
    @DisplayName("Should correctly sort two elements")
    void testTwoElements(Algorithm algo) {
        int[] arr = {9, 3};
        sort(algo, arr);
        assertArrayEquals(new int[]{3, 9}, arr);
    }

    @ParameterizedTest(name = "{0} — all duplicates")
    @MethodSource("allAlgorithms")
    @DisplayName("Should handle all identical elements")
    void testAllDuplicates(Algorithm algo) {
        int[] arr = {5, 5, 5, 5, 5};
        sort(algo, arr);
        assertArrayEquals(new int[]{5, 5, 5, 5, 5}, arr);
    }

    @ParameterizedTest(name = "{0} — already sorted")
    @MethodSource("allAlgorithms")
    @DisplayName("Should handle already sorted input")
    void testAlreadySorted(Algorithm algo) {
        int[] arr = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] expected = arr.clone();
        sort(algo, arr);
        assertArrayEquals(expected, arr);
    }

    @ParameterizedTest(name = "{0} — reverse sorted")
    @MethodSource("allAlgorithms")
    @DisplayName("Should correctly sort reverse-sorted input")
    void testReverseSorted(Algorithm algo) {
        int[] arr      = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        int[] expected = {1,  2, 3, 4, 5, 6, 7, 8, 9, 10};
        sort(algo, arr);
        assertArrayEquals(expected, arr);
    }

    @ParameterizedTest(name = "{0} — negative numbers")
    @MethodSource("allAlgorithms")
    @DisplayName("Should correctly sort arrays with negative numbers")
    void testNegativeNumbers(Algorithm algo) {
        int[] arr = {-3, 5, -1, 0, 2, -7, 4};
        int[] ref = arr.clone();
        Arrays.sort(ref);
        sort(algo, arr);
        assertArrayEquals(ref, arr);
    }

    // ─────────────────────────────────────────
    // Correctness Against Arrays.sort Reference
    // ─────────────────────────────────────────

    @ParameterizedTest(name = "{0} — small random (n=50)")
    @MethodSource("allAlgorithms")
    @DisplayName("Should sort small random array correctly")
    void testSmallRandom(Algorithm algo) {
        Random rand = new Random(99);
        int[] arr = rand.ints(50, -1000, 1000).toArray();
        int[] ref = arr.clone();
        Arrays.sort(ref);
        sort(algo, arr);
        assertArrayEquals(ref, arr);
    }

    @ParameterizedTest(name = "{0} — medium random (n=1000)")
    @MethodSource("allAlgorithms")
    @DisplayName("Should sort medium random array correctly")
    void testMediumRandom(Algorithm algo) {
        Random rand = new Random(7);
        int[] arr = rand.ints(1000, 0, 100_000).toArray();
        int[] ref = arr.clone();
        Arrays.sort(ref);
        sort(algo, arr);
        assertArrayEquals(ref, arr);
    }

    @ParameterizedTest(name = "{0} — large random (n=10000)")
    @MethodSource("allAlgorithms")
    @DisplayName("Should sort large random array correctly")
    void testLargeRandom(Algorithm algo) {
        Random rand = new Random(13);
        int[] arr = rand.ints(10_000, -500_000, 500_000).toArray();
        int[] ref = arr.clone();
        Arrays.sort(ref);
        sort(algo, arr);
        assertArrayEquals(ref, arr);
    }

    @ParameterizedTest(name = "{0} — many duplicates (10 distinct values)")
    @MethodSource("allAlgorithms")
    @DisplayName("Should handle many-duplicate input correctly")
    void testManyDuplicates(Algorithm algo) {
        Random rand = new Random(22);
        int[] arr = new int[500];
        for (int i = 0; i < arr.length; i++) arr[i] = rand.nextInt(10);
        int[] ref = arr.clone();
        Arrays.sort(ref);
        sort(algo, arr);
        assertArrayEquals(ref, arr);
    }

    // ─────────────────────────────────────────
    // Stats Counter Tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("Stats comparisons should be > 0 for non-trivial input")
    void testStatsCounterCounts() {
        int[] arr = {5, 3, 1, 4, 2};
        SortingAlgorithms.Stats stats = new SortingAlgorithms.Stats();
        SortingAlgorithms.insertionSort(arr, stats);
        assertTrue(stats.comparisons > 0, "Expected comparisons > 0");
        assertTrue(stats.swaps > 0, "Expected swaps > 0");
    }

    @Test
    @DisplayName("Insertion Sort on sorted input should have O(n) comparisons")
    void testInsertionSortBestCaseComparisons() {
        int n = 1000;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = i;
        SortingAlgorithms.Stats stats = new SortingAlgorithms.Stats();
        SortingAlgorithms.insertionSort(arr, stats);
        // Best case: exactly n-1 comparisons (one per outer loop iteration)
        assertTrue(stats.comparisons <= n,
            "Expected at most n comparisons on sorted input, got: " + stats.comparisons);
    }

    @Test
    @DisplayName("Merge Sort comparisons should be approximately n*log2(n)")
    void testMergeSortComparisonBound() {
        int n = 10_000;
        int[] arr = new Random(1).ints(n).toArray();
        SortingAlgorithms.Stats stats = new SortingAlgorithms.Stats();
        SortingAlgorithms.mergeSort(arr, stats);
        double upperBound = n * (Math.log(n) / Math.log(2)) * 1.5; // Allow 50% margin
        assertTrue(stats.comparisons <= upperBound,
            "Comparisons " + stats.comparisons + " exceeded expected bound of " + upperBound);
    }
}
