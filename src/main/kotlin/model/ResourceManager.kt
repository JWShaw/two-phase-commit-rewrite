package model

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

class ResourceManager {

    private var _state = State.DISCONNECTED
    val state
        get() = _state

    private var transactionManagerConnection: Connection? = null
    private var listenJob: Job? = null

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun connectTransactionManager(connection: Connection) {
        transactionManagerConnection = connection
        _state = State.WORKING
        transactionManagerConnection?.broadcastState(state)
        listenJob = GlobalScope.launch {
            try {
                connection.transactionManagerMessage.collect { msg ->
                    when (msg) {
                        TransactionManager.Message.PREPARE -> prepare()
                        TransactionManager.Message.COMMIT -> commit()
                        TransactionManager.Message.ABORT -> abort()
                    }
                }
            } finally {
                listenJob?.cancel()
            }
        }
    }

    suspend fun prepare() {
        if (_state == State.WORKING) {
            _state = State.PREPARED
            transactionManagerConnection?.broadcastState(state)
        }
    }

    suspend fun abort() {
        if (_state == State.WORKING || _state == State.PREPARED) {
            _state = State.ABORTED
            transactionManagerConnection?.broadcastState(state)
            listenJob?.cancel()
        }
    }

    private suspend fun commit() {
        _state = State.COMMITTED
        transactionManagerConnection?.broadcastState(state)
        listenJob?.cancel()
    }

    enum class State {
        DISCONNECTED, WORKING, PREPARED, COMMITTED, ABORTED
    }

    interface Connection {
        val transactionManagerMessage: Flow<TransactionManager.Message>
        suspend fun broadcastState(state: State)
    }
}