package cassdemo.classes;

import java.util.List;

public class Task {
    private int clientId;
    private List<Product> productsNeeded;
    private String taskStatus;

    public Task(int clientId, List<Product> productsNeeded, String taskStatus) {
        this.clientId = clientId;
        this.productsNeeded = productsNeeded;
        this.taskStatus = taskStatus;
    }

    // Getters and setters
    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public List<Product> getProductsNeeded() {
        return productsNeeded;
    }

    public void setProductsNeeded(List<Product> productsNeeded) {
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
                ", productsNeeded=" + productsNeeded +
                ", taskStatus='" + taskStatus + '\'' +
                '}';
    }
}