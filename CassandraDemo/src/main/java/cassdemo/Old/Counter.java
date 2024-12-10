package cassdemo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cassdemo.backend.BackendException;
import cassdemo.backend.BackendSession;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.ResultSet;





class ChatSim3 implements Runnable {
    int id;
    private BackendSession session;
    private int exceptions;

    public ChatSim3(BackendSession session, int id) {
        this.session = session;
        this.id = id;
        this.exceptions = 0;
    }

    @Override
    public void run() {
        Random random = new Random();
        for(int i = 0;i<50000;i++){
            try {
                session.add_counter(id);
            }
            catch (Exception e){
                exceptions ++;
            }

        }
        System.out.println("Thread "+ id+ " caught "+ exceptions +" exceptions");
    }

}



public class Counter {

    private static final String PROPERTIES_FILENAME = "config.properties";

    public static void main(String[] args) throws IOException, BackendException, InterruptedException {

        String contactPoint = null;
        String keyspace = null;

        Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME));

            contactPoint = properties.getProperty("contact_point");
            keyspace = properties.getProperty("keyspace");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        BackendSession session = new BackendSession(contactPoint, keyspace);


        Thread[] arr = new Thread[10];
        for (int i =0; i<10;i++){
            ChatSim3 chaos = new ChatSim3(session, i);
            Thread titus = new Thread(chaos);
            arr[i] = titus;
            titus.start();
        }
        for (int i =0; i<10;i++){
            try{
                arr[i].join();
            }catch (Exception e){

            }
        }


        System.exit(0);
    }
}
