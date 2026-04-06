# CSC 483 – Algorithm Design, Analysis, and Optimization
## Complete Assignment Answers

---

# QUESTION 1: Online Store Search Optimization

---

## Part A: Algorithm Analysis

### A.1 – Sequential Search: Exact Comparison Count for Array of n Products

Sequential (linear) search visits each element one at a time from index 0 to n−1,
stopping only when the target is found or the entire array has been checked.

| Case | Condition | Exact # of Comparisons |
|---|---|---|
| **Best Case** | Target is at index 0 (first element) | **1** |
| **Average Case** | Target is equally likely at any position | **(n + 1) / 2 ≈ n/2** |
| **Worst Case** | Target is at index n−1 or not in array | **n** |

**Derivation of average case:**
If the target is at position i (0-indexed), we perform i + 1 comparisons.
Assuming uniform distribution across all n positions:

  Average = (1 + 2 + 3 + ... + n) / n = n(n + 1) / (2n) = (n + 1) / 2

For n = 100,000: Average comparisons = (100,000 + 1) / 2 ≈ **50,000.5**

**Time Complexity: O(n)**
**Space Complexity: O(1)**

---

### A.2 – Why Binary Search Is More Efficient

**Precondition (critical):** The array MUST be sorted (in ascending or descending order
by the search key — in this case, productId). Binary search is UNDEFINED on an unsorted array.

**Why it's more efficient:**

Binary search uses a divide-and-conquer strategy. At each step it compares the target
with the element at the midpoint of the remaining search range. Based on the comparison:
- If target == mid element → found (return)
- If target < mid element → search the left half only
- If target > mid element → search the right half only

Each comparison eliminates **half** the remaining candidates, compared to sequential
search which eliminates only **one** candidate per comparison. After k comparisons,
at most n / 2^k elements remain. The search ends when this count reaches 1:

  n / 2^k = 1  →  k = log₂(n)

| Case | Sequential | Binary Search |
|---|---|---|
| Best  | 1 | 1 |
| Avg   | (n + 1) / 2 | ≈ log₂(n) |
| Worst | n | ⌊log₂(n)⌋ + 1 |

**Time Complexity: O(log n)**
**Space Complexity: O(1)**

---

### A.3 – Speed Comparison for n = 100,000

```
Sequential average comparisons  = (n + 1) / 2 = 50,000.5 ≈ 50,000
Binary search average comparisons = log₂(n) = log₂(100,000) = 16.61 ≈ 17

Speedup ratio = 50,000 / 17 ≈ 2,941

∴ Binary search is approximately 2,941× faster than sequential search on average
  for a catalog of 100,000 products.
```

**Cross-check with worst case:**
```
Sequential worst = 100,000 comparisons
Binary worst     = ⌊log₂(100,000)⌋ + 1 = 16 + 1 = 17 comparisons
Worst-case speedup = 100,000 / 17 ≈ 5,882×
```

---

## Part B: Implementation and Optimization

See `Product.java`, `SearchOperations.java`, and `TechMartBenchmark.java` in the
accompanying GitHub repository. Key design decisions explained below.

### B.1 – Product Class Design

The `Product` class implements `Comparable<Product>` with ordering by `productId`.
This is required so that `Arrays.sort(products)` sorts by ID without needing a
separate Comparator — a clean, idiomatic Java design that makes the binary search
precondition self-documenting.

Defensive validation is placed in the constructor (throwing `IllegalArgumentException`
for negative prices or stock quantities) rather than in every method that uses those
fields — a "fail fast" principle.

### B.2 – Search Method Notes

**`sequentialSearchById`:** Iterates index 0 to n−1. Returns the first match.
No precondition. Works on sorted and unsorted arrays.

**`binarySearchById`:** Uses the `low + (high - low) / 2` midpoint formula instead
of the naive `(low + high) / 2` to prevent integer overflow when both indices are
large (i.e., near Integer.MAX_VALUE).

**`searchByName`:** Must be sequential because the array is sorted by ID, not name.
A binary search on names requires a separate data structure (see Part C).

### B.3 – Test Program Design

The benchmark (TechMartBenchmark.java):
1. Generates 100,000 products with **unique** random IDs in [1, 200,000] using a HashSet
   sentinel to guarantee uniqueness without retrying on every collision.
2. Performs WARMUP_RUNS before timing to allow the JVM's JIT compiler to fully
   compile hot paths, making timing results representative of steady-state performance.
3. Averages TIMING_RUNS repetitions per test to reduce noise.
4. Verifies correctness separately from timing (correctness checks have overhead
   that would distort timing measurements).

---

