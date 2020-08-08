import io.ktor.network.sockets.Socket
import io.ktor.utils.io.*

class Session(
	val socket: Socket,
	val reader: ByteReadChannel,
	val writer: ByteWriteChannel,
	val user: UserId,
	var position: NodeId
)