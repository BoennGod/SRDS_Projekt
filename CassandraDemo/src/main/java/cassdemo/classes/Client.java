package cassdemo.classes;

import java.util.*;

import cassdemo.backend.BackendSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.sleep;

public class Client implements Runnable {
    int id;
    private BackendSession session;
    String[] possible_parts = {"a", "b", "c", "d", "e"};
    Random random = new Random();

    private static final Logger logger = LoggerFactory.getLogger(BackendSession.class);

    public Client(BackendSession session, int id) {
        this.session = session;
        this.id = id;
    }

    private void generateTask(){
        int len = random.nextInt(5)+1;
        HashMap<String, String> parts = new HashMap<>();
        for(int i=0; i<len; i++){
            parts.put(possible_parts[random.nextInt(possible_parts.length)], "nd");
        }
        
        session.insertTask(id, parts, "nd");
    }

    @Override
    public void run() {
        generateTask();
        for(int i=0; i<10; i++){
            while(true) {
                logger.info(id +" Checking my order...");
                if (session.checkifTaskDone(id)) {
                    logger.info("My order is done.");
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