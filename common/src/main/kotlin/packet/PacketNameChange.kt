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
}

suspend fun ByteReadChannel.readPacketNameChange(): PacketNameChange {
	return PacketNameChange(this.readString())
}
