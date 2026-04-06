package com.csc483.assignment2.sorting;

/**
 * Implementations of four classical sorting algorithms for the empirical comparison.
 *
 * <p>Each method sorts the given int[] in ascending order IN-PLACE (except Merge Sort,
 * which uses O(n) auxiliary space). A {@link Stats} object is populated with
 * comparison and swap/assignment counts.
 *
 * <p>Complexity Reference:
 * <pre>
 * ┌─────────────────┬─────────────┬─────────────┬─────────────┬────────┬────────┬──────────┐
 * │ Algorithm       │ Best        │ Average     │ Worst       │ Space  │ Stable │ In-Place │
 * ├─────────────────┼─────────────┼─────────────┼─────────────┼────────┼────────┼──────────┤
 * │ Insertion Sort  │ O(n)        │ O(n²)       │ O(n²)       │ O(1)   │ Yes    │ Yes      │
 * │ Merge Sort      │ O(n log n)  │ O(n log n)  │ O(n log n)  │ O(n)   │ Yes    │ No       │
 * │ Quick Sort      │ O(n log n)  │ O(n log n)  │ O(n²)       │ O(log n)│ No    │ Yes      │
 * │ Heap Sort       │ O(n log n)  │ O(n log n)  │ O(n log n)  │ O(1)   │ No     │ Yes      │
 * └─────────────────┴─────────────┴─────────────┴─────────────┴────────┴────────┴──────────┘
 * </pre>
 *
 * @author [Your Name]
 * @version 1.0
 */
public class SortingAlgorithms {

    // ─────────────────────────────────────────────────────────────────────
    // Stats Collector
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Mutable container for counting comparisons and swaps/assignments.
     * Pass a fresh Stats object to each sort call.
     */
    public static class Stats {
        public long comparisons = 0;
        public long swaps       = 0; // For Merge Sort this counts element assignments