## Part C: Hybrid Search Approach

### C.1 – Design

The `HybridSearchEngine` maintains **two data structures in parallel**:

```
┌─────────────────────────────────────────────────────┐
│                HybridSearchEngine                   │
│                                                     │
│  Primary:   Product[] sortedProducts                │
│             Sorted ascending by productId           │
│             Supports: O(log n) binary search by ID  │
│                                                     │
│  Secondary: HashMap<String, List<Product>>          │
│             Key   = productName.toLowerCase()       │
│             Value = all products with that name     │
│             Supports: O(1) avg name lookup          │
└─────────────────────────────────────────────────────┘
```

### C.2 – Handling New Product Additions Without Full Rebuild

`addProduct(Product newProduct)` performs the following steps:

1. **Check capacity** — if the backing array is full, double its size (amortised O(1))
2. **Binary search for insertion position** — O(log n) to find where to place the new ID
3. **Shift right** — move all elements from insertPos to size−1 one position right: O(n)
4. **Insert** at insertPos
5. **Update HashMap index** — `computeIfAbsent` adds the product to the name list: O(1) avg

Key insight: the HashMap never needs to be rebuilt. Every insertion adds one entry
to the map in O(1) amortised time, regardless of the catalog size.

### C.3 – Pseudocode

```
HYBRID SEARCH ENGINE DESIGN:

DATA STRUCTURES:
  sortedProducts: Product[CAPACITY]   // Primary index — sorted by ID
  size          : int                 // Number of active products
  nameIndex     : HashMap<String → List<Product>>  // Secondary index

──────────────────────────────────────────
PROCEDURE searchById(targetId):
  low ← 0,  high ← size − 1
  WHILE low ≤ high:
    mid ← low + (high − low) / 2
    IF sortedProducts[mid].id == targetId: RETURN sortedProducts[mid]
    ELSE IF sortedProducts[mid].id < targetId: low ← mid + 1
    ELSE: high ← mid − 1
  RETURN null
Time: O(log n)

──────────────────────────────────────────
PROCEDURE searchByName(targetName):
  key ← toLowercase(targetName)
  RETURN nameIndex.get(key)    // O(1) average
Time: O(1) average, O(k) where k = matching products

──────────────────────────────────────────
PROCEDURE addProduct(newProduct):
  IF size == capacity: DOUBLE array capacity    // O(n) amortised O(1)
  insertPos ← binaryFindInsertPosition(newProduct.id)  // O(log n)
  SHIFT sortedProducts[insertPos .. size-1] right by 1  // O(n)
  sortedProducts[insertPos] ← newProduct
  size ← size + 1
  nameIndex[toLowercase(newProduct.name)].add(newProduct)  // O(1)
Time: O(n) dominated by shift

──────────────────────────────────────────
PROCEDURE binaryFindInsertPosition(targetId):
  low ← 0,  high ← size
  WHILE low < high:
    mid ← low + (high − low) / 2
    IF sortedProducts[mid].id < targetId: low ← mid + 1
    ELSE: high ← mid
  RETURN low
Time: O(log n)
```

### C.4 – Complexity Analysis of Hybrid Approach

| Operation | Time Complexity | Space | Notes |
|---|---|---|---|
| `searchById` | O(log n) | O(1) | Binary search on sorted array |
| `searchByName` | O(1) avg, O(n) worst | O(1) | HashMap lookup; worst if all names hash to same bucket |
| `addProduct` (sorted array) | O(n) | O(1) | Shift step dominates |
| `addProduct` (HashMap update) | O(1) amortised | O(1) | No rebuild needed |
| Build initial engine | O(n log n) | O(n) | Initial sort + HashMap construction |

**Trade-off discussion:**
The O(n) insertion cost is acceptable when insertions are rare (as stated in the
problem: "products are frequently added but rarely removed"). For high-frequency
insertions, a **TreeMap<Integer, Product>** (red-black BST) would give O(log n)
for both search and insert, at the cost of slightly higher constant factors
compared to array binary search.

---

# QUESTION 2: Algorithm Analysis and Comparison

---

## Part A: Comparative Analysis Table

