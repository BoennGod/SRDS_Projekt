package cassdemo;

import java.io.IOException;
import java.util.Properties;

import cassdemo.classes.Client;
import cassdemo.backend.BackendException;
import cassdemo.backend.BackendSession;
import cassdemo.classes.Factory;


public class Main {

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


		int numthreads = 1;

        Thread[] clients = new Thread[numthreads];
        Thread[] factories = new Thread[numthreads];
        
        for (int i =0; i<numthreads;i++){
            Client c = new Client(session, i+1);
            Factory f = new Factory(session, i+1);
            Thread titus1 = new Thread(c);
            Thread titus2 = new Thread(f);
            clients[i] = titus1;
            factories[i] = titus2;
            titus1.start();
			titus2.start();
        }
        for (int i =0; i<numthreads;i++){
            try{
                clients[i].join();
            }catch (Exception ignored){

            }
        }

		for (int i = 0; i<numthreads; i++){
			try{
                factories[i].join();
			}catch (Exception ignored){

			}
		}

		System.exit(0);
	}
}
