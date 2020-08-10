package packet

import io.ktor.utils.io.*

data class PacketLoginAcknowlegdement(
	val authenticated: Boolean
) : Packet(PacketId.LOGIN_ACKNOWLEDGEMENT) {
	override suspend fun writePacketContent(writer: ByteWriteChannel) {
		writer.writeBoolean(authenticated)
	}
}

suspend fun ByteReadChannel.readPacketLoginAcknowledgement(): PacketLoginAcknowlegdement {
	return PacketLoginAcknowlegdement(this.readBoolean())
}
