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
	val nodeId: NodeId,
	val own: Boolean,
	val snapshot: Snapshot,
	val parentId: NodeId
) : Packet(PacketId.NODE_REVEAL) {
	override suspend fun writePacketContent(writer: ByteWriteChannel) {
		writer.writeLong(nodeId.value)
		writer.writeBoolean(own)
		writer.writeString(snapshot.content)
		writer.writeInstant(snapshot.date)
		writer.writeLong(parentId.value)
	}
}

suspend fun ByteReadChannel.readPacketNodeReveal(): PacketNodeReveal {
	return PacketNodeReveal(
		nodeId = NodeId(this.readLong()),
		own = this.readBoolean(),
		snapshot = Snapshot(this.readString(), this.readInstant()),
		parentId = NodeId(this.readLong())
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
