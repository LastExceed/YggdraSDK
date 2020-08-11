import database.Database
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import packet.*
import org.jetbrains.exposed.sql.Database as ExposedDatabase
import packet.Packet.Companion.writePacket

class Server(private val database: Database) {
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

		val login = handleLogin(reader, writer) ?: return client.dispose()

		val newSession = Session(
			client,
			reader,
			writer,
			login,
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

	private suspend fun handleLogin(reader: ByteReadChannel, writer: ByteWriteChannel): UserId? {
		if (reader.readByte() != PacketId.LOGIN.value) {
			error("first packet must be a login")
		}
		val login = reader.readPacketLogin()
		val userId = database.getUser(login.email, login.password)
		val authenticated = userId != null
		writer.writePacket(PacketLoginAcknowlegdement(authenticated))
		return userId
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
