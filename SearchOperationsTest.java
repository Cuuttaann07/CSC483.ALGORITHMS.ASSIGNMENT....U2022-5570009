package com.csc483.assignment1.search;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for TechMart search operations and hybrid engine.
 *
 * <p>Covers: null safety, empty arrays, single element, large arrays,
 * best/average/worst case correctness, and hybrid engine insertion.
 *
 * @author [Your Name]
 */
@DisplayName("TechMart Search Tests")
class SearchOperationsTest {

    // ───── Shared fixtures ─────
    private static Product[] SORTED_ARRAY;
    private static Product[] UNSORTED_ARRAY;

    @BeforeAll
    static void setup() {
        // Create 10 products with known IDs: 10, 20, 30, ..., 100
        SORTED_ARRAY = new Product[10];
        for (int i = 0; i < 10; i++) {
            SORTED_ARRAY[i] = new Product((i + 1) * 10, "Product-" + (i + 1) * 10,
                "Electronics", 100.0 + i * 10, 50);
        }

        // Unsorted: reversed order
        UNSORTED_ARRAY = Arrays.copyOf(SORTED_ARRAY, SORTED_ARRAY.length);
        for (int i = 0; i < UNSORTED_ARRAY.length / 2; i++) {
            Product tmp = UNSORTED_ARRAY[i];
            UNSORTED_ARRAY[i] = UNSORTED_ARRAY[UNSORTED_ARRAY.length - 1 - i];
            UNSORTED_ARRAY[UNSORTED_ARRAY.length - 1 - i] = tmp;
        }
    }

    // ─────────────────────────────────────────────
    // Sequential Search Tests
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("Sequential Search")
    class SequentialSearchTests {

        @Test
        @DisplayName("Should find product at first position (best case)")
        void testBestCase() {
            Product result = SearchOperations.sequentialSearchById(SORTED_ARRAY, 10);
            assertNotNull(result);
            assertEquals(10, result.getProductId());
        }

        @Test
        @DisplayName("Should find product at last position (worst case)")
        void testWorstCase() {
            Product result = SearchOperations.sequentialSearchById(SORTED_ARRAY, 100);
            assertNotNull(result);
            assertEquals(100, result.getProductId());
        }

        @Test
        @DisplayName("Should return null for absent ID")
        void testNotFound() {
            assertNull(SearchOperations.sequentialSearchById(SORTED_ARRAY, 999));
        }

        @Test
        @DisplayName("Should handle null array gracefully")
        void testNullArray() {
            assertNull(SearchOperations.sequentialSearchById(null, 10));
        }

        @Test
        @DisplayName("Should handle empty array")
        void testEmptyArray() {
            assertNull(SearchOperations.sequentialSearchById(new Product[0], 10));
        }

        @Test
        @DisplayName("Should find all products in unsorted array")
        void testAllProductsFound() {
            for (Product p : SORTED_ARRAY) {
                Product result = SearchOperations.sequentialSearchById(UNSORTED_ARRAY, p.getProductId());
                assertNotNull(result, "Should find ID " + p.getProductId());
                assertEquals(p.getProductId(), result.getProductId());
            }
        }
    }

    // ─────────────────────────────────────────────
    // Binary Search Tests
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("Binary Search")
    class BinarySearchTests {

        @Test
        @DisplayName("Should find product at midpoint (best case)")
        void testBestCase() {
            // Mid of 10 elements is index 4 or 5 → ID 50 or 60
            Product result = SearchOperations.binarySearchById(SORTED_ARRAY, 50);
            assertNotNull(result);
            assertEquals(50, result.getProductId());
        }

        @Test
        @DisplayName("Should find product at first position")
        void testFirstElement() {
            Product result = SearchOperations.binarySearchById(SORTED_ARRAY, 10);
            assertNotNull(result);
            assertEquals(10, result.getProductId());
        }

        @Test
        @DisplayName("Should find product at last position")
        void testLastElement() {
            Product result = SearchOperations.binarySearchById(SORTED_ARRAY, 100);
            assertNotNull(result);
            assertEquals(100, result.getProductId());
        }

        @Test
        @DisplayName("Should return null for absent ID (worst case)")
        void testNotFound() {
            assertNull(SearchOperations.binarySearchById(SORTED_ARRAY, 999));
        }

        @Test
        @DisplayName("Should handle null array")
        void testNullArray() {
            assertNull(SearchOperations.binarySearchById(null, 10));
        }

        @Test
        @DisplayName("Should handle empty array")
        void testEmptyArray() {
            assertNull(SearchOperations.binarySearchById(new Product[0], 10));
        }

        @Test
        @DisplayName("Should handle single-element array — found")
        void testSingleElementFound() {
            Product[] single = { new Product(42, "Solo", "Test", 9.99, 1) };
            assertNotNull(SearchOperations.binarySearchById(single, 42));
        }

        @Test
        @DisplayName("Should handle single-element array — not found")
        void testSingleElementNotFound() {
            Product[] single = { new Product(42, "Solo", "Test", 9.99, 1) };
            assertNull(SearchOperations.binarySearchById(single, 99));
        }

        @ParameterizedTest(name = "Binary search finds ID {0}")
        @ValueSource(ints = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100})
        @DisplayName("Should find every product in sorted array")
        void testAllProductsFound(int id) {
            Product result = SearchOperations.binarySearchById(SORTED_ARRAY, id);
            assertNotNull(result);
            assertEquals(id, result.getProductId());
        }

