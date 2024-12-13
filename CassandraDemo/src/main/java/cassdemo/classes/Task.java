package cassdemo.classes;

import java.util.Map;

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

    public int getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(int factoryId) {
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


    public String getNextProduct() {
        for (Map.Entry<String, String> entry : productsNeeded.entrySet()) {
            if ("pending".equals(entry.getValue())) {                           //HERE SET PENDING TO WHATEVER WE DECIDED ON
                return entry.getKey();
            }
        }
        return null;
    }


    public String setNextProduct() {
        for (Map.Entry<String, String> entry : productsNeeded.entrySet()) {
            if ("pending".equals(entry.getValue())) {                           //HERE SET PENDING TO WHATEVER WE DECIDED ON
                productsNeeded.put(entry.getKey(), "done");                     //HERE SET DONE TO WHATEVER WE DECIDED ON
                return entry.getKey();
            }
        }
        return null;
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