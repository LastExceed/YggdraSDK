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
}

suspend fun ByteReadChannel.readPacketNodeCreate(): PacketNodeCreate {
	return PacketNodeCreate(
		NodeId(this.readLong()),
		this.readString()
	)
}
