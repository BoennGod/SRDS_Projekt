import cassdemo.backend.BackendException;
import cassdemo.backend.BackendSession;

class Factory implements Runnable {
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
    //

    String id;
    private BackendSession session;


    public Factory(BackendSession session, String id) {
        this.session = session;
        this.id = id;
    }





    private static final List<String> names = Arrays.asList("Borys", "Natalia", "Patryk", "Mateusz");




    @Override
    public void run() {
        Random random = new Random();
        for (int i=0 ;i<10000;i++){
            String selectedName = names.get(random.nextInt(names.size()));
            System.out.println(id + ": selected Name: " + selectedName);

            // Check if the name is taken
            String existingId = session.selectName(selectedName);
            if (existingId != null) {
                System.out.println(id + ": selected name " + selectedName + " but it is taken by " + existingId);
            }
            else {
                // Name is free, insert it with the id
                session.insertName(selectedName, id);
//				System.out.println(selectedName + " is free. Inserted into the table.");

                // Verify if the name is taken by us
                existingId = session.selectName(selectedName);
                if (existingId.equals(id)) {
                    System.out.println(id + ": " + selectedName + " is now taken by me");
                } else {
                    System.out.println(id + ": tried selecting " + selectedName + ", but it has been taken by " + existingId);
                    continue;
                }

                // Wait for 1 ms
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Free the nick by deleting it
                session.deleteName(selectedName);
                System.out.println(id + ": I freed " + selectedName);
                // Exit the main loop after successfully inserting and deleting
            }
        }
    }

}
