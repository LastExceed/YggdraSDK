package packet

import NodeId
import io.ktor.utils.io.*
import readString
import writeString

data class PacketNodeCreate(
	val parentId: NodeId,
	val message: String
) : Packet(PacketId.NODE_CREATE) {
	override suspend fun writePacketContent(writer: ByteWriteChannel) {
		writer.writeByte(id.value)
		writer.writeLong(parentId.value)
		writer.writeString(message)
	}

	override suspend fun readPacketContent(reader: ByteReadChannel) {
		TODO("Not yet implemented")
	}
}

suspend fun ByteReadChannel.readPacketNodeCreate(): PacketNodeCreate {
	return PacketNodeCreate(
		NodeId(this.readLong()),
		this.readString()
	)
}

//suspend fun ByteWriteChannel.writePacketNodeCreate(packet: PacketNodeCreate) {
//	this.writeByte(packet.id.value)
//	this.writeLong(packet.parentId.value)
//	this.writeString(packet.message)
//}
