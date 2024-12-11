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


    int id;
    private BackendSession session;

    private List<Machine> machines;
    private List<Machine> lockedMachines;

    public Factory(BackendSession session, int id) {
        this.session = session;
        this.id = id + 1;
        this.lockedMachines = new ArrayList<>();
    }

    @Override
    public void run() {
        Random random = new Random();

        System.out.println("Started a Factory " + id);
        machines = session.getMachines();

        while (lockedMachines.size() < 3) {
            List<Machine> availableMachines = new ArrayList<>();
            for (Machine machine : machines) {
                if (machine.getFactoryId() == 0) {
                    availableMachines.add(machine);
                }
            }
            if (availableMachines.isEmpty()) {
                System.out.println("No available machines to lock.");
                break;
            }
            System.out.println("got to availible machines");

            Machine machine = availableMachines.get(random.nextInt(availableMachines.size()));
            System.out.println("Attempting to lock machine: " + machine);

            boolean locked = session.lockMachine(id, machine.getMachineId());

            if (locked) {
                System.out.println("Successfully locked machine with ID: " + machine.getMachineId());
                lockedMachines.add(machine);
                machines.remove(machine);
            } else {
                System.out.println("Failed to lock machine with ID: " + machine.getMachineId() + ". Trying another...");
            }
        }

        System.out.println("Factory " + id + " locked machines: " + lockedMachines);
    }
}
