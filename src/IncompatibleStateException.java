public class IncompatibleStateException extends java.lang.RuntimeException {

    public IncompatibleStateException(ManagerState oldState, ManagerState newState) {
        super("Tried to switch from " + oldState + " to " + newState);
    }
}
