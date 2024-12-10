package cassdemo.classes;

public class Product {
    private String product;
    private String status;

    public Product(String product, String status) {
        this.product = product;
        this.status = status;
    }

    // Getters and setters
    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "(" + product +", " + status + ')';
    }
}