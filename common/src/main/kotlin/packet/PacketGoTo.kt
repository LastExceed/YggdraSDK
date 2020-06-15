package packet

import NodeId
import io.ktor.utils.io.*

data class PacketGoTo(
	val position: NodeId
) : Packet(PacketId.GOTO)

suspend fun ByteReadChannel.readPacketGoTo(): PacketGoTo {
	return PacketGoTo(NodeId(this.readLong()))
}

suspend fun ByteWriteChannel.writePacketGoTo(packet: PacketGoTo) {
	this.writeByte(packet.id.value)
	this.writeLong(packet.position.value)
}