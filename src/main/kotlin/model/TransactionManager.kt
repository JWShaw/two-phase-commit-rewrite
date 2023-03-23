package model

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TransactionManager {

    private var _state = State.WORKING
    val state
        get() = _state

    private val connections = mutableListOf<Connection>()

    suspend fun connectResourceManager(connection: Connection) = coroutineScope {
        connections.add(connection)
        launch {
            connection.resourceManagerState.collect { state ->
                when (state) {
                    ResourceManager.State.PREPARED -> prepare()
                    ResourceManager.State.ABORTED -> abort()
                    else -> Unit
                }
            }
        }
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
        }
    }

    private suspend fun commit() {
        _state = State.COMMITTED
        connections.forEach { it.broadcastMessage(Message.COMMIT) }
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