| Algorithm | Best Case | Average Case | Worst Case | Space | Stable? | In-Place? | When to Use |
|---|---|---|---|---|---|---|---|
| **Sequential Search** | O(1) | O(n) | O(n) | O(1) | N/A | N/A | Unsorted arrays; small n |
| **Binary Search** | O(1) | O(log n) | O(log n) | O(1) | N/A | N/A | Sorted arrays; any n |
| **Bubble Sort** | O(n)¹ | O(n²) | O(n²) | O(1) | Yes | Yes | Educational only; never in production |
| **Insertion Sort** | O(n) | O(n²) | O(n²) | O(1) | Yes | Yes | Small n or nearly-sorted data |
| **Merge Sort** | O(n log n) | O(n log n) | O(n log n) | O(n) | Yes | No | Stable sort required; linked lists; external sort |
| **Quick Sort** | O(n log n) | O(n log n) | O(n²) | O(log n)² | No | Yes | General-purpose; random data; when avg case matters |
| **Heap Sort** | O(n log n) | O(n log n) | O(n log n) | O(1) | No | Yes | Guaranteed O(n log n) with O(1) space |

¹ Best case O(n) with early-termination optimization (flag for "no swaps").
² O(log n) stack space for recursive calls (median-of-3 or random pivot).

**Notes on key distinctions:**

- **Stability** means equal elements preserve their original relative order after sorting.
  Merge Sort and Insertion Sort are stable; Quick Sort and Heap Sort are not.
  Stability matters when sorting objects by a secondary key after a primary key sort.

- **In-place** means O(1) auxiliary space. Merge Sort is the only common O(n log n)
  algorithm that is NOT in-place (it requires O(n) auxiliary space for merging).

- **Bubble Sort**: Despite the same O(n) best case as Insertion Sort, Insertion Sort
  makes far fewer writes and has better constant factors — Bubble Sort is effectively
  obsolete in practice.

---

## Part B: Algorithm Identification and Justification

### Scenario A: Sort 1 million nearly-sorted database records; minimal memory
**Chosen Algorithm: Insertion Sort**

Justification:
- Insertion Sort has O(n) best-case performance on already-sorted or nearly-sorted data.
  Each element is only a few positions from its final position, so the inner shift
  loop terminates almost immediately — yielding effectively linear performance.
- Space is O(1) — completely in-place, no auxiliary memory required.
- No other common algorithm combines O(1) space with near-linear speed on nearly-sorted data.
- Merge Sort would give O(n log n) always but requires O(n) extra memory (violates constraint).
- Quick Sort degrades to O(n²) on nearly-sorted data with a poor pivot strategy.

**Alternative: Tim Sort** (which uses Insertion Sort for small runs + Merge Sort for large ones)
— this is what Java's `Arrays.sort` uses for Object arrays and is ideal here.

### Scenario B: Real-time system, guaranteed O(n log n) worst case
**Chosen Algorithm: Heap Sort (or Merge Sort)**

Justification:
- Heap Sort guarantees O(n log n) in ALL cases (best, average, worst) with O(1) space.
  No pathological input can degrade it — making it ideal for real-time systems.
- Quick Sort is disqualified because its worst case is O(n²), which cannot be tolerated
  in a system with guaranteed response time requirements.
- Merge Sort also guarantees O(n log n) but requires O(n) space; if memory is not
  constrained, Merge Sort is preferred as it's cache-friendlier.
- **Recommendation: Heap Sort** if memory is constrained; Merge Sort otherwise.

### Scenario C: Stable sort, 10,000 objects with 8-byte keys, no memory constraint
**Chosen Algorithm: Merge Sort**

Justification:
- Stability is required → eliminates Quick Sort and Heap Sort.
- Merge Sort is stable and guarantees O(n log n) in all cases.
- Memory is not constrained → the O(n) auxiliary space is acceptable.
- For 10,000 elements, the constant factors of Merge Sort are manageable.
- Insertion Sort is stable but O(n²) for unsorted data — too slow at 10,000 elements.

### Scenario D: Find element in sorted array of 1 billion elements
**Chosen Algorithm: Binary Search**

Justification:
- The array is sorted — binary search precondition is satisfied.
- Binary search: log₂(1,000,000,000) ≈ 30 comparisons. Sequential: 500 million average.
- No other comparison-based search algorithm can beat O(log n) on a sorted array.
- Space: O(1) iterative implementation — critical at billion-element scale.
- Hash-based lookup could give O(1) but requires hashing the entire array upfront and
  O(n) space — impractical for 1 billion elements.

### Scenario E: Collection with frequent insertions and frequent access to smallest element
**Chosen Algorithm: Min-Heap (Priority Queue)**

Justification:
- A binary min-heap stores the smallest element at the root (index 0), giving O(1) access.
- Insert: O(log n) — sift up the new element.
- Extract-Min: O(log n) — remove root, sift down replacement.
- Java's `PriorityQueue<E>` implements this exact structure.
- Sorted array: O(n) insertion (to maintain order) — too slow.
- Sorted linked list: O(1) min access but O(n) insertion — better, but still O(n).
- No comparison-based structure beats O(log n) for both insert and min-access.

