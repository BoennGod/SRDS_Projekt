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
import cassdemo.Factory;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.ResultSet;




class Klient implements Runnable {
	int id;
	private BackendSession session;

	public ChatSim2(BackendSession session, int id) {
		this.session = session;
		this.id = id;
	}

	@Override
	public void run() {
		Random random = new Random();
		for(int i = 0;i<100000;i++){

			session.setUpdateSecond(id);
			session.selectSecond();

			String result = session.selectSecond();
			if (result != null) {
//				System.out.println("Selected Values: " + result);
				Pattern pattern = Pattern.compile("col1: (-?\\d+), col2: (-?\\d+)");
				Matcher matcher = pattern.matcher(result);

				if (matcher.find()) {
					int col1 = Integer.parseInt(matcher.group(1));
					int col2 = Integer.parseInt(matcher.group(2));

					if (col1 != -1* col2) {
						System.out.println("ANOMALIA " + col1 + " " + col2);
					}
				}

			}

		}
	}

}



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


		// ex 1
//		Thread[] arr = new Thread[100];
//		for (int i =0; i<100;i++){
//			ChatSim chas = new ChatSim(session, ("Borys " + i));
//			Thread titus = new Thread(chas);
//			arr[i] = titus;
//			titus.start();
//		}
//		for (int i =0; i<100;i++){
//			try{
//				arr[i].join();
//			}catch (Exception e){
//
//			}
//		}
		//ex 2
		Thread[] arr = new Thread[100];
		for (int i =0; i<1000;i++){
			ChatSim2 chaos = new ChatSim2(session, i);
			Thread titus = new Thread(chaos);
			arr[i] = titus;
			titus.start();
		}
		for (int i =0; i<1000;i++){
			try{
				arr[i].join();
			}catch (Exception e){

			}
		}

		System.exit(0);
	}
}
