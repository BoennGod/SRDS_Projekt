package cassdemo.classes;

import java.util.*;

import cassdemo.backend.BackendSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.sleep;

public class Client implements Runnable {
    int id;
    private BackendSession session;
    String[] possible_parts = {"A", "B", "C","D","E","F","G","H","J","K"};
    Random random = new Random();

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public Client(BackendSession session, int id, int nodeId) {
        this.session = session;
        this.id = 10000* nodeId + id +1;
    }

    private void generateTask(){
        int len = random.nextInt(5)+1;
        Map<String, String> parts = new HashMap<>();
        for(int i=0; i<len; i++){
            parts.put("product"+possible_parts[random.nextInt(possible_parts.length)], "Pending");
        }
        session.insertTask(id, parts, "Pending");
    }

    public Boolean checkTaskDone(){
        Task task = session.selectTask(id);
        logger.info("Client {}: Checking my order: {}", id, task);
        return task.getTaskStatus().equals("Done");
    }

    @Override
    public void run() {
        generateTask();
        for(int i=0; i<20; i++){
            while(true) {
                if (checkTaskDone()) {
                    logger.info("Client {}: My order is done.", id);
                    session.deleteTask(id);
                    try {
                        sleep(300);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;

                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            generateTask();
        }
    }

}