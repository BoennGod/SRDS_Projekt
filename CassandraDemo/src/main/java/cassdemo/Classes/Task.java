package cassdemo.classes;

import java.util.List;
import java.util.Map;
import cassdemo.classes.Product;

public class Task {
    private int clientId;
    private int factoryId;
    private Map<String, String> productsNeeded;
    private String taskStatus;

    public Task(int clientId, int factoryId, Map<String, String> productsNeeded) {
        this.clientId = clientId;
        this.factoryId = factoryId;
        this.productsNeeded = productsNeeded;
    }

    // Getters and setters
    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getfactoryId() {
        return factoryId;
    }

    public void setfactoryId(int factoryId) {
        this.factoryId = factoryId;
    }

    public Map<String, String> getProductsNeeded() {
        return productsNeeded;
    }

    public void setProductsNeeded(Map<String, String> productsNeeded) {
        this.productsNeeded = productsNeeded;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    @Override
    public String toString() {
        return "Task{" +
                "clientId='" + clientId + '\'' +
                "factoryId='" + factoryId + '\'' +
                ", productsNeeded=" + productsNeeded +
                ", taskStatus='" + taskStatus + '\'' +
                '}';
    }
}