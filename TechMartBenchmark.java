package com.csc483.assignment1.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * TechMart Search Performance Benchmark
 *
 * <p>Generates 100,000 products with random IDs (1–200,000), measures
 * and compares sequential vs binary search performance across best, average,
 * and worst cases, then demonstrates the hybrid engine.
 *
 * @author [Your Name]
 * @version 1.0
 */
public class TechMartBenchmark {

    // ──────── Constants ────────
    private static final int N               = 100_000;   // Catalog size
    private static final int ID_RANGE_MAX    = 200_000;   // ID pool upper bound
    private static final int WARMUP_RUNS     = 5;         // JVM warm-up iterations
    private static final int TIMING_RUNS     = 10;        // Timing iterations per test
    private static final int NANO_TO_MS      = 1_000_000; // Nanoseconds per millisecond

    private static final String[] CATEGORIES = {
        "Laptop", "Phone", "Tablet", "Monitor", "Keyboard",
        "Mouse", "Headphones", "Webcam", "Speaker", "Router"
    };

    private static final String[] NAME_PREFIXES = {
        "TechPro", "SmartTech", "AlphaTech", "UltraTech", "NovaTech",
        "EliteTech", "ZenTech", "OmegaTech", "HyperTech", "AeroTech"
    };

    // ──────────────────────────────────────────────────────────────────
    // Main Entry Point
    // ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println(" TECHMART SEARCH PERFORMANCE ANALYSIS (n = " + N + " products)");
        System.out.println("================================================================\n");

        // 1. Generate dataset
        System.out.println("[1/4] Generating " + N + " random products...");
        Product[] unsortedCatalog = generateProducts(N);

        // 2. Create sorted copy for binary search
        System.out.println("[2/4] Sorting catalog for binary search...");
        Product[] sortedCatalog = Arrays.copyOf(unsortedCatalog, N);
        Arrays.sort(sortedCatalog); // Uses Product.compareTo → sorts by productId

        // 3. Determine test targets
        int bestCaseId    = sortedCatalog[0].getProductId();                   // First element
        int avgCaseId     = sortedCatalog[N / 2].getProductId();               // Middle element
        int worstCaseId   = findAbsentId(sortedCatalog);                       // ID not in array
        String searchName = sortedCatalog[N / 2].getProductName();             // Existing name

        System.out.println("[3/4] Running benchmarks (JVM warm-up included)...\n");

        // ────── Warm Up JVM ──────
        for (int i = 0; i < WARMUP_RUNS; i++) {
            SearchOperations.sequentialSearchById(sortedCatalog, avgCaseId);
            SearchOperations.binarySearchById(sortedCatalog, avgCaseId);
        }

        // ────── Sequential Search ──────
        double seqBest    = measureSeq(unsortedCatalog, bestCaseId);
        double seqAvg     = measureSeq(unsortedCatalog, avgCaseId);
        double seqWorst   = measureSeq(unsortedCatalog, worstCaseId);

        // ────── Binary Search ──────
        double binBest    = measureBin(sortedCatalog, bestCaseId);
        double binAvg     = measureBin(sortedCatalog, avgCaseId);
        double binWorst   = measureBin(sortedCatalog, worstCaseId);

        // ────── Hybrid Engine ──────
        System.out.println("[4/4] Testing Hybrid Search Engine...");
        HybridSearchEngine engine = new HybridSearchEngine(sortedCatalog);

        double hybridNameAvg   = measureHybridNameSearch(engine, searchName);
        double hybridInsertAvg = measureHybridInsert(engine);

        // ────── Results Table ──────
        double speedup = seqAvg / binAvg;

        System.out.println("================================================================");
        System.out.println(" TECHMART SEARCH PERFORMANCE ANALYSIS (n = " + N + " products)");
        System.out.println("================================================================\n");

        System.out.println("SEQUENTIAL SEARCH (on unsorted array):");
        System.out.printf("  Best  Case (target at index 0):     %8.4f ms%n", seqBest);
        System.out.printf("  Avg   Case (target at middle):      %8.4f ms%n", seqAvg);
        System.out.printf("  Worst Case (target not in array):   %8.4f ms%n%n", seqWorst);

        System.out.println("BINARY SEARCH (on sorted array):");
        System.out.printf("  Best  Case (target at midpoint):    %8.4f ms%n", binBest);
        System.out.printf("  Avg   Case (target at middle):      %8.4f ms%n", binAvg);
        System.out.printf("  Worst Case (target not in array):   %8.4f ms%n%n", binWorst);

        System.out.printf("PERFORMANCE IMPROVEMENT: Binary search is ~%.0fx faster on average%n%n", speedup);

        System.out.println("HYBRID NAME SEARCH:");
        System.out.printf("  Average name search time:           %8.4f ms%n", hybridNameAvg);
        System.out.printf("  Average product insert time:        %8.4f ms%n%n", hybridInsertAvg);

        System.out.println("================================================================");

        // ────── Theoretical Comparison ──────
        System.out.println("\nTHEORETICAL ANALYSIS:");
        System.out.printf("  Sequential avg comparisons (n/2):  %.0f%n", (double) N / 2);
        System.out.printf("  Binary search avg comparisons (log2 n): %.1f%n", Math.log(N) / Math.log(2));
        System.out.printf("  Theoretical speedup ratio:          %.0fx%n",
            (N / 2.0) / (Math.log(N) / Math.log(2)));

