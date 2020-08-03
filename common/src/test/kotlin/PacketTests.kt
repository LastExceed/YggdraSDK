import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import packet.*
import java.net.InetSocketAddress
import kotlin.random.Random
import kotlin.test.*

class PacketTests {
	data class PacketGoToData(
		val position: NodeId = NodeId(Random.nextLong())
	)

	//TODO this is kinda dirty


	private val goToPort = 1
	@TestFactory
	fun writeAndReadPacketGoTo() = runBlocking {
		val address = InetSocketAddress("127.0.0.1", goToPort)
		val listener = Globals.tcpSocketBuilder.bind(address)
		val writer = Globals.tcpSocketBuilder.connect(address)
			.openWriteChannel(true)
		val reader = listener.accept().openReadChannel()

		(1..20).map {
			val random = PacketGoToData()
			DynamicTest.dynamicTest(random.position.value.toString()) {
				runBlocking {
					val packet = PacketGoTo(random.position)
					writer.writePacketGoTo(packet)
					assertEquals(reader.readByte(), PacketId.GOTO.value)
					val readPacket = reader.readPacketGoTo()

					assertNotNull(readPacket)
					assertNotNull(packet)
					assertEquals(packet, readPacket)
				}
			}
		}
	}
}
