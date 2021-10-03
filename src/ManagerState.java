public enum ManagerState {
    // RM States
    WORKING, PREPARED, COMMITTED, ABORTED,

    // TM States
    INITIALIZING, PREPARING;
}