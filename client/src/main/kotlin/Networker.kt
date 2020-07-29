import frontend.components.NodeCached
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import packet.*
import tornadofx.runLater

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
		writer.writePacketNameChange(PacketNameChange(username))
		writer.writePacketGoTo(PacketGoTo(NodeId(1L)))//TODO: dont hardcode root ID
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
		if (nodeRevelation.nodeId.value == 1L) { //TODO: dont hardcode root ID
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
		writer.writePacketNodeCreate(
			PacketNodeCreate(
				parent.id,
				text
			)
		)
	}

	suspend fun goTo(target: NodeCached) {
		writer.writePacketGoTo(PacketGoTo(target.id))
	}
}
