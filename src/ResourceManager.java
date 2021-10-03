import java.util.Random;

public class ResourceManager {

    private static int lastId = 0;

    private int id;
    private double abortProb;
    private ManagerState state;

    private Random rng = new Random();

    public ResourceManager(double abortProb) {
        this.id = lastId;
        lastId++;
        this.abortProb = abortProb;
        this.state = ManagerState.WORKING;
    }

    public void receiveBroadcast(Message msg) {
        if (msg == Message.PREPARE)
            prepare();
        else if (msg == Message.COMMIT)
            commit();
        else if (msg == Message.ABORT)
            abort();
        else
            throw new RuntimeException("Resource Manager " + id +
                " received incompatible message");
    }

    public int getId() {
        return id;
    }

    public void prepare() {
        double rand = rng.nextDouble();

        if (state != ManagerState.WORKING) {
            throw new IncompatibleStateException(state, ManagerState.PREPARED);
        }

        if (rand > abortProb) {
            changeState(ManagerState.PREPARED);
        } else {
            abort();
        }
    }

    private void commit() {
       if (state != ManagerState.PREPARED) {
           throw new IncompatibleStateException(state, ManagerState.COMMITTED);
       }

       changeState(ManagerState.COMMITTED);
    }

    public void abort() {
        if (state == ManagerState.COMMITTED) {
            throw new IncompatibleStateException(state, ManagerState.ABORTED)
        }

        changeState(ManagerState.ABORTED);
    }

    private void changeState(ManagerState st) {
        this.state = st;
    }
}
