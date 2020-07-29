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
) : Packet(PacketId.NODE_REVEAL)

suspend fun ByteReadChannel.readPacketNodeReveal(): PacketNodeReveal {
	return PacketNodeReveal(
		nodeId = NodeId(this.readLong()),
		own = this.readBoolean(),
		snapshot = Snapshot(this.readString(), this.readInstant()),
		parentId = NodeId(this.readLong())
	)
}

suspend fun ByteWriteChannel.writePacketNodeReveal(packet: PacketNodeReveal) {
	this.writeByte(packet.id.value)

	this.writeLong(packet.nodeId.value)
	this.writeBoolean(packet.own)
	this.writeString(packet.snapshot.content)
	this.writeInstant(packet.snapshot.date)
	this.writeLong(packet.parentId.value)
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
