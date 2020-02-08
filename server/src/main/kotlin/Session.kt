import io.ktor.network.sockets.Socket
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.ByteWriteChannel

class Session(
	val socket: Socket,
	val reader: ByteReadChannel,
	val writer: ByteWriteChannel,
	val username: String,
	var position: Node
)