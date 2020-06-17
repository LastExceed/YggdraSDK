package packet

import IdNode
import io.ktor.utils.io.*

data class PacketNodeCreate(
	val parentId: IdNode,
	val message: String
) : Packet(PacketId.NODE_CREATE)

suspend fun ByteReadChannel.readPacketNodeCreate(): PacketNodeCreate {
	return PacketNodeCreate(
		IdNode(this.readLong()),
		this.readUTF8Line(999)!!
	)
}

suspend fun ByteWriteChannel.writePacketNodeCreate(packet: PacketNodeCreate) {
	this.writeByte(packet.id.value)
	this.writeLong(packet.parentId.value)
	this.writeStringUtf8(packet.message + '\n')
}
