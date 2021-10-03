import java.util.ArrayList;
import java.util.List;

public class Simulation {
    private List<ResourceManager> resourceManagers;
    private TransactionManager transactionManager;

    public Simulation(TransactionManager tm) {
        resourceManagers = new ArrayList<>();
    }

    public void addResourceManager(ResourceManager rm) {
        resourceManagers.add(rm);
        transactionManager.registerNewClient(rm.getId());
    }

    public void run() {

    }
}
