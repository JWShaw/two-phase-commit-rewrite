package model

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Duplex : ResourceManager.Connection,
    TransactionManager.Connection {

    private val _transactionManagerMessage = MutableSharedFlow<TransactionManager.Message>()
    override val transactionManagerMessage = _transactionManagerMessage

    override suspend fun broadcastState(state: ResourceManager.State) {
        _resourceManagerState.value = state
    }

    private val _resourceManagerState = MutableStateFlow(ResourceManager.State.DISCONNECTED)
    override val resourceManagerState: StateFlow<ResourceManager.State>
        get() = _resourceManagerState

    override suspend fun broadcastMessage(msg: TransactionManager.Message) {
        _transactionManagerMessage.emit(msg)
    }
}