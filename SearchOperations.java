package com.csc483.assignment1.search;

/**
 * Provides sequential, binary, and name-based search methods
 * for the TechMart product catalog.
 *
 * <p>Complexity Summary:
 * <ul>
 *   <li>sequentialSearchById  – O(n) time, O(1) space</li>
 *   <li>binarySearchById      – O(log n) time, O(1) space (precondition: sorted by ID)</li>
 *   <li>searchByName          – O(n) time, O(1) space</li>
 * </ul>
 *
 * @author [Your Name]
 * @version 1.0
 */
public class SearchOperations {

    // ─────────────────────────────────────────────────────
    // 1. Sequential Search by ID
    // ─────────────────────────────────────────────────────

    /**
     * Searches for a product by its unique ID using linear/sequential scan.
     *
     * <p>Complexity:
     * <ul>
     *   <li>Best  case: O(1)   — target is the first element</li>
     *   <li>Avg   case: O(n/2) — target found in middle on average</li>
     *   <li>Worst case: O(n)   — target is last element or not present</li>
     * </ul>
     *
     * @param products  Array of Product objects (any order)
     * @param targetId  The product ID to find
     * @return          The matching Product, or null if not found
     */
    public static Product sequentialSearchById(Product[] products, int targetId) {
        if (products == null) return null;

        for (int i = 0; i < products.length; i++) {
            if (products[i] != null && products[i].getProductId() == targetId) {
                return products[i];
            }
        }
        return null; // Not found
    }

    // ─────────────────────────────────────────────────────
    // 2. Binary Search by ID
    // ─────────────────────────────────────────────────────

    /**
     * Searches for a product by its unique ID using binary search.
     *
     * <p><b>Precondition:</b> The {@code products} array MUST be sorted in ascending
     * order of {@code productId}. If this condition is not met, the result is undefined.
     *
     * <p>Complexity:
     * <ul>
     *   <li>Best  case: O(1)     — target is at the midpoint on first check</li>
     *   <li>Avg   case: O(log n) — ~log₂(n) comparisons on average</li>
     *   <li>Worst case: O(log n) — target not present; search space halved each step</li>
     * </ul>
     *
     * <p>For n = 100,000: log₂(100,000) ≈ 17 comparisons vs ~50,000 for sequential.
     *
     * @param products  Array of Products sorted ascending by productId
     * @param targetId  The product ID to find
     * @return          The matching Product, or null if not found
     */
    public static Product binarySearchById(Product[] products, int targetId) {
        if (products == null || products.length == 0) return null;

        int low  = 0;
        int high = products.length - 1;

        while (low <= high) {
            // Use low + (high - low) / 2 to prevent integer overflow
            int mid = low + (high - low) / 2;
            int midId = products[mid].getProductId();

            if (midId == targetId) {
                return products[mid];          // Found
            } else if (midId < targetId) {
                low = mid + 1;                 // Search right half
            } else {
                high = mid - 1;               // Search left half
            }
        }
        return null; // Not found
    }

    // ─────────────────────────────────────────────────────
    // 3. Sequential Search by Name
    // ─────────────────────────────────────────────────────

    /**
     * Searches for a product by name using case-insensitive sequential scan.
     *
     * <p>Binary search cannot be used here because the array is sorted by ID,
     * not by name. A name-based binary search would require a separately
     * maintained sorted-by-name structure (see HybridSearchEngine).
     *
     * <p>Complexity: O(n) — must inspect every element in the worst case.
     *
     * @param products    Array of Product objects (sorted by ID, not name)
     * @param targetName  The product name to search for (case-insensitive)
     * @return            The first matching Product, or null if not found
     */
    public static Product searchByName(Product[] products, String targetName) {
        if (products == null || targetName == null) return null;

        for (Product product : products) {
            if (product != null && product.getProductName().equalsIgnoreCase(targetName)) {
                return product;
            }
        }
        return null;
    }
}
