import java.util.List;

public class TransactionManager {

    private ManagerState state;
    private List<Integer> clientConnections;

    public void registerNewClient(int id) {
        clientConnections.add(id);
    }


}