        @Test
        @DisplayName("Sequential and Binary must agree on all results")
        void testConsistencyWithSequential() {
            for (Product p : SORTED_ARRAY) {
                Product seq = SearchOperations.sequentialSearchById(SORTED_ARRAY, p.getProductId());
                Product bin = SearchOperations.binarySearchById(SORTED_ARRAY, p.getProductId());
                assertEquals(seq.getProductId(), bin.getProductId(),
                    "Results must match for ID " + p.getProductId());
            }
        }
    }

    // ─────────────────────────────────────────────
    // Search By Name Tests
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("Name Search")
    class NameSearchTests {

        @Test
        @DisplayName("Should find product by exact name")
        void testExactName() {
            assertNotNull(SearchOperations.searchByName(SORTED_ARRAY, "Product-10"));
        }

        @Test
        @DisplayName("Should find product case-insensitively")
        void testCaseInsensitive() {
            assertNotNull(SearchOperations.searchByName(SORTED_ARRAY, "PRODUCT-10"));
            assertNotNull(SearchOperations.searchByName(SORTED_ARRAY, "product-10"));
        }

        @Test
        @DisplayName("Should return null for nonexistent name")
        void testNotFound() {
            assertNull(SearchOperations.searchByName(SORTED_ARRAY, "Ghost Product"));
        }

        @Test
        @DisplayName("Should handle null array")
        void testNullArray() {
            assertNull(SearchOperations.searchByName(null, "Product-10"));
        }

        @Test
        @DisplayName("Should handle null name")
        void testNullName() {
            assertNull(SearchOperations.searchByName(SORTED_ARRAY, null));
        }
    }

    // ─────────────────────────────────────────────
    // Hybrid Engine Tests
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("Hybrid Search Engine")
    class HybridEngineTests {

        private HybridSearchEngine engine;

        @BeforeEach
        void setupEngine() {
            engine = new HybridSearchEngine(Arrays.copyOf(SORTED_ARRAY, SORTED_ARRAY.length));
        }

        @Test
        @DisplayName("searchById should find existing products")
        void testSearchById() {
            assertNotNull(engine.searchById(10));
            assertNotNull(engine.searchById(100));
        }

        @Test
        @DisplayName("searchByName should return matching products via HashMap")
        void testSearchByName() {
            List<Product> results = engine.searchByName("Product-50");
            assertFalse(results.isEmpty());
            assertEquals(50, results.get(0).getProductId());
        }

        @Test
        @DisplayName("searchByName should return empty list for unknown name")
        void testSearchByNameNotFound() {
            assertTrue(engine.searchByName("NonExistent").isEmpty());
        }

        @Test
        @DisplayName("addProduct should maintain sorted order")
        void testAddProductMaintainsSortedOrder() {
            Product newP = new Product(25, "New Mid Product", "Electronics", 199.99, 5);
            assertTrue(engine.addProduct(newP));

            Product[] sorted = engine.getSortedProducts();
            for (int i = 0; i < sorted.length - 1; i++) {
                assertTrue(sorted[i].getProductId() < sorted[i + 1].getProductId(),
                    "Array must remain sorted after insertion");
            }
        }

        @Test
        @DisplayName("addProduct should reject duplicate IDs")
        void testAddProductDuplicateId() {
            Product duplicate = new Product(10, "Duplicate", "Electronics", 5.0, 1);
            assertFalse(engine.addProduct(duplicate));
            assertEquals(10, engine.getSize()); // Size must not change
        }

        @Test
        @DisplayName("addProduct should allow searching inserted product by ID and name")
        void testInsertedProductIsSearchable() {
            Product newP = new Product(999, "Quantum Widget", "Accessories", 49.99, 20);
            engine.addProduct(newP);

            assertNotNull(engine.searchById(999));
            assertFalse(engine.searchByName("quantum widget").isEmpty());
        }

        @Test
        @DisplayName("addProduct should handle null gracefully")
        void testAddNullProduct() {
            int sizeBefore = engine.getSize();
            assertFalse(engine.addProduct(null));
            assertEquals(sizeBefore, engine.getSize());
        }
    }

    // ─────────────────────────────────────────────
    // Product Class Tests
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("Product Class")
    class ProductClassTests {

        @Test
        @DisplayName("Constructor should reject negative price")
        void testNegativePrice() {
            assertThrows(IllegalArgumentException.class,
                () -> new Product(1, "X", "Y", -5.0, 0));
        }

        @Test
        @DisplayName("Constructor should reject negative stock")
        void testNegativeStock() {
            assertThrows(IllegalArgumentException.class,
                () -> new Product(1, "X", "Y", 5.0, -1));
        }

        @Test
        @DisplayName("compareTo should order by productId")
        void testCompareTo() {
            Product p1 = new Product(10, "A", "B", 1.0, 1);
            Product p2 = new Product(20, "A", "B", 1.0, 1);
            assertTrue(p1.compareTo(p2) < 0);
            assertTrue(p2.compareTo(p1) > 0);
            assertEquals(0, p1.compareTo(p1));
        }

        @Test
        @DisplayName("equals should compare by productId")
        void testEquals() {
            Product p1 = new Product(42, "Alpha", "Cat", 9.99, 5);
            Product p2 = new Product(42, "Beta",  "Cat", 4.99, 3); // same ID, different details
            assertEquals(p1, p2);
        }
    }
}