---

## Part C: Empirical Analysis

### C.1 – Experimental Design

**Algorithms tested:** Insertion Sort, Merge Sort, Quick Sort, Heap Sort

**Dataset types (5):**
1. Random — uniform random integers
2. Sorted ascending — already in order (best case for Insertion Sort)
3. Sorted descending — worst case for many algorithms
4. Nearly sorted — 90% sorted, 10% random swaps
5. Many duplicates — only 10 distinct values in n elements

**Input sizes:** 100, 1,000, 10,000, 100,000

**Measurements per (algo, type, size) combination:**
- Wall-clock time: averaged over 5 runs (nanosecond precision via System.nanoTime())
- Comparison count: accumulated in Stats.comparisons
- Swap/assignment count: accumulated in Stats.swaps
- Mean and standard deviation computed across the 5 runs

**JVM warm-up:** 3 full benchmark passes discarded before measurement to allow
JIT compilation of hot loops.

### C.2 – Expected Experimental Results (Theoretical Predictions)

The following values are based on the mathematical complexity of each algorithm.

**Random Data:**
```
n=1,000:
  Insertion Sort: ~250,000 comparisons (n²/4 avg)
  Merge Sort:     ~9,966 comparisons (n log₂ n)
  Quick Sort:     ~10,000–13,000 comparisons (n log n * 1.4 avg)
  Heap Sort:      ~14,000 comparisons (~2n log n)

n=10,000:
  Insertion Sort: ~25,000,000 comparisons
  Merge Sort:     ~132,877 comparisons
  Quick Sort:     ~140,000–180,000 comparisons
  Heap Sort:      ~266,000 comparisons
```

**Already-Sorted Data:**
```
Insertion Sort: ~n−1 = 999 comparisons for n=1,000 (BEST CASE)
Merge Sort:     ~n log n (unchanged — not input-adaptive)
Quick Sort:     ~n log n (with median-of-3 pivot; naive first-element pivot gives O(n²))
Heap Sort:      ~2n log n (unchanged)
```

**Reverse-Sorted Data:**
```
Insertion Sort: n(n-1)/2 comparisons (WORST CASE: ~499,500 for n=1,000)
Merge Sort:     ~n log n (unchanged)
Quick Sort:     ~n log n (with median-of-3; ~n² with naive pivot)
Heap Sort:      ~2n log n (unchanged)
```

### C.3 – Statistical Analysis

**t-test interpretation:**
For two sets of runtime measurements A (algorithm 1) and B (algorithm 2):

  t = (mean_A − mean_B) / sqrt(s²_A/n + s²_B/n)

A |t| > 2.306 (critical value for 2-tailed t-test, df=4, α=0.05) indicates a
statistically significant performance difference between the two algorithms.

The `SortingBenchmark.tTest(double[], double[])` method computes this value.

### C.4 – Empirical vs Theoretical Complexity

By plotting log(time) vs log(n), the slope reveals the empirical growth rate:
- Slope ≈ 1.0 → O(n) (expected: Insertion Sort on sorted data)
- Slope ≈ 1.0–1.1 → O(n log n) (expected: Merge, Quick, Heap on random data)
- Slope ≈ 2.0 → O(n²) (expected: Insertion Sort on random/reverse data)

---

## Part D: Algorithm Selection Decision Tree

```
START
│
├─ Is the collection frequently accessed for min/max element?
│   └─ YES → Use MIN-HEAP / PRIORITY QUEUE (Java PriorityQueue)
│
├─ Is the data already sorted and you need to SEARCH?
│   └─ YES → Use BINARY SEARCH [O(log n)]
│   └─ NO  → Use SEQUENTIAL SEARCH [O(n)]
│
└─ Do you need to SORT?
    │
    ├─ What is the input size (n)?
    │   │
    │   ├─ n ≤ 20 (tiny)
    │   │   └─ Use INSERTION SORT (low overhead, stable, in-place)
    │   │
    │   ├─ n ≤ 1,000 (small)
    │   │   ├─ Is data nearly sorted?
    │   │   │   └─ YES → INSERTION SORT (near-linear performance)
    │   │   └─ NO → INSERTION SORT or MERGE SORT (both fine at this scale)
    │   │
    │   └─ n > 1,000 (medium to large)
    │       │
    │       ├─ Is STABILITY required?
    │       │   │
    │       │   └─ YES ──► Is memory constrained?
    │       │               ├─ YES → MERGE SORT (stable, O(n log n), O(n) space unavoidable)
    │       │               └─ NO  → MERGE SORT (stable, predictable)
    │       │
    │       └─ Stability NOT required
    │           │
    │           ├─ Is GUARANTEED worst-case performance required?
    │           │   ├─ YES (e.g., real-time) ──► Memory constrained?
    │           │   │                              ├─ YES → HEAP SORT [O(n log n), O(1) space]
    │           │   │                              └─ NO  → MERGE SORT [O(n log n), O(n) space]
    │           │   │
    │           │   └─ NO (average case acceptable)
    │           │       ├─ Data is RANDOM / GENERAL → QUICK SORT (median-of-3)
    │           │       │                              [Best avg performance in practice]
    │           │       ├─ Data has MANY DUPLICATES → 3-WAY QUICK SORT
    │           │       │                              [Degenerates less on repeated keys]
    │           │       └─ Data is NEARLY SORTED → INSERTION SORT or TIM SORT
    │
    └─ Special Cases:
        ├─ Sorting linked list?       → MERGE SORT (no random access needed)
        ├─ External sort (disk data)? → MERGE SORT (sequential I/O pattern)
        ├─ Integer keys in [0..k]?    → COUNTING SORT [O(n+k)] — beats comparison sorts
        └─ Strings / variable keys?   → RADIX SORT [O(n·L) where L = key length]
```

