package packet

import NodeId
import io.ktor.utils.io.*

data class PacketGoTo(
	val position: NodeId
) : Packet(PacketId.GOTO) {
	override suspend fun writePacketContent(writer: ByteWriteChannel) {
		writer.writeLong(position.value)
	}

	override suspend fun readPacketContent(reader: ByteReadChannel) {
		TODO("Not yet implemented")
	}

}

suspend fun ByteReadChannel.readPacketGoTo(): PacketGoTo {
	return PacketGoTo(NodeId(this.readLong()))
}

//suspend fun ByteWriteChannel.writePacketGoTo(packet: PacketGoTo) {
//	this.writeByte(packet.id.value)
//	this.writeLong(packet.position.value)
//}
