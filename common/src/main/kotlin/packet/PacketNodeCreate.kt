package packet

import NodeId
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.coroutines.io.writeStringUtf8

data class PacketNodeCreate(
	val parentId: NodeId,
	val message: String
) : Packet(PacketId.NODE_CREATE)

suspend fun ByteReadChannel.readPacketNodeCreate(): PacketNodeCreate {
	return PacketNodeCreate(
		NodeId(this.readLong()),
		this.readUTF8Line(999)!!
	)
}

suspend fun ByteWriteChannel.writePacketNodeCreate(packet: PacketNodeCreate) {
	this.writeByte(packet.id.value)
	this.writeLong(packet.parentId.value)
	this.writeStringUtf8(packet.message + '\n')
}