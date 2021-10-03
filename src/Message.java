public enum Message {
    PREPARE, COMMIT, ABORT, // TM -> RM messages
    PREPARED, COMMITTED, ABORTED; // RM -> TM messages
}