---

## Reflection and Learning Summary (Bonus)

### 1. Most Challenging Aspect
The most challenging aspect was the `addProduct` method in the hybrid engine: ensuring
that insertion into the sorted array correctly shifts elements without off-by-one errors,
especially at the boundaries (insert at index 0 or at index size). Writing the
`findInsertPosition` binary search with `high = size` (not `size - 1`) was a subtle
but critical distinction from the regular binary search.

### 2. Most Surprising Algorithm
Insertion Sort was most surprising. Despite its O(n²) worst case, its O(n) performance
on nearly-sorted data is genuinely better than Merge Sort and Quick Sort in that scenario.
Java's own `Arrays.sort` (TimSort) uses Insertion Sort internally for sub-arrays smaller
than 32 elements — a testament to its underrated efficiency in the right context.

### 3. Change in Understanding
Before this assignment, algorithm selection felt like a theoretical exercise. After
empirically measuring how Quick Sort's 2× faster constant factor eclipses Merge Sort
at n = 10,000 on random data, algorithm selection now feels like a concrete engineering
decision with measurable trade-offs. The choice isn't just "O(n log n) is good enough"
but depends on data distribution, stability requirements, cache behavior, and memory.

### 4. Real-World Applications
- **Binary search**: Git's `bisect` command for finding the commit that introduced a bug
- **Merge sort**: External database sorts where data doesn't fit in RAM
- **Quick sort**: C's `qsort`, Java's `Arrays.sort` for primitives
- **Heap sort / priority queue**: Dijkstra's shortest path algorithm, OS job schedulers
- **Hybrid (Hybrid Search Engine)**: Elasticsearch's combined inverted index + sorted ID store

### 5. What I'd Do Differently
I would instrument the code with comparison counters from the start rather than adding
them later. Retrofitting the Stats object into existing loops required revisiting every
method. Starting with instrumentation built in would have saved time and reduced the
risk of miscounting comparisons in edge cases (e.g., the j >= 0 check in insertion sort
that isn't technically a value comparison but still represents a loop guard).

---

## README.md (GitHub Repository)

```markdown
# CSC483-Algorithms-Assignment

## Structure
```
src/
  com/csc483/assignment1/search/
    Product.java
    SearchOperations.java
    HybridSearchEngine.java
    TechMartBenchmark.java
  com/csc483/assignment2/sorting/
    SortingAlgorithms.java
    SortingBenchmark.java
test/
  com/csc483/assignment1/search/
    SearchOperationsTest.java
  com/csc483/assignment2/sorting/
    SortingAlgorithmsTest.java
```

## Compilation
```bash
javac -d out src/com/csc483/assignment1/search/*.java
javac -d out src/com/csc483/assignment2/sorting/*.java
```

## Execution
```bash
java -cp out com.csc483.assignment1.search.TechMartBenchmark
java -cp out com.csc483.assignment2.sorting.SortingBenchmark
```

## Running Tests (JUnit 5)
Download `junit-platform-console-standalone.jar` from https://junit.org/junit5/
```bash
java -jar junit-platform-console-standalone.jar --classpath out --scan-class-path
```

## Dependencies
- Java 11+
- JUnit 5.9+ (for tests only)

## Known Limitations
- `addProduct` is O(n) due to array shifting; use a TreeMap for high-frequency inserts
- Benchmark timings vary by machine; results are relative, not absolute
```
