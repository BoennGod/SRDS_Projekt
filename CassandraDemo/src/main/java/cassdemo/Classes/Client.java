package cassdemo.Classes;

import java.util.*;

import cassdemo.backend.BackendSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client implements Runnable {
    int id;
    private BackendSession session;

    private static final Logger logger = LoggerFactory.getLogger(BackendSession.class);

    public Client(BackendSession session, int id) {
        this.session = session;
        this.id = id;
    }

    @Override
    public void run() {
        session.insertTask(id, new HashMap<String, String>(Map.of("a","d", "b","d", "d","d")));
        boolean done = false;
        while(!done){
            session.selectTask(id);
            if (session.checkifTaskDone(id)){
                done = true;
            }
        }
        logger.info("My order is done.");
        session.deleteTask(id);
    }

}