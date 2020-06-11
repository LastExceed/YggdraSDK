import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import kotlinx.coroutines.*
import kotlinx.coroutines.io.writeBoolean
import packet.*
import java.net.InetSocketAddress

class Server {
	private val rootId = IdDispenser.next()
	private val tree = mutableMapOf(rootId to Node(rootId, "rootuser", "this is the root node", NodeId(-1)))
	private val sessions = mutableListOf<Session>()

	suspend fun start() {
		val listener = Globals.tcpSocketBuilder.bind(Globals.serverAddress)

		val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
		while (true) {
			val client = listener.accept()
			scope.launch {
				handleClient(client)
			}
		}
	}

	private suspend fun handleClient(client: Socket) {
		val reader = client.openReadChannel()
		val writer = client.openWriteChannel(true)

		val clientVersion = reader.readInt()
		val versionMatch = clientVersion == Globals.protocolVersion
		writer.writeBoolean(versionMatch)
		if (!versionMatch) {
			client.close()
			return
		}

		if (reader.readByte() != PacketId.NAMECHANGE.value) {
			error("first packet must be a namechange")
		}
		val nameChange = reader.readPacketNameChange()
		val newSession = Session(
			client,
			reader,
			writer,
			nameChange.name,
			tree[NodeId(0L)]!!
		)
		sessions.add(newSession)

		while (true) {
			val packetID = PacketId(newSession.reader.readByte())
			when (packetID) {
				PacketId.NODE_CREATE -> onPacketNodeCreate(newSession)
				PacketId.GOTO -> onPacketGoTo(newSession)
				else -> {
					error("unknown packet ID: $packetID")
				}
			}
		}
	}

	private suspend fun onPacketGoTo(source: Session) {
		val jump = source.reader.readPacketGoTo()
		source.position = tree[jump.position]!!
		source.position.children.forEach {
			source.writer.writePacketNodeReveal(PacketNodeReveal(it))
		}
	}

	private suspend fun onPacketNodeCreate(source: Session) {
		val nodeCreation = source.reader.readPacketNodeCreate()

		val newNode = Node(
			id = IdDispenser.next(),
			author = source.username,
			message = nodeCreation.message,
			parentId = nodeCreation.parentId
		)
		tree[newNode.id] = newNode
		val parent = tree[newNode.parentId]!!
		parent.children.add(newNode)

		val nodeRevelation = PacketNodeReveal(newNode)
		for (session in sessions) {
			if (session.position.id != newNode.parentId) continue
			session.writer.writePacketNodeReveal(nodeRevelation)
		}
	}
}