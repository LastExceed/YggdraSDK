package packet

import Globals
import IdNode
import IdUser
import Node
import Snapshot
import io.ktor.utils.io.*
import java.time.Instant

data class PacketNodeReveal(
	val node: Node
) : Packet(PacketId.NODE_REVEAL)

suspend fun ByteReadChannel.readPacketNodeReveal(): PacketNodeReveal {
	return PacketNodeReveal(
		Node(
			id = IdNode(this.readLong()),
			author = IdUser(this.readLong()),
			snapshot = Snapshot(this.readUTF8Line(Globals.messageSizeLimit)!!, this.readInstant()),
			parentId = IdNode(this.readLong())
		)
	)
}

suspend fun ByteWriteChannel.writePacketNodeReveal(packet: PacketNodeReveal) {
	this.writeByte(packet.id.value)

	this.writeLong(packet.node.id.value)
	this.writeLong(packet.node.author.value)
	this.writeStringUtf8(packet.node.snapshot.content + '\n')
	this.writeInstant(packet.node.snapshot.date)
	this.writeLong(packet.node.parentId?.value ?: error("tried to write root node"))
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
