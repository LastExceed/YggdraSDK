import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import packet.*
import packet.Packet.Companion.readPacket
import packet.Packet.Companion.writePacket
import java.net.*
import kotlin.random.*
import kotlin.test.*
import random.*
import java.time.*

const val testRepeats: Int = 100

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

		(1..testRepeats).map {
			val random = PacketGoToData()
			val packet = PacketGoTo(random.position)

			DynamicTest.dynamicTest(random.position.value.toString()) {
				writeAndReadPacket(packet, writer, reader)
			}
		}
	}

	data class PacketNameChangeData(
		val name: String = (CharPool.ASCII.value + '\n').random(Random.nextInt(1, Globals.userNameLimit))
	)

	private val nameChangeTestPort = 2
	@TestFactory
	fun writeAndReadPacketNameChange() = runBlocking {
		val address = InetSocketAddress("127.0.0.1", nameChangeTestPort)
		val listener = Globals.tcpSocketBuilder.bind(address)
		val writer = Globals.tcpSocketBuilder.connect(address)
			.openWriteChannel(true)
		val reader = listener.accept().openReadChannel()

		(1..testRepeats).map {
			val random = PacketNameChangeData()
			val packet = PacketNameChange(random.name)

			DynamicTest.dynamicTest(random.name) {
				writeAndReadPacket(packet, writer, reader)
			}
		}
	}

	data class PacketNodeCreateData(
		val parentId: NodeId = NodeId(Random.nextLong()),
		val message: String = (CharPool.ASCII.value + '\n').random(Random.nextInt(1, Globals.messageSizeLimit))
	)

	private val nodeCreateTestPort = 3
	@TestFactory
	fun writeAndReadPacketNodeCreate() = runBlocking {
		val address = InetSocketAddress("127.0.0.1", nodeCreateTestPort)
		val listener = Globals.tcpSocketBuilder.bind(address)
		val writer = Globals.tcpSocketBuilder.connect(address)
			.openWriteChannel(true)
		val reader = listener.accept().openReadChannel()

		(1..testRepeats).map {
			val random = PacketNodeCreateData()
			val packet = PacketNodeCreate(random.parentId, random.message)

			DynamicTest.dynamicTest("parentId: ${random.parentId.value} message: ${random.message}") {
				writeAndReadPacket(packet, writer, reader)
			}
		}
	}

	data class NodeData(
		val node: Node = Node(
			NodeId(Random.nextLong()),
			UserId(Random.nextLong()),
			Snapshot(
				(CharPool.ASCII.value + '\n').random(Random.nextInt(1, Globals.messageSizeLimit)),
				Instant.now() //TODO create random Instants
			),
			NodeId(Random.nextLong())
		)
	)

	private val nodeRevealTestPort = 12345
	@TestFactory
	fun writeAndReadPacketNodeReveal() = runBlocking {
		val address = InetSocketAddress("127.0.0.1", nodeRevealTestPort)
		val listener = Globals.tcpSocketBuilder.bind(address)
		val writer = Globals.tcpSocketBuilder.connect(address)
			.openWriteChannel(true)
		val reader = listener.accept().openReadChannel()

		(1..testRepeats).map {
			val random = NodeData().node
			val packet = PacketNodeReveal(random)

			DynamicTest.dynamicTest(
				"Id: ${random.id.value} author: ${random.author.value} parentId: ${random.parentId?.value}" +
				" snapshotData: ${random.latestSnapshot.date} snapshotContent: ${random.latestSnapshot.content}"
			) {
				writeAndReadPacket(packet, writer, reader)
			}
		}
	}
}
