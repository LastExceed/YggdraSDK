import database.Database
import io.ktor.network.sockets.*
import io.ktor.utils.io.writeBoolean
import kotlinx.coroutines.*
import packet.*
import org.jetbrains.exposed.sql.Database as ExposedDatabase
import packet.Packet.Companion.writePacket

class Server {
	private val database = Database(
		ExposedDatabase.connect("jdbc:sqlite:/data/database.db", "org.sqlite.JDBC")
	)
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

		if (reader.readByte() != PacketId.LOGIN.value) {
			error("first packet must be a namechange")
		}
		val login = reader.readPacketLogin()
		val newSession = Session(
			client,
			reader,
			writer,
			UserId(database.getOrCreateUser(nameChange.name)),
			NodeId(1L)//TODO: dont hardcode root ID
		)
		sessions.add(newSession)

		val map = mapOf(
			PacketId.NODE_CREATE to ::onPacketNodeCreate,
			PacketId.GOTO to ::onPacketGoTo
		)

		while (true) {
			val packetId = PacketId(newSession.reader.readByte())
			val packetHandler = map[packetId] ?: error("unknown packet ID: ${packetId.value}")
			packetHandler(newSession)
		}
	}

	private suspend fun onPacketGoTo(source: Session) {
		val jump = source.reader.readPacketGoTo()

		val node = database.getNode(jump.position)
			?: error("Node with ID ${jump.position.value} not found")

		source.position = jump.position
		node.children.forEach {
			val node = database.getNode(it)!!
			source.writer.writePacket(
				PacketNodeReveal(
					node.id,
					node.author == source.user,
					node.latestSnapshot,
					node.parentId!!
				)
			)
		}
	}

	private suspend fun onPacketNodeCreate(source: Session) {
		val nodeCreation = source.reader.readPacketNodeCreate()
		val newNode = database.createNode(
			source.user.value,
			nodeCreation.message,
			nodeCreation.parentId
		)

		val nodeRevelation = PacketNodeReveal(//TODO: use factory function
			newNode.id,
			false,
			newNode.latestSnapshot,
			newNode.parentId!!
		)
		sessions.filter { it.position == newNode.parentId }.forEach {
			val toSend =
				if (source == it) nodeRevelation.copy(own = true)
				else nodeRevelation

			it.writer.writePacket(toSend)
		}
	}
}
