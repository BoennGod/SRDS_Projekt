package cassdemo.backend;

import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cassdemo.classes.Machine;
import cassdemo.classes.Task;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

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

    private static PreparedStatement INSERT_TO_TASKS;
    private static PreparedStatement DELETE_FROM_TASKS;
    private static PreparedStatement SELECT_FROM_TASKS;
   // private static PreparedStatement FINISH_PRODUCT;

	private static PreparedStatement GET_TASKS;
	private static PreparedStatement LOCK_TASK;
	private static PreparedStatement FREE_TASK;
	private static PreparedStatement FINISH_TASK;


	private static final String USER_FORMAT = "- %-10s  %-16s %-10s %-10s\n";
	// private static final SimpleDateFormat df = new
	// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private void prepareStatements() throws BackendException {
		try {
			GET_MACHINES = session.prepare("SELECT * FROM machines").setConsistencyLevel(ConsistencyLevel.ONE);
			LOCK_MACHINE = session.prepare("UPDATE machines SET factory_id = ? where id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
			GET_TASKS = session.prepare("SELECT * FROM tasks").setConsistencyLevel(ConsistencyLevel.ONE);
			LOCK_TASK = session.prepare("UPDATE tasks SET factory_id = ? where id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
			FREE_TASK = session.prepare("UPDATE tasks SET factory_id = 0, tasks = ? where id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
			FINISH_TASK = session.prepare("UPDATE tasks SET factory_id = 0, status = 'd' where id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);

            SELECT_FROM_TASKS = session.prepare("SELECT * FROM Tasks WHERE id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
            INSERT_TO_TASKS = session.prepare("INSERT INTO Tasks (id, factory_id, tasks, status) VALUES (?, ?, ?, ?)").setConsistencyLevel(ConsistencyLevel.QUORUM);
            DELETE_FROM_TASKS = session.prepare("DELETE FROM Tasks WHERE id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
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
        BoundStatement bs = new BoundStatement(GET_MACHINES);
        ResultSet resultSet = session.execute(bs);

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

	public Integer selectTask(Integer id){
		BoundStatement bs = new BoundStatement(SELECT_FROM_TASKS);
		ResultSet resultSet = session.execute(bs.bind(id));
		if (!resultSet.isExhausted()) {
			Row row = resultSet.one();
			return row.getInt("id");
		}
		return null;
	}

	public void insertTask(Integer id, HashMap<String, String> tasks, String status ){
		BoundStatement bs = new BoundStatement(INSERT_TO_TASKS);
		bs.bind(id, 0, tasks, status);
		session.execute(bs);
	}
//TODO: trzeba przetestowac z fabrykami, tutaj trzeba by podać całą mapę tasków zaktualizowaną przez fabrykę i ją całą wymieniamy w bazie (tak chyba bedzie najlatwiej)
	public void freeTask(HashMap<String, String> tasks, Integer task_id){
		BoundStatement bs = new BoundStatement(FREE_TASK);
		bs.bind(tasks, task_id);
		session.execute(bs);
	}
//TODO: trzeba przetestowac z fabrykami
	public void finishTask(Integer task_id){
		BoundStatement bs = new BoundStatement(FINISH_TASK);
		bs.bind(task_id);
		session.execute(bs);
	}

	public void deleteTask(Integer id){
		BoundStatement bs = new BoundStatement(DELETE_FROM_TASKS);
		bs.bind(id);
		session.execute(bs);
	}

	public Boolean checkifTaskDone(Integer id){
		BoundStatement bs = new BoundStatement(SELECT_FROM_TASKS);
		ResultSet resultSet = session.execute(bs.bind(id));
		Row row = resultSet.one();
		if(row.getString("status").equals("nd")){
			return false;
		}
		else
		{
			return true;
		}
	}

//To sprawdza po kolei w mapie zadan czy wszystko jest gotowe do wydania produktu
	public Boolean checkAllParts(Integer id){
		BoundStatement bs = new BoundStatement(SELECT_FROM_TASKS);
		ResultSet resultSet = session.execute(bs.bind(id));
		Row row = resultSet.one();
		Map<String, String> tasks = row.getMap("tasks", String.class, String.class);
		for(Map.Entry<String, String> task: tasks.entrySet()){
			if("nd".equals(task.getValue())){
				return false;
			}
		}
		return true;
	}


	public List<Task> getTasks(){
		BoundStatement bs = new BoundStatement(GET_TASKS);
		ResultSet resultSet = session.execute(bs);

		List<Task> taskList = new ArrayList<>();

		for (Row row : resultSet) {
			int id = row.getInt("id");
			int factory = row.getInt("factory_id");
			Map<String, String> Tasks = row.getMap("tasks", String.class, String.class);


			Task zadanie = new Task(id, factory, Tasks);
			taskList.add(zadanie);
		}
		return taskList;
	}


}
