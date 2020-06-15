import frontend.components.ObservableNode
import io.ktor.network.sockets.*
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.ByteWriteChannel
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
		writer.writePacketGoTo(PacketGoTo(NodeId(0L)))
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
		if (newNode.parentId.value == -1L) {
			error("root revealed")
		}
		val newObservableNode = ObservableNode(
			id = newNode.id,
			author = newNode.author,
			message = newNode.message,
			parent = Tree.allNodes[newNode.parentId]!!
		)

		Tree.allNodes[newObservableNode.id] = newObservableNode

		runLater {
			newObservableNode.parent!!.children.add(newObservableNode)
		}
	}

	suspend fun createNode(parent: ObservableNode, text: String) {
		writer.writePacketNodeCreate(
			PacketNodeCreate(
				parent.id,
				text
			)
		)
	}

	suspend fun goTo(target: ObservableNode) {
		writer.writePacketGoTo(PacketGoTo(target.id))
	}
}