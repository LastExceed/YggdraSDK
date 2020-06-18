import database.Database
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.writeBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import packet.*

class Server {
	//private val rootId = IdDispenser.next()
	//private val tree = mutableMapOf(rootId to Node(rootId, "rootuser", "this is the root node", NodeId(-1)))
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
			client.dispose()
			return
		}

		if (reader.readByte() != PacketId.NAMECHANGE.value) {
			error("first packet must be a namechange")
		}
		val nameChange = reader.readPacketNameChange()
		Database.getOrCreateUser(nameChange.name)
		val newSession = Session(
			client,
			reader,
			writer,
			nameChange.name,
			IdNode(1L)//TODO: dont hardcode root ID
		)
		sessions.add(newSession)

		val map = mapOf(
			PacketId.NODE_CREATE to ::onPacketNodeCreate,
			PacketId.GOTO to ::onPacketGoTo
		)

		while (true) {
			val packetID = PacketId(newSession.reader.readByte())
			val packetHandler = map[packetID] ?: error("unknown packet ID: $packetID")
			packetHandler(newSession)
		}
	}

	private suspend fun onPacketGoTo(source: Session) {
		val jump = source.reader.readPacketGoTo()

		val node = Database.getNode(jump.position)
			?: error("Node with ID ${jump.position} not found")

		source.position = jump.position
		node.children.forEach {
			source.writer.writePacketNodeReveal(PacketNodeReveal(Database.getNode(it)!!))
		}
	}

	private suspend fun onPacketNodeCreate(source: Session) {
		val nodeCreation = source.reader.readPacketNodeCreate()
		val newNode = Database.createNode(
			Database.getUserID(source.username),
			nodeCreation.message,
			nodeCreation.parentId
		)

		val nodeRevelation = PacketNodeReveal(newNode)
		sessions.filter { it.position == newNode.parentId }.forEach {
			it.writer.writePacketNodeReveal(nodeRevelation)
		}
	}
}
