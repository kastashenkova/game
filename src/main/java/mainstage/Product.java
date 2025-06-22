package mainstage;

/**
 * Represents a product available in a shop or cafe, with its basic attributes.
 */
public class Product {
    private final String name;
    private final String imagePath;
    private final int price;
    private final int nutrition;

    /**
     * Constructs a new {@code Product} with the specified name, price, nutrition value, and image path.
     *
     * @param name      The name of the product.
     * @param price     The price of the product.
     * @param nutrition The nutritional value provided by the product.
     * @param imagePath The file path to the product's image.
     */
    public Product(String name, int price, int nutrition, String imagePath) {
        this.name = name;
        this.imagePath = imagePath;
        this.price = price;
        this.nutrition = nutrition;
    }

    /**
     * Returns the name of the product.
     *
     * @return The product's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the file path to the product's image.
     *
     * @return The image file path.
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Returns the price of the product.
     *
     * @return The product's price.
     */
    public int getPrice() {
        return price;
    }

    /**
     * Returns the nutritional value provided by the product.
     *
     * @return The product's nutrition value.
     */
    public int getNutrition() {
        return nutrition;
    }
}