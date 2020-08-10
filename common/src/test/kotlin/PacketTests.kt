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

	data class PacketLoginData(
		val email: String = (CharPool.ASCII.value + '\n').random(Random.nextInt(1, Globals.emailSizeLimit)),
		val password: String = (CharPool.ASCII.value + '\n').random(Random.nextInt(1, Globals.passwordSizeLimit))
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
			val random = PacketLoginData()
			val packet = PacketLogin(random.email, random.password)

			DynamicTest.dynamicTest("email: ${random.email}, password: ${random.password}") {
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
		val nodeId: NodeId = NodeId(Random.nextLong()),
		val own: Boolean = Random.nextBoolean(),
		val snapshot: Snapshot = Snapshot(
				(CharPool.ASCII.value + '\n').random(Random.nextInt(1, Globals.messageSizeLimit)),
				Instant.now() //TODO create random Instants
			),
		val parentId: NodeId = NodeId(Random.nextLong())
	)

	private val nodeRevealTestPort = 12345
	@TestFactory
	fun writeAndReadPacketNodeReveal() = runBlocking { //TODO redo
		val address = InetSocketAddress("127.0.0.1", nodeRevealTestPort)
		val listener = Globals.tcpSocketBuilder.bind(address)
		val writer = Globals.tcpSocketBuilder.connect(address)
			.openWriteChannel(true)
		val reader = listener.accept().openReadChannel()

		(1..testRepeats).map {
			val random = NodeData()
			val packet = PacketNodeReveal(random.nodeId, random.own, random.snapshot, random.parentId)

			DynamicTest.dynamicTest(
				"Id: ${random.nodeId.value} own: ${random.own} parentId: ${random.parentId.value}" +
				" snapshotData: ${random.snapshot.date} snapshotContent: ${random.snapshot.content}"
			) {
				writeAndReadPacket(packet, writer, reader)
			}
		}
	}
}
