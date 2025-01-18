package cassdemo.classes;

public class Machine {
    private int machineId;
    private String factoryId;
    private String productType;
    private int time;

    public Machine(int machineId, String factoryId, String productType, int time) {
        this.machineId = machineId;
        this.factoryId = factoryId;
        this.productType = productType;
        this.time = time;
    }

    // Getters and setters
    public int getMachineId() {
        return machineId;
    }

    public void setMachineId(int machineId) {
        this.machineId = machineId;
    }

    public String getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(String factoryId) {
        this.factoryId = factoryId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Machine{" +
                "machineId='" + machineId + '\'' +
                ", factoryId='" + factoryId + '\'' +
                ", productType='" + productType + '\'' +
                ", time=" + time +
                '}';
    }
}