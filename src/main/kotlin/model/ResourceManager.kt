package model

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

class ResourceManager {

    private var _state = State.DISCONNECTED
    val state
        get() = _state

    private var transactionManagerConnection: Connection? = null

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun connectTransactionManager(connection: Connection) {
        transactionManagerConnection = connection
        setState(State.WORKING)
        GlobalScope.launch {
            connection.transactionManagerMessage.collect { msg ->
                when (msg) {
                    TransactionManager.Message.PREPARE -> prepare()
                    TransactionManager.Message.COMMIT -> {
                        commit()
                        return@collect
                    }
                    TransactionManager.Message.ABORT -> {
                        abort()
                        return@collect
                    }
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

    private suspend fun commit() {
        setState(State.COMMITTED)
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