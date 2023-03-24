package model

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow

class TransactionManager {

    private var _state = State.WORKING
    val state
        get() = _state

    private val connections = mutableListOf<Connection>()
    private val jobs = mutableMapOf<Connection, Job>()

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun connectResourceManager(connection: Connection) {
        connections.add(connection)
        val job = GlobalScope.launch {
            try {
                connection.resourceManagerState.collect { state ->
                    when (state) {
                        ResourceManager.State.PREPARED -> prepare()
                        ResourceManager.State.ABORTED -> abort()
                        else -> Unit
                    }
                }
            } finally {
                jobs[connection]?.cancel()
            }
        }
        jobs[connection] = job
    }

    suspend fun prepare() {
        if (_state == State.WORKING) {
            _state = State.PREPARING
            connections.forEach { it.broadcastMessage(Message.PREPARE) }
        } else if (_state == State.PREPARING) {
            if (connections.all { it.resourceManagerState.value == ResourceManager.State.PREPARED }) {
                commit()
            }
        }
    }

    suspend fun abort() {
        if (_state == State.WORKING || _state == State.PREPARING) {
            _state = State.ABORTED
            connections.forEach { it.broadcastMessage(Message.ABORT) }
            connections.forEach { jobs[it]?.cancel() }
        }
    }

    private suspend fun commit() {
        _state = State.COMMITTED
        connections.forEach { it.broadcastMessage(Message.COMMIT) }
        connections.forEach { jobs[it]?.cancel() }
    }

    enum class State {
        WORKING, PREPARING, COMMITTED, ABORTED
    }

    enum class Message {
        PREPARE, COMMIT, ABORT
    }

    interface Connection {
        val resourceManagerState: StateFlow<ResourceManager.State>
        suspend fun broadcastMessage(msg: Message)
    }
}