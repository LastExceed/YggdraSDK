import frontend.components.NodeCached
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import javafx.application.Application.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import packet.*
import packet.Packet.Companion.writePacket
import java.net.InetSocketAddress

class Networker(val address: InetSocketAddress) {
	private lateinit var socket: Socket
	private lateinit var reader: ByteReadChannel
	private lateinit var writer: ByteWriteChannel
	private var isRunning: Boolean = true

	suspend fun connect(email: String, password: String): Boolean {
		socket = Globals.tcpSocketBuilder.connect(address)
		reader = socket.openReadChannel()
		writer = socket.openWriteChannel(true)

		writer.writeInt(Globals.protocolVersion)
		val response = reader.readBoolean()
		if (!response) {
			error("outdated")
		}
		return login(email, password)
	}

	suspend fun handlePackets() {
		writer.writePacket(PacketGoTo(Globals.rootId))//TODO: dont hardcode root ID
		while (isRunning) {
			try {
				val packetID = PacketId(reader.readByte())
				when (packetID) { //use a map instead
					PacketId.NODE_REVEAL -> onPacketNodeReveal()

					else -> error("unknown packetId: $packetID")
				}
			} catch (e: Throwable) {
				println("Socket closed with message: ${e.message}")
			}
		}
	}

	fun stop() {
		try {
			isRunning = false
			socket.dispose()
		} catch (e: Throwable) {
			println(e.message)
		}
	} //TODO send disconnect package?

	private suspend fun login(email: String, password: String): Boolean {
		writer.writePacket(PacketLogin(email, password))
		val packetID = PacketId(reader.readByte())
		if(packetID != PacketId.LOGIN_ACKNOWLEDGEMENT) error("expected Login Acknowledgement packet")
		return reader.readPacketLoginAcknowledgement().authenticated
	}

	private suspend fun onPacketNodeReveal() {
		val nodeRevelation = reader.readPacketNodeReveal()
		if (nodeRevelation.nodeId == Globals.rootId) {
			error("root revealed")
		}
		val newNodeCached = NodeCached(
			id = nodeRevelation.nodeId,
			own = nodeRevelation.own,
			message = nodeRevelation.snapshot.content,
			parent = NodeCache.allNodes[nodeRevelation.parentId]!!
		)

		NodeCache.allNodes[newNodeCached.id] = newNodeCached

		withContext(Dispatchers.Main) {
			newNodeCached.parent!!.children.add(newNodeCached)
		}
	}

	suspend fun createNode(parent: NodeCached, text: String) {
		writer.writePacket(PacketNodeCreate(parent.id, text))
	}

	suspend fun goTo(target: NodeCached) {
		writer.writePacket(PacketGoTo(target.id))
	}
}
