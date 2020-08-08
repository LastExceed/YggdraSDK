import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.Dispatchers
import java.net.InetSocketAddress

object Globals {
	const val protocolVersion = 1
	val serverAddress = InetSocketAddress("127.0.0.1", 12321)
	val tcpSocketBuilder = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
	const val messageSizeLimit = 2000
	const val emailSizeLimit = 40
	const val passwordSizeLimit = 40
}
