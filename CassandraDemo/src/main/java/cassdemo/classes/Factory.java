package cassdemo.classes;

import cassdemo.backend.BackendSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Factory implements Runnable {
    //1. machines
    // 1.1 choose which you want
    // 1.2 take what you want and check if you've taken it
    // 1.3 if not/ choose other machine go to step 1
    //
    //2. tasks
    // 2.1 check what tasks are availible
    // 2.2 if there's one that I can do, take this task
    // 2.3 assign this task to the machine
    // 2.4 after doing everything I can do on this task, free it for the others
    //
    private static final Logger logger = LoggerFactory.getLogger(Factory.class);

    private final BackendSession session;

    String id;
    private List<String> products;
    private List<Machine> lockedMachines;

    public Factory(BackendSession session, int id, int nodeId) {
        this.session = session;
        this.id = "factory-" + id + "-node-" + nodeId;
        this.lockedMachines = new ArrayList<>();
    }

    @Override
    public void run() {
        List<Machine> machines;
        Random random = new Random();
        products = new ArrayList<>();
        logger.info("{} Started a Factory", id);

        // machines
        machines = session.getMachines();

        // see what machines are already assigned to us
        for (Machine machine : machines) {
            if (id.equals(machine.getFactoryId())) {
                lockedMachines.add(machine);
                products.add(machine.getProductType());
            }
        }
        // try to assign at least 3 machines
        while (lockedMachines.size() < 3) {
            List<Machine> availableMachines = new ArrayList<>();
            for (Machine machine : machines) {
                if ("0".equals(machine.getFactoryId()) && !products.contains(machine.getProductType())) {
                    availableMachines.add(machine);
                }
            }
            if (availableMachines.isEmpty()) {
                logger.info("{} has no available machines to lock.", id);
                break;
            }

            Machine machine = availableMachines.get(random.nextInt(availableMachines.size()));
            logger.info("{}: Attempting to lock machine: {}", id, machine);

            boolean locked = session.lockMachine(id, machine.getMachineId());

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            boolean loaded = id.equals(session.checkedLockedMachine(machine.getMachineId()));


            if (locked && loaded) {
                logger.info("SUCCESS {} locked machine with ID: {}", id, machine.getMachineId());
                lockedMachines.add(machine);
                products.add(machine.getProductType());
                machines.remove(machine);
            } else {
                logger.info("FAIL {} did not lock machine with ID: {}. Trying another...", id, machine.getMachineId());
            }
        }

        logger.info("{} locked machines: {}", id, lockedMachines);


        List<Task> tasks;
        boolean chosen;

        // tasks
        while(true){
            tasks = session.getTasks();
//            System.out.println(tasks);
            Task lockedTask = null;
            chosen = false;

            for (Task task : tasks) {                                   //check all tasks
                if ("0".equals(task.getFactoryId())||id.equals(task.getFactoryId())) {                        // if task is free
                    if (products.contains(task.getNextProduct())){    // and has available product we can produce
                        logger.info("{}: Trying to lock task {}", id, task);
                        boolean locked = session.lockTask(id, task.getClientId());       //  try to lock it in

                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        boolean loaded  = id.equals(session.checkedLockedTask(task.getClientId())); // check if truly locked

                        if (locked && loaded){
                            lockedTask = task;
                            chosen = true;
                            logger.info("SUCCESS {} locked task {}", id, task);
                            break;              //if done, exit
                        }
                        else{
                            logger.info("FAIL {} did not lock task {}", id, task);
                        }

                    }
                }
            }

            // if no task was chosen, try again
            if (!chosen) {
                try {
                    Thread.sleep(100); // Sleep for 1 second
                } catch (InterruptedException e) {
                    System.err.println(id + " was interrupted while waiting.");
                    Thread.currentThread().interrupt(); // Preserve the interrupt status
                }
                continue;
            }


            // create products needed for task
            while (true){
                for (Machine machine : lockedMachines) {
                    if (machine.getProductType().equals(lockedTask.getNextProduct())) {
                        try {
                            Thread.sleep(machine.getTime()* 10L);
                        } catch (InterruptedException e) {
                            System.err.println("Factory " + id + " was interrupted while waiting.");
                            Thread.currentThread().interrupt();
                        }
                        lockedTask.setNextProduct();
                        logger.info("{} did task {}", id ,lockedTask);
                        break;
                    }
                }


                if (!products.contains(lockedTask.getNextProduct())){
                    if(lockedTask.checkIfAllPartsDone()){
                        logger.info("{}: Finished product {}", id, lockedTask);
                        session.finishTask(lockedTask.getProductsNeeded(), lockedTask.getClientId());
                        break;
                    }
                    boolean loaded  = id.equals(session.checkedLockedTask(lockedTask.getClientId())); // check if truly locked

                    if (loaded){
                        logger.info("{}: Did what I could, returning unfinished product {}", id, lockedTask);
                        session.freeTask( lockedTask.getProductsNeeded() ,lockedTask.getClientId());
                        break;              //if done, exit
                    }
                    else{
                        logger.info("FAIL {} someone stole my task {}", id, lockedTask);
                    }

                    break;
                }


            }

//            break;
        }
        // while loop,
        // get all tasks
        // try to lock one eligible and then see if you took it,
        // do all products that you can
        // give back the task
    }
}
