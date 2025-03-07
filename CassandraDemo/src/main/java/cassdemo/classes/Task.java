package cassdemo.classes;

import java.util.Map;

public class Task {
    private int clientId;
    private String factoryId;
    private Map<String, String> productsNeeded;
    private String taskStatus;

    public Task(int clientId, String factoryId, Map<String, String> productsNeeded, String taskStatus) {
        this.clientId = clientId;
        this.factoryId = factoryId;
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

    public String getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(String factoryId) {
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
            if ("Pending".equals(entry.getValue())) {                           //HERE SET PENDING TO WHATEVER WE DECIDED ON
                return entry.getKey();
            }
        }
        return null;
    }


    public String setNextProduct() {
        for (Map.Entry<String, String> entry : productsNeeded.entrySet()) {
            if ("Pending".equals(entry.getValue())) {                           //HERE SET PENDING TO WHATEVER WE DECIDED ON
                productsNeeded.put(entry.getKey(), "Done");                     //HERE SET DONE TO WHATEVER WE DECIDED ON
                return entry.getKey();
            }
        }
        return null;
    }

    public Boolean checkIfAllPartsDone(){
        for (Map.Entry<String, String> entry : productsNeeded.entrySet()) {
            if ("Pending".equals(entry.getValue())) {                           //HERE SET PENDING TO WHATEVER WE DECIDED ON
                return false;
            }
        }
        return true;
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