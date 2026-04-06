package com.csc483.assignment1.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hybrid Search Engine that maintains:
 * <ol>
 *   <li>A sorted Product array for O(log n) ID-based binary search</li>
 *   <li>A HashMap index (name → List&lt;Product&gt;) for O(1) average name lookup</li>
 * </ol>
 *
 * <p><b>Design Rationale:</b><br>
 * The primary sorted array supports fast binary search by ID. The HashMap index
 * allows O(1) average-case name lookup without rebuilding any structure on insert —
 * we simply add the new product to both structures.
 *
 * <p><b>Complexity Summary:</b>
 * <ul>
 *   <li>searchById(id)       – O(log n)  binary search on sorted array</li>
 *   <li>searchByName(name)   – O(1) avg, O(k) where k = products sharing that name</li>
 *   <li>addProduct(product)  – O(n) for insertion into sorted array (shift right);
 *                              O(1) amortised for HashMap update</li>
 * </ul>
 *
 * <p><b>Note on addProduct complexity:</b><br>
 * Insertion into a sorted array is O(n) because elements must be shifted.
 * For frequent insertions, a balanced BST (TreeMap) or skip list is preferred.
 * This implementation uses the sorted array approach as required by the assignment,
 * with the trade-off that inserts are O(n) but searches remain O(log n).
 *
 * @author [Your Name]
 * @version 1.0
 */
public class HybridSearchEngine {

    /** Sorted array of products (ascending by productId). Acts as primary index. */
    private Product[] sortedProducts;

    /** Current number of valid products stored in sortedProducts. */
    private int size;

    /**
     * HashMap secondary index: productName (lowercase) → list of Products.
     * Handles multiple products sharing the same name (e.g., "USB Cable").
     * Average lookup: O(1).
     */
    private final Map<String, List<Product>> nameIndex;

    // ────────────────────────────────────────────────────────────
    // Constructor
    // ────────────────────────────────────────────────────────────

    /**
     * Initialises the engine with an existing sorted product array.
     * Builds the name index in O(n) time.
     *
     * @param initialProducts Pre-sorted array of products (sorted by productId).
     *                        Pass null or empty array to start with no products.
     */
    public HybridSearchEngine(Product[] initialProducts) {
        nameIndex = new HashMap<>();

        if (initialProducts == null || initialProducts.length == 0) {
            sortedProducts = new Product[16]; // Start small; will grow
            size = 0;
            return;
        }

        // Defensive copy + sort (in case caller didn't pre-sort)
        this.sortedProducts = Arrays.copyOf(initialProducts, initialProducts.length * 2); // extra capacity
        this.size = initialProducts.length;
        Arrays.sort(sortedProducts, 0, size); // Uses Product.compareTo (by ID)

        // Build name index in O(n)
        for (int i = 0; i < size; i++) {
            indexByName(sortedProducts[i]);
        }
    }

    // ────────────────────────────────────────────────────────────
    // Search Operations
    // ────────────────────────────────────────────────────────────

    /**
     * Search by product ID using binary search on the sorted array.
     * Time complexity: O(log n).
     *
     * @param targetId The product ID to find.
     * @return The matching Product, or null if not found.
     */
    public Product searchById(int targetId) {
        int low  = 0;
        int high = size - 1;

        while (low <= high) {
            int mid   = low + (high - low) / 2;
            int midId = sortedProducts[mid].getProductId();

            if (midId == targetId)     return sortedProducts[mid];
            else if (midId < targetId) low  = mid + 1;
            else                       high = mid - 1;
        }
        return null;
    }

    /**
     * Search by product name using the HashMap index.
     * Time complexity: O(1) average case.
     *
     * @param targetName Product name to search for (case-insensitive).
     * @return List of matching products (may have multiple), or empty list if none.
     */
    public List<Product> searchByName(String targetName) {
        if (targetName == null) return new ArrayList<>();
        List<Product> result = nameIndex.get(targetName.toLowerCase());
        return (result != null) ? result : new ArrayList<>();
    }

    // ────────────────────────────────────────────────────────────
    // Insert Operation
    // ────────────────────────────────────────────────────────────

    /**
     * Adds a new product while maintaining sorted order in the primary array
     * and updating the name index — without rebuilding any structure.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Grow array if at capacity (amortised O(1))</li>
     *   <li>Find the correct insertion position using binary search on IDs — O(log n)</li>
     *   <li>Shift elements right to make room — O(n) worst case</li>
     *   <li>Insert at found position</li>
     *   <li>Update the HashMap index — O(1) average</li>
     * </ol>
     *
     * <p>Overall: O(n) dominated by the shift step.
     *
     * @param newProduct The product to add. Duplicate IDs are rejected.
     * @return true if inserted successfully; false if a product with that ID already exists.
     */
    public boolean addProduct(Product newProduct) {
        if (newProduct == null) return false;

        // Reject duplicate IDs
        if (searchById(newProduct.getProductId()) != null) {
            System.err.println("WARNING: Product with ID " + newProduct.getProductId() + " already exists.");
            return false;
        }

        // Grow backing array if needed (double the capacity)
        if (size == sortedProducts.length) {
            sortedProducts = Arrays.copyOf(sortedProducts, sortedProducts.length * 2);
        }

        // Binary search for correct insertion position
        int insertPos = findInsertPosition(newProduct.getProductId());

        // Shift elements right from insertPos to size-1
        System.arraycopy(sortedProducts, insertPos, sortedProducts, insertPos + 1, size - insertPos);

        // Insert the new product
        sortedProducts[insertPos] = newProduct;
        size++;

        // Update name index (O(1) average)
        indexByName(newProduct);

        return true;
    }

    // ────────────────────────────────────────────────────────────
    // Helper Methods
    // ────────────────────────────────────────────────────────────

    /**
     * Finds the index at which a product with the given ID should be inserted
     * to keep the array sorted. Uses binary search. O(log n).
     */
    private int findInsertPosition(int targetId) {
        int low  = 0;
        int high = size; // Note: high = size (not size-1) for insertion

        while (low < high) {
            int mid   = low + (high - low) / 2;
            if (sortedProducts[mid].getProductId() < targetId) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }

    /**
     * Adds a product to the HashMap name index.
     * Keys are lowercase product names; values are lists of Products.
     */
    private void indexByName(Product product) {
        String key = product.getProductName().toLowerCase();
        nameIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(product);
    }

    /** Returns current number of products stored. */
    public int getSize() { return size; }

    /** Returns a read-only view of the sorted array (for testing). */
    public Product[] getSortedProducts() {
        return Arrays.copyOf(sortedProducts, size);
    }
}
