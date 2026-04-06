package com.csc483.assignment1.search;

/**
 * Represents a product in the TechMart electronics inventory.
 * Implements Comparable to allow natural ordering by productId.
 *
 * @author [Your Name]
 * @version 1.0
 */
public class Product implements Comparable<Product> {

    private final int productId;       // Unique identifier
    private final String productName;  // Human-readable product name
    private final String category;     // Product category (e.g., "Laptop", "Phone")
    private final double price;        // Price in USD
    private int stockQuantity;         // Current stock count

    /**
     * Constructs a Product with all required fields.
     *
     * @param productId     Unique integer identifier (1 to 200,000)
     * @param productName   Display name of the product
     * @param category      Category the product belongs to
     * @param price         Price in USD (must be >= 0)
     * @param stockQuantity Initial quantity in stock (must be >= 0)
     * @throws IllegalArgumentException if price or stockQuantity is negative
     */
    public Product(int productId, String productName, String category,
                   double price, int stockQuantity) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative.");
        if (stockQuantity < 0) throw new IllegalArgumentException("Stock cannot be negative.");
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // ──────────────── Getters ────────────────

    public int getProductId()       { return productId; }
    public String getProductName()  { return productName; }
    public String getCategory()     { return category; }
    public double getPrice()        { return price; }
    public int getStockQuantity()   { return stockQuantity; }

    public void setStockQuantity(int qty) {
        if (qty < 0) throw new IllegalArgumentException("Stock cannot be negative.");
        this.stockQuantity = qty;
    }

    /**
     * Natural ordering is by productId (ascending).
     * Required for binary search on a sorted array.
     */
    @Override
    public int compareTo(Product other) {
        return Integer.compare(this.productId, other.productId);
    }

    @Override
    public String toString() {
        return String.format(
            "Product{id=%-6d name='%-30s' category='%-12s' price=$%-8.2f stock=%d}",
            productId, productName, category, price, stockQuantity
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Product)) return false;
        Product other = (Product) obj;
        return this.productId == other.productId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(productId);
    }
}
