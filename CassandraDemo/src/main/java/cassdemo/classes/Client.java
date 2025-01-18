package cassdemo.classes;

import java.util.*;

import cassdemo.backend.BackendSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.sleep;

public class Client implements Runnable {
    int id;
    private BackendSession session;
    String[] possible_parts = {"A", "B", "C"};
    Random random = new Random();

    private static final Logger logger = LoggerFactory.getLogger(BackendSession.class);

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
        System.out.println("Client " + id +": Checking my order:: " + task);
        System.out.println(task);
        return task.getTaskStatus().equals("Done");
    }

    @Override
    public void run() {
        generateTask();
        for(int i=0; i<10; i++){
            while(true) {
                if (checkTaskDone()) {
                    System.out.println("Client " + id +": My order is done.");
                    session.deleteTask(id);
                    try {
                        sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;

                }
                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            generateTask();
        }
    }

}