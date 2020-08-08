import frontend.components.NodeCached
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import packet.*
import packet.Packet.Companion.writePacket

object Networker {
	private lateinit var socket: Socket
	private lateinit var reader: ByteReadChannel
	private lateinit var writer: ByteWriteChannel

	suspend fun connect(username: String) {
		socket = Globals.tcpSocketBuilder.connect(Globals.serverAddress)
		reader = socket.openReadChannel()
		writer = socket.openWriteChannel(true)

		writer.writeInt(Globals.protocolVersion)
		val response = reader.readBoolean()
		if (!response) {
			error("outdated")
		}
		writer.writePacket((PacketNameChange(username)))
		writer.writePacket(PacketGoTo(NodeId(1L)))//TODO: dont hardcode root ID
		while (true) {
			val packetID = PacketId(reader.readByte())
			when (packetID) { //use a map instead
				PacketId.NODE_REVEAL -> onPacketNodeReveal()

				else -> error("unknown packetId: $packetID")
			}
		}
	}

	private suspend fun onPacketNodeReveal() {
		val nodeRevelation = reader.readPacketNodeReveal()
		val newNode = nodeRevelation.node
		if (newNode.id.value == 1L) { //TODO: dont hardcode root ID
			error("root revealed")
		}
		val newNodeCached = NodeCached(
			id = newNode.id,
			author = newNode.author.value.toString(),
			message = newNode.latestSnapshot.content,
			parent = NodeCache.allNodes[newNode.parentId]!!
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
