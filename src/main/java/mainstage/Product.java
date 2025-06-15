package mainstage;

public class Product {
    private final String name;
    private final String imagePath;
    private final int price;
    private final int nutrition;

    public Product(String name, int price, int nutrition, String imagePath) {
        this.name = name;
        this.imagePath = imagePath;
        this.price = price;
        this.nutrition = nutrition;
    }

    public String getName() { return name; }
    public String getImagePath() { return imagePath; }
    public int getPrice() { return price; }
    public int getNutrition() { return nutrition; }
}



