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
	private static PreparedStatement CHECK_LOCKED_MACHINE;

    private static PreparedStatement INSERT_TO_TASKS;
    private static PreparedStatement DELETE_FROM_TASKS;
    private static PreparedStatement SELECT_FROM_TASKS;

	private static PreparedStatement GET_TASKS;
	private static PreparedStatement LOCK_TASK;
	private static PreparedStatement CHECK_LOCKED_TASK;

	private static PreparedStatement FREE_TASK;
	private static PreparedStatement FINISH_TASK;



	private void prepareStatements() throws BackendException {
		try {
			GET_MACHINES = session.prepare("SELECT * FROM machines").setConsistencyLevel(ConsistencyLevel.ONE);
			LOCK_MACHINE = session.prepare("UPDATE machines SET factory_id = ? WHERE id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
			CHECK_LOCKED_MACHINE = session.prepare("SELECT factory_id FROM machines WHERE id = ?").setConsistencyLevel(ConsistencyLevel.ONE);

			GET_TASKS = session.prepare("SELECT * FROM tasks").setConsistencyLevel(ConsistencyLevel.ONE);
			LOCK_TASK = session.prepare("UPDATE tasks SET factory_id = ? WHERE id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
			CHECK_LOCKED_TASK = session.prepare("SELECT factory_id FROM tasks WHERE id = ?").setConsistencyLevel(ConsistencyLevel.ONE);

			FREE_TASK = session.prepare("UPDATE tasks SET factory_id = '0', tasks = ? WHERE id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);
			FINISH_TASK = session.prepare("UPDATE tasks SET factory_id = '0', tasks = ?, status = 'Done' WHERE id = ?").setConsistencyLevel(ConsistencyLevel.QUORUM);

			SELECT_FROM_TASKS = session.prepare("SELECT * FROM Tasks WHERE id = ?").setConsistencyLevel(ConsistencyLevel.ONE);
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
            String factory = row.getString("factory_id");
            String product = row.getString("product");
            int time = row.getInt("time");

            Machine machine = new Machine(id, factory,  product, time);
            machineList.add(machine);
        }
        return machineList;
    }

    public boolean lockMachine(String factory_id, int machineId) {
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

    public Task selectTask(Integer id){
        BoundStatement bs = new BoundStatement(SELECT_FROM_TASKS);
        ResultSet resultSet = session.execute(bs.bind(id));
		Task task = null;
        if (!resultSet.isExhausted()) {
            Row row = resultSet.one();
            String factory_id = row.getString("factory_id");
			Map<String, String> tasks = row.getMap("tasks", String.class, String.class);
			String status =  row.getString("status");
			task = new Task(id, factory_id, tasks, status);
        }
		return task;
    }

    public void insertTask(Integer id, Map<String, String> tasks, String status ){
        BoundStatement bs = new BoundStatement(INSERT_TO_TASKS);
        bs.bind(id, "0", tasks, status);
        session.execute(bs);
    }

    public void freeTask(Map<String, String> tasks, Integer task_id){
        BoundStatement bs = new BoundStatement(FREE_TASK);
        bs.bind(tasks, task_id);
        session.execute(bs);
    }

    public void finishTask(Map<String, String> tasks, Integer task_id){
        BoundStatement bs = new BoundStatement(FINISH_TASK);
        bs.bind(tasks, task_id);
        session.execute(bs);
    }

	public void deleteTask(Integer id){
		BoundStatement bs = new BoundStatement(DELETE_FROM_TASKS);
		bs.bind(id);
		session.execute(bs);
	}

	public List<Task> getTasks(){
		BoundStatement bs = new BoundStatement(GET_TASKS);
		ResultSet resultSet = session.execute(bs);

		List<Task> taskList = new ArrayList<>();

		for (Row row : resultSet) {
			int id = row.getInt("id");
			String factory = row.getString("factory_id");
			Map<String, String> Tasks = row.getMap("tasks", String.class, String.class);
			String status = row.getString("status");

			Task task = new Task(id, factory, Tasks, status);
			taskList.add(task);
		}
		return taskList;
	}


	public boolean lockTask(String factory_id, int taskId) {
		BoundStatement bs = new BoundStatement(LOCK_TASK);
		bs.bind(factory_id, taskId);

		try {
			session.execute(bs);
			return true;
		} catch (Exception e) {
			System.err.println("Could not lock task with id " + taskId + ": " + e.getMessage());
			return false;
		}
	}

	public String checkedLockedMachine(int machineId) {
		return LockChecking(machineId, CHECK_LOCKED_MACHINE);
	}

	public String checkedLockedTask(int taskId) {
		return LockChecking(taskId, CHECK_LOCKED_TASK);
	}

	private String LockChecking(int id, PreparedStatement command) {
		BoundStatement bs = new BoundStatement(command);
		bs.bind(id);

		try {
			ResultSet resultSet = session.execute(bs);
			Row row = resultSet.one();

			if (row != null) {
				return row.getString("factory_id");
			} else {
				System.err.println("No factory_id found for id " + id);
				return "";
			}
		} catch (Exception e) {
			System.err.println("Could not retrieve factory_id for id " + id + ": " + e.getMessage());
			return "";
		}
	}

}
