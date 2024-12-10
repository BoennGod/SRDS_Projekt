package cassdemo.backend;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cassdemo.classes.Machine;
import java.util.ArrayList;
import java.util.List;

/*
 * For error handling done right see: 
 * https://www.datastax.com/dev/blog/cassandra-error-handling-done-right
 * 
 * Performing stress tests often results in numerous WriteTimeoutExceptions, 
 * ReadTimeoutExceptions (thrown by Cassandra replicas) and 
 * OpetationTimedOutExceptions (thrown by the client). Remember to retry
 * failed operations until success (it can be done through the RetryPolicy mechanism:
 * https://stackoverflow.com/questions/30329956/cassandra-datastax-driver-retry-policy )
 */

public class BackendSession {
	private static final Logger logger = LoggerFactory.getLogger(BackendSession.class);

	private Session session;

	public BackendSession(String contactPoint, String keyspace) throws BackendException {

		Cluster cluster = Cluster.builder().addContactPoint(contactPoint).build();
		try {
			session = cluster.connect(keyspace);
		} catch (Exception e) {
			throw new BackendException("Could not connect to the cluster. " + e.getMessage() + ".", e);
		}
		prepareStatements();
	}

	private static PreparedStatement GET_MACHINES;
	private static PreparedStatement LOCK_MACHINE;


	private static final String USER_FORMAT = "- %-10s  %-16s %-10s %-10s\n";
	// private static final SimpleDateFormat df = new
	// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private void prepareStatements() throws BackendException {
		try {
			GET_MACHINES = session.prepare("SELECT * FROM machines").setConsistencyLevel(ConsistencyLevel.ONE);
			LOCK_MACHINE = session.prepare("UPDATE machines SET factory_id = ? where id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);

	} catch (Exception e) {
			throw new BackendException("Could not prepare statements. " + e.getMessage() + ".", e);
		}

		logger.info("Statements prepared");
	}
	


	protected void finalize() {
		try {
			if (session != null) {
				session.getCluster().close();
			}
		} catch (Exception e) {
			logger.error("Could not close existing cluster", e);
		}
	}


	public List<Machine> getMachines(){
		System.out.println("got here alright");
		BoundStatement bs = new BoundStatement(GET_MACHINES);
		System.out.println("got here alright 2");
		ResultSet resultSet = session.execute(bs);
		System.out.println("got here alright 3");

		List<Machine> machineList = new ArrayList<>();

		for (Row row : resultSet) {
			int id = row.getInt("id");
			int factory = row.getInt("factory_id");
			String product = row.getString("product");
			int time = row.getInt("time");



			Machine machine = new Machine(id, factory,  product, time);
			machineList.add(machine);
		}
		return machineList;
	}
	public boolean lockMachine(int factory_id, int machineId) {
		BoundStatement bs = new BoundStatement(LOCK_MACHINE);
		bs.bind(factory_id, machineId);

		try {
			session.execute(bs);
			return true;
		} catch (Exception e) {

			System.err.println("Could not lock machine with id " + machineId + ": " + e.getMessage());
			return false;
		}
	}


}
