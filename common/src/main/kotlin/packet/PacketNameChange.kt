package packet

import io.ktor.utils.io.*
import readString
import writeString

data class PacketNameChange(
	val name: String
) : Packet(PacketId.NAMECHANGE) {
	override suspend fun writePacketContent(writer: ByteWriteChannel) {
		writer.writeString(name)
	}

	override suspend fun readPacketContent(reader: ByteReadChannel) {
		TODO("Not yet implemented")
	}
}

suspend fun ByteReadChannel.readPacketNameChange(): PacketNameChange {
	return PacketNameChange(this.readString())
}

//suspend fun ByteWriteChannel.writePacketNameChange(packet: PacketNameChange) {
//	this.writeByte(packet.id.value)
//	this.writeString(packet.name)
//}
