package packet

import NodeId
import UserId
import Node
import Snapshot
import io.ktor.utils.io.*
import readString
import writeString
import java.time.Instant

data class PacketNodeReveal(
	val node: Node
) : Packet(PacketId.NODE_REVEAL) {
	override suspend fun writePacketContent(writer: ByteWriteChannel) {
		writer.writeLong(node.id.value)
		writer.writeLong(node.author.value)
		writer.writeString(node.latestSnapshot.content)
		writer.writeInstant(node.latestSnapshot.date)
		writer.writeLong(node.parentId?.value ?: error("tried to write root node"))
	}
}

suspend fun ByteReadChannel.readPacketNodeReveal(): PacketNodeReveal {
	return PacketNodeReveal(
		Node(
			id = NodeId(this.readLong()),
			author = UserId(this.readLong()),
			latestSnapshot = Snapshot(this.readString(), this.readInstant()),
			parentId = NodeId(this.readLong())
		)
	)
}

suspend fun ByteWriteChannel.writeInstant(instant: Instant) {
	this.writeLong(instant.epochSecond)
	this.writeInt(instant.nano)
}

suspend fun ByteReadChannel.readInstant(): Instant {
	val seconds = this.readLong()
	val nanos = this.readInt()
	return Instant.ofEpochSecond(seconds, nanos.toLong())
}
