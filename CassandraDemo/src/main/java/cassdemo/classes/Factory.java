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

    int id;
    private List<String> products;
    private List<Machine> lockedMachines;

    public Factory(BackendSession session, int id) {
        this.session = session;
        this.id = id + 1;
        this.lockedMachines = new ArrayList<>();
    }

    @Override
    public void run() {
        List<Machine> machines;
        Random random = new Random();

        System.out.println("Started a Factory " + id);

        // machines
        machines = session.getMachines();

        // see what machines are already assigned to us
        for (Machine machine : machines) {
            if (machine.getFactoryId() == id) {
                lockedMachines.add(machine);
                products.add(machine.getProductType());
            }
        }
        // try to assign at least 3 machines
        while (lockedMachines.size() < 3) {
            List<Machine> availableMachines = new ArrayList<>();
            for (Machine machine : machines) {
                if (machine.getFactoryId() == 0 && !products.contains(machine.getProductType())) {
                    availableMachines.add(machine);
                }
            }
            if (availableMachines.isEmpty()) {
                System.out.println("factory " + id + " has no available machines to lock.");
                break;
            }

            Machine machine = availableMachines.get(random.nextInt(availableMachines.size()));
            System.out.println("Attempting to lock machine: " + machine);

            boolean locked = session.lockMachine(id, machine.getMachineId());


            boolean loaded = id == session.checkedLockedMachine(machine.getMachineId());


            if (locked && loaded) {
                System.out.println("Successfully locked machine with ID: " + machine.getMachineId());
                lockedMachines.add(machine);
                products.add(machine.getProductType());
                machines.remove(machine);
            } else {
                System.out.println("Failed to lock machine with ID: " + machine.getMachineId() + ". Trying another...");
            }
        }

        System.out.println("Factory " + id + " locked machines: " + lockedMachines);


        List<Task> tasks;
        boolean chosen;

        // tasks
        while(true){
            tasks = session.getTasks();
            System.out.println(tasks);
            Task lockedTask = null;
            chosen = false;

            for (Task task : tasks) {                                   //check all tasks
                if (task.getFactoryId() == 0) {                         // if task is free
                    if (products.contains(task.getNextProduct())){    // and has available product we can produce
                        boolean locked = session.lockTask(id, task.getClientId());       //   try to lock it in

                        boolean loaded  = id == session.checkedLockedTask(task.getClientId()); // check if truly locked

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
                    Thread.sleep(1000); // Sleep for 1 second
                } catch (InterruptedException e) {
                    System.err.println("Factory " + id + " was interrupted while waiting.");
                    Thread.currentThread().interrupt(); // Preserve the interrupt status
                }
                continue;
            }


            // create products needed for task
            while (true){
                for (Machine machine : lockedMachines) {
                    if (machine.getProductType().equals(lockedTask.getNextProduct())) {
                        try {
                            Thread.sleep(machine.getTime()* 1000L);
                        } catch (InterruptedException e) {
                            System.err.println("Factory " + id + " was interrupted while waiting.");
                            Thread.currentThread().interrupt();
                        }
                        lockedTask.setNextProduct();
                        break;
                       /*
                            TUTAJ POTRZEBNY KOD NA SESSION.FREETASK
                        */

                    }
                }

               if (!products.contains(lockedTask.getNextProduct())){
                   session.lockTask(0, lockedTask.getClientId());
                   break;
               }


            }

            break;
        }
        // while loop,
        // get all tasks
        // try to lock one eligible and then see if you took it,
        // do all products that you can
        // give back the task
    }
}
