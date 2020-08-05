import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import packet.*
import packet.Packet.Companion.readPacket
import java.net.InetSocketAddress
import kotlin.random.Random
import kotlin.test.*
import packet.Packet.Companion.writePacket
import random.*

class PacketTests {
	private fun writeAndReadPacket(packet: Packet, writer: ByteWriteChannel, reader: ByteReadChannel) = runBlocking {
		writer.writePacket(packet)
		val readPacket = reader.readPacket()
		assertNotNull(readPacket)
		assertNotNull(packet)
		assertEquals(packet, readPacket)
	}

	data class PacketGoToData(
		val position: NodeId = NodeId(Random.nextLong())
	)

	private val goToTestPort = 1
	@TestFactory
	fun writeAndReadPacketGoTo() = runBlocking {
		val address = InetSocketAddress("127.0.0.1", goToTestPort)
		val listener = Globals.tcpSocketBuilder.bind(address)
		val writer = Globals.tcpSocketBuilder.connect(address)
			.openWriteChannel(true)
		val reader = listener.accept().openReadChannel()

		(1..20).map {
			val random = PacketGoToData()
			val packet = PacketGoTo(random.position)

			DynamicTest.dynamicTest(random.position.value.toString()) {
				writeAndReadPacket(packet, writer, reader)
			}
		}
	}

	data class PacketNameChangeData(
		val name: String = (CharPool.ASCII.value + '\n').random(Random.nextInt(1,2000))
	)

	private val nameChangeTestPort = 2
	@TestFactory
	fun writeAndReadPacketNameChange() = runBlocking {
		val address = InetSocketAddress("127.0.0.1", nameChangeTestPort)
		val listener = Globals.tcpSocketBuilder.bind(address)
		val writer = Globals.tcpSocketBuilder.connect(address)
			.openWriteChannel(true)
		val reader = listener.accept().openReadChannel()

		(1..20).map {
			val random = PacketNameChangeData()
			val packet = PacketNameChange(random.name)

			DynamicTest.dynamicTest(random.name) {
				writeAndReadPacket(packet, writer, reader)
			}
		}
	}

}
