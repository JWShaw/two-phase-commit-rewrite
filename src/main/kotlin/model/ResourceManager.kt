package model

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ResourceManager {

    private var _state = State.DISCONNECTED
    val state
        get() = _state

    private var transactionManagerConnection: Connection? = null

    suspend fun connectTransactionManager(connection: Connection) = coroutineScope {
        transactionManagerConnection = connection
        setState(State.WORKING)
        launch {
            connection.transactionManagerMessage.collect { msg ->
                when (msg) {
                    TransactionManager.Message.PREPARE -> setState(State.PREPARED)
                    TransactionManager.Message.COMMIT -> setState(State.COMMITTED)
                    TransactionManager.Message.ABORT -> setState(State.ABORTED)
                }
            }
        }
    }

    suspend fun prepare() {
        if (_state == State.WORKING) setState(State.PREPARED)
    }

    suspend fun abort() {
        if (_state == State.WORKING || _state == State.PREPARED) setState(State.ABORTED)
    }

    private suspend fun setState(state: State) {
        _state = state
        transactionManagerConnection?.broadcastState(state)
    }

    enum class State {
        DISCONNECTED, WORKING, PREPARED, COMMITTED, ABORTED
    }

    interface Connection {
        val transactionManagerMessage: Flow<TransactionManager.Message>
        suspend fun broadcastState(state: State)
    }
}