        @Override
        public String toString() {
            return String.format("comparisons=%,d  swaps/assignments=%,d", comparisons, swaps);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 1. Insertion Sort
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Insertion Sort — stable, in-place, O(n²) average, O(n) best (already sorted).
     *
     * <p><b>Behaviour on nearly-sorted data:</b> Extremely efficient because inner loop
     * exits quickly when the element is already in the right region. This makes it
     * competitive against O(n log n) algorithms for small or almost-sorted inputs.
     *
     * <p><b>Algorithm:</b><br>
     * For each position i from 1 to n-1, "insert" arr[i] into the sorted
     * sub-array arr[0..i-1] by shifting larger elements right.
     *
     * @param arr   Array to sort (modified in-place)
     * @param stats Stats collector (may be null)
     */
    public static void insertionSort(int[] arr, Stats stats) {
        if (arr == null || arr.length <= 1) return;
        if (stats == null) stats = new Stats();

        for (int i = 1; i < arr.length; i++) {
            int key = arr[i]; // Element to insert
            int j   = i - 1;

            // Shift elements of arr[0..i-1] that are greater than key one position right
            while (j >= 0 && arr[j] > key) {
                stats.comparisons++;
                arr[j + 1] = arr[j]; // shift right
                stats.swaps++;
                j--;
            }
            // Count the final comparison that exits the while (key <= arr[j] or j < 0)
            if (j >= 0) stats.comparisons++;

            arr[j + 1] = key; // Insert key at correct position
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2. Merge Sort
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Merge Sort — stable, NOT in-place, guaranteed O(n log n) all cases.
     *
     * <p><b>Strategy:</b> Divide-and-conquer. Recursively split into halves,
     * sort each half, then merge. Splitting creates a recursion tree of depth log₂(n),
     * and merging at each level costs O(n), giving O(n log n) overall.
     *
     * <p><b>Space:</b> O(n) auxiliary array for merging.
     *
     * @param arr   Array to sort (modified in-place)
     * @param stats Stats collector (may be null)
     */
    public static void mergeSort(int[] arr, Stats stats) {
        if (arr == null || arr.length <= 1) return;
        if (stats == null) stats = new Stats();
        mergeSortHelper(arr, 0, arr.length - 1, stats);
    }

    private static void mergeSortHelper(int[] arr, int left, int right, Stats stats) {
        if (left >= right) return;                        // Base case: 1 element
        int mid = left + (right - left) / 2;
        mergeSortHelper(arr, left, mid, stats);           // Sort left half
        mergeSortHelper(arr, mid + 1, right, stats);      // Sort right half
        merge(arr, left, mid, right, stats);              // Merge sorted halves
    }

    private static void merge(int[] arr, int left, int mid, int right, Stats stats) {
        int leftLen  = mid - left + 1;
        int rightLen = right - mid;

        // Temporary arrays
        int[] L = new int[leftLen];
        int[] R = new int[rightLen];

        System.arraycopy(arr, left,     L, 0, leftLen);
        System.arraycopy(arr, mid + 1,  R, 0, rightLen);

        int i = 0, j = 0, k = left;

        while (i < leftLen && j < rightLen) {
            stats.comparisons++;
            if (L[i] <= R[j]) {          // <= preserves stability
                arr[k++] = L[i++];
            } else {
                arr[k++] = R[j++];
            }
            stats.swaps++; // Count each assignment into arr
        }

        // Copy remaining elements
        while (i < leftLen) { arr[k++] = L[i++]; stats.swaps++; }
        while (j < rightLen) { arr[k++] = R[j++]; stats.swaps++; }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. Quick Sort  (3-way median-of-3 pivot selection)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Quick Sort — NOT stable, in-place, O(n log n) average, O(n²) worst case.
     *
     * <p><b>Pivot strategy:</b> Median-of-three (first, middle, last element).
     * This avoids the O(n²) worst case on already-sorted input that plagues naive
     * implementations using the first or last element as pivot.
     *
     * <p><b>Worst case:</b> Still O(n²) in theory (e.g., if all elements are equal
     * and a bad pivot is consistently chosen), but very unlikely with median-of-3.
     *
     * @param arr   Array to sort (modified in-place)
     * @param stats Stats collector (may be null)
     */
    public static void quickSort(int[] arr, Stats stats) {
        if (arr == null || arr.length <= 1) return;
        if (stats == null) stats = new Stats();
        quickSortHelper(arr, 0, arr.length - 1, stats);
    }

    private static void quickSortHelper(int[] arr, int low, int high, Stats stats) {
        if (low < high) {
            int pivotIdx = partition(arr, low, high, stats);
            quickSortHelper(arr, low,          pivotIdx - 1, stats);
            quickSortHelper(arr, pivotIdx + 1, high,         stats);
        }
    }

    /**
     * Partitions arr[low..high] around a median-of-3 pivot.
     * Returns the final index of the pivot.
     */
    private static int partition(int[] arr, int low, int high, Stats stats) {
        // Median-of-3 pivot selection
        int mid = low + (high - low) / 2;
        if (arr[mid] < arr[low])  swap(arr, low,  mid,  stats);
        if (arr[high] < arr[low]) swap(arr, low,  high, stats);
        if (arr[mid] < arr[high]) swap(arr, mid,  high, stats);
        // Now arr[high] is the median — use it as pivot
        int pivot = arr[high];

        int i = low - 1;
        for (int j = low; j < high; j++) {
            stats.comparisons++;
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j, stats);
            }
        }
        swap(arr, i + 1, high, stats); // Place pivot in final position
        return i + 1;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4. Heap Sort
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Heap Sort — NOT stable, in-place, guaranteed O(n log n) all cases.
     *
     * <p><b>Strategy:</b>
     * <ol>
     *   <li><b>Build Max-Heap:</b> O(n) — heapify from n/2 down to 0.</li>
     *   <li><b>Extract-Max repeatedly:</b> O(n log n) — swap root with last element,
     *       reduce heap size, sift down new root.</li>
     * </ol>
     *
     * <p><b>Advantage over Quick Sort:</b> Guaranteed O(n log n) worst case.
     * <p><b>Disadvantage:</b> Poor cache performance due to non-sequential memory access;
     * often slower than Quick Sort in practice despite same asymptotic complexity.
     *
     * @param arr   Array to sort (modified in-place)
     * @param stats Stats collector (may be null)
     */
    public static void heapSort(int[] arr, Stats stats) {
        if (arr == null || arr.length <= 1) return;
        if (stats == null) stats = new Stats();
        int n = arr.length;

        // Phase 1: Build max-heap (heapify bottom-up)
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i, stats);
        }

        // Phase 2: Extract elements one by one
        for (int i = n - 1; i > 0; i--) {
            swap(arr, 0, i, stats); // Move current root (max) to end
            heapify(arr, i, 0, stats); // Re-heapify reduced heap
        }
    }

    /**
     * Sift down element at index {@code rootIdx} in a max-heap of size {@code heapSize}.
     */
    private static void heapify(int[] arr, int heapSize, int rootIdx, Stats stats) {
        int largest = rootIdx;
        int left    = 2 * rootIdx + 1;
        int right   = 2 * rootIdx + 2;

        if (left < heapSize) {
            stats.comparisons++;
            if (arr[left] > arr[largest]) largest = left;
        }
        if (right < heapSize) {
            stats.comparisons++;
            if (arr[right] > arr[largest]) largest = right;
        }

        if (largest != rootIdx) {
            swap(arr, rootIdx, largest, stats);
            heapify(arr, heapSize, largest, stats); // Recurse on affected subtree
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────────────────────────────────

    private static void swap(int[] arr, int i, int j, Stats stats) {
        if (i != j) {
            int tmp  = arr[i];
            arr[i]   = arr[j];
            arr[j]   = tmp;
            stats.swaps++;
        }
    }
}
