package packet

import io.ktor.utils.io.*
import readString
import writeString

data class PacketLogin(
	val email: String,
	val password: String
) : Packet(PacketId.LOGIN) {
	override suspend fun writePacketContent(writer: ByteWriteChannel) {
		writer.writeString(email)
		writer.writeString(password)
	}
}

suspend fun ByteReadChannel.readPacketLogin(): PacketLogin {
	return PacketLogin(this.readString(), this.readString())
}
