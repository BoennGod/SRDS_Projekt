package cassdemo.classes;

import cassdemo.backend.BackendSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        System.out.println("Factory "+id+" Started a Factory");

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
                System.out.println(id + " has no available machines to lock.");
                break;
            }

            Machine machine = availableMachines.get(random.nextInt(availableMachines.size()));
            System.out.println(id+": Attempting to lock machine: " + machine);

            boolean locked = session.lockMachine(id, machine.getMachineId());

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            boolean loaded = id.equals(session.checkedLockedMachine(machine.getMachineId()));


            if (locked && loaded) {
                System.out.println(id+": Successfully locked machine with ID: " + machine.getMachineId());
                lockedMachines.add(machine);
                products.add(machine.getProductType());
                machines.remove(machine);
            } else {
                System.out.println(id+": Failed to lock machine with ID: " + machine.getMachineId() + ". Trying another...");
            }
        }

        System.out.println(id + " locked machines: " + lockedMachines);


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
                        System.out.println(id+": Trying to lock task "+task);
                        boolean locked = session.lockTask(id, task.getClientId());       //  try to lock it in

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        boolean loaded  = id.equals(session.checkedLockedTask(task.getClientId())); // check if truly locked

                        if (locked && loaded){
                            lockedTask = task;
                            chosen = true;
                            break;              //if done, exit
                        }
                    }
                }
            }

            // if no task was chosen, try again
            if (!chosen) {
                try {
                    Thread.sleep(10); // Sleep for 1 second
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
                        System.out.println("did task " + lockedTask);
                        break;
                    }
                }


                if (!products.contains(lockedTask.getNextProduct())){
                    if(lockedTask.checkIfAllPartsDone()){
                        System.out.println("Factory "+id+": Finished product "+lockedTask);
                        session.finishTask(lockedTask.getProductsNeeded(), lockedTask.getClientId());
                        break;
                    }
                    System.out.println("Factory "+id+": Did what I could, returning unfinished product "+lockedTask);
                    session.freeTask( lockedTask.getProductsNeeded() ,lockedTask.getClientId());
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
