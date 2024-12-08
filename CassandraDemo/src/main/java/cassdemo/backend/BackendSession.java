package cassdemo.backend;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static PreparedStatement SELECT_FROM_NAMES;
	private static PreparedStatement INSERT_TO_NAMES;
	private static PreparedStatement DELETE_NAME;

	private static PreparedStatement UPDATE_SECOND;
	private static PreparedStatement SELECT_SECOND;

	private static PreparedStatement ADD_COUNTER;


	private static final String USER_FORMAT = "- %-10s  %-16s %-10s %-10s\n";
	// private static final SimpleDateFormat df = new
	// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private void prepareStatements() throws BackendException {
		try {
//			SELECT_FROM_NAMES = session.prepare("SELECT * FROM nicks WHERE nick = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
//			INSERT_TO_NAMES = session.prepare("INSERT INTO nicks (nick, id) VALUES (?, ?)").setConsistencyLevel(ConsistencyLevel.QUORUM);
//			DELETE_NAME = session.prepare("DELETE FROM nicks WHERE nick = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
//			UPDATE_SECOND = session.prepare("UPDATE czwarte set col1 = ?, col2 =? WHERE id = 0");
//			SELECT_SECOND = session.prepare("SELECT * FROM czwarte WHERE id = 0");
			ADD_COUNTER = session.prepare("UPDATE licznik SET ctr = ctr + 1 WHERE id = ?" ).setConsistencyLevel(ConsistencyLevel.ONE);

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

	public String selectName(String name) {
		BoundStatement bs = new BoundStatement(SELECT_FROM_NAMES);
		ResultSet resultSet = session.execute(bs.bind(name));
		if (!resultSet.isExhausted()) {
			Row row = resultSet.one();
			return row.getString("id");
		}
		return null; // Return null if the name is free
	}



	public void insertName(String name, String id) {
		BoundStatement bs = new BoundStatement(INSERT_TO_NAMES);
		bs.bind(name, id);
		session.execute(bs);
	}

	public void deleteName(String name) {
		BoundStatement bs = new BoundStatement(DELETE_NAME);
		bs.bind(name);
		session.execute(bs);
	}

	public void setUpdateSecond(int id){
		BoundStatement bs = new BoundStatement(UPDATE_SECOND);
		bs.bind(id, -1*id);
		session.execute(bs);
	}
	public String selectSecond(){
		BoundStatement bs = new BoundStatement(SELECT_SECOND);
		ResultSet resultSet = session.execute(bs);
		if (!resultSet.isExhausted()) {
			Row row = resultSet.one();
			int col1 = row.getInt("col1");
			int col2 = row.getInt("col2");
			return "col1: " + col1 + ", col2: " + col2;
		}
		return null;
	}


	public void add_counter(int id){
		BoundStatement bs = new BoundStatement(ADD_COUNTER);
		bs.bind(id);

		session.execute(bs);

	}


}
