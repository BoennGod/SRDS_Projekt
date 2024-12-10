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
import cassdemo.classes.Factory;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.ResultSet;



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

		Thread[] arr = new Thread[numthreads];

		for (int i = 0; i<numthreads; i++){
			Factory chaos = new Factory(session, i+1);
			Thread titus = new Thread(chaos);
			arr[i] = titus;
			titus.start();
		}
		for (int i = 1; i<numthreads; i++){
			try{
				arr[i].join();
			}catch (Exception e){

			}
		}

		System.exit(0);
	}
}