        // ────── Correctness Verification ──────
        System.out.println("\nCORRECTNESS CHECK:");
        verifySearchResults(unsortedCatalog, sortedCatalog, bestCaseId, avgCaseId, worstCaseId);
    }

    // ──────────────────────────────────────────────────────────────────
    // Measurement Helpers
    // ──────────────────────────────────────────────────────────────────

    private static double measureSeq(Product[] arr, int targetId) {
        long total = 0;
        for (int i = 0; i < TIMING_RUNS; i++) {
            long start = System.nanoTime();
            SearchOperations.sequentialSearchById(arr, targetId);
            total += System.nanoTime() - start;
        }
        return (double) total / TIMING_RUNS / NANO_TO_MS;
    }

    private static double measureBin(Product[] arr, int targetId) {
        long total = 0;
        for (int i = 0; i < TIMING_RUNS; i++) {
            long start = System.nanoTime();
            SearchOperations.binarySearchById(arr, targetId);
            total += System.nanoTime() - start;
        }
        return (double) total / TIMING_RUNS / NANO_TO_MS;
    }

    private static double measureHybridNameSearch(HybridSearchEngine engine, String name) {
        long total = 0;
        for (int i = 0; i < TIMING_RUNS; i++) {
            long start = System.nanoTime();
            engine.searchByName(name);
            total += System.nanoTime() - start;
        }
        return (double) total / TIMING_RUNS / NANO_TO_MS;
    }

    private static double measureHybridInsert(HybridSearchEngine engine) {
        Random rand = new Random(42);
        long total  = 0;
        int inserted = 0;

        // Attempt to insert 100 new products and measure successful insertions
        for (int attempt = 0; attempt < TIMING_RUNS * 10 && inserted < TIMING_RUNS; attempt++) {
            int newId = ID_RANGE_MAX + rand.nextInt(50_000) + 1;
            Product p = new Product(newId, "NewProduct-" + newId, "Electronics", 99.99, 10);
            long start = System.nanoTime();
            boolean ok = engine.addProduct(p);
            long elapsed = System.nanoTime() - start;
            if (ok) {
                total += elapsed;
                inserted++;
            }
        }
        return inserted > 0 ? (double) total / inserted / NANO_TO_MS : -1;
    }

    // ──────────────────────────────────────────────────────────────────
    // Dataset Generation
    // ──────────────────────────────────────────────────────────────────

    /**
     * Generates {@code count} products with unique random IDs from 1 to 200,000.
     * Ensures uniqueness via a HashSet sentinel.
     */
    private static Product[] generateProducts(int count) {
        Product[] products = new Product[count];
        Set<Integer> usedIds = new HashSet<>(count * 2);
        Random rand = new Random(12345L); // Fixed seed for reproducibility
        int i = 0;

        while (i < count) {
            int id = rand.nextInt(ID_RANGE_MAX) + 1;
            if (usedIds.add(id)) { // add returns false if already present
                String cat  = CATEGORIES[rand.nextInt(CATEGORIES.length)];
                String name = NAME_PREFIXES[rand.nextInt(NAME_PREFIXES.length)]
                              + " " + cat + " " + (1000 + rand.nextInt(9000));
                double price = 10.0 + rand.nextDouble() * 2990.0;
                int stock    = rand.nextInt(500);
                products[i++] = new Product(id, name, cat, price, stock);
            }
        }
        return products;
    }

    /**
     * Finds an ID that does NOT exist in the sorted catalog (for worst-case testing).
     */
    private static int findAbsentId(Product[] sorted) {
        Set<Integer> presentIds = new HashSet<>(sorted.length * 2);
        for (Product p : sorted) presentIds.add(p.getProductId());

        for (int candidate = 1; candidate <= ID_RANGE_MAX + 10_000; candidate++) {
            if (!presentIds.contains(candidate)) return candidate;
        }
        return ID_RANGE_MAX + 99_999; // Fallback
    }

    // ──────────────────────────────────────────────────────────────────
    // Correctness Verification
    // ──────────────────────────────────────────────────────────────────

    private static void verifySearchResults(Product[] unsorted, Product[] sorted,
                                            int bestId, int avgId, int worstId) {
        // Both methods should agree on existing elements
        Product seq1 = SearchOperations.sequentialSearchById(unsorted, bestId);
        Product bin1 = SearchOperations.binarySearchById(sorted, bestId);
        System.out.println("  ID " + bestId + " found by both methods: " +
            (seq1 != null && bin1 != null && seq1.getProductId() == bin1.getProductId() ? "PASS ✓" : "FAIL ✗"));

        Product seq2 = SearchOperations.sequentialSearchById(unsorted, avgId);
        Product bin2 = SearchOperations.binarySearchById(sorted, avgId);
        System.out.println("  ID " + avgId + " found by both methods: " +
            (seq2 != null && bin2 != null && seq2.getProductId() == bin2.getProductId() ? "PASS ✓" : "FAIL ✗"));

        Product seq3 = SearchOperations.sequentialSearchById(unsorted, worstId);
        Product bin3 = SearchOperations.binarySearchById(sorted, worstId);
        System.out.println("  Absent ID " + worstId + " returns null from both: " +
            (seq3 == null && bin3 == null ? "PASS ✓" : "FAIL ✗"));
    }
}
