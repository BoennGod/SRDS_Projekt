package cassdemo;

import java.io.IOException;
import java.util.Properties;

import cassdemo.backend.BackendException;
import cassdemo.backend.BackendSession;

public class Main {

	private static final String PROPERTIES_FILENAME = "config.properties";

	public static void main(String[] args) throws IOException, BackendException {
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
		Thread[] threads =  new Thread[10];
		for (int i=0; i<10; i++){
			Thread thread = new Thread(new ChatSim(String.valueOf(i), session));
			threads[i] = thread;
			thread.start();

		}
		for (int i=0; i<10; i++){
			try {threads[i].join();}
			catch (InterruptedException e){
				throw new RuntimeException(e);
			}

		}

//		session.deleteAll();

		System.exit(0);
	}
}
