package packet

import IdNode
import io.ktor.utils.io.*

data class PacketGoTo(
	val position: IdNode
) : Packet(PacketId.GOTO)

suspend fun ByteReadChannel.readPacketGoTo(): PacketGoTo {
	return PacketGoTo(IdNode(this.readLong()))
}

suspend fun ByteWriteChannel.writePacketGoTo(packet: PacketGoTo) {
	this.writeByte(packet.id.value)
	this.writeLong(packet.position.value)
}
