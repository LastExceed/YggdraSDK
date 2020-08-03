package packet

abstract class Packet(val id: PacketId) {
//	abstract suspend fun deserialize(readChannel: ByteReadChannel) TODO resolve this
//	suspend fun serialize(writeChannel: ByteWriteChannel) {
//		writeChannel.writeByte(id.value)
//		writePacketContent(writeChannel)
//	}
//
//	protected abstract suspend fun writePacketContent(writeChannel: ByteWriteChannel)
}
