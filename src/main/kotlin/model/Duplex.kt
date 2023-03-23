package model

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

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

fun main() {
    println("Starting..")
    val tm = TransactionManager()
    val rms = (1..5).map { ResourceManager() }
    val connections = runBlocking {
        rms.forEach { rm ->
            val duplex = Duplex()
            println("Connecting RM")
            tm.connectResourceManager(duplex)
            println("Connecting TM")
            rm.connectTransactionManager(duplex)
        }
    }
    println("Everything's connected!")
}