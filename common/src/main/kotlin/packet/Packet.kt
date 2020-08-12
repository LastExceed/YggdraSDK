package packet

import io.ktor.utils.io.*

abstract class Packet(val id: PacketId) {
	protected abstract suspend fun writePacketContent(writer: ByteWriteChannel)

	companion object {
		private val packetReaders = mapOf(
			PacketId.NODE_CREATE to ByteReadChannel::readPacketNodeCreate,
			PacketId.GOTO to ByteReadChannel::readPacketGoTo,
			PacketId.LOGIN to ByteReadChannel::readPacketLogin,
			PacketId.NODE_REVEAL to ByteReadChannel::readPacketNodeReveal,
			PacketId.LOGIN_ACKNOWLEDGEMENT to ByteReadChannel::readPacketLoginAcknowledgement
		)

		suspend fun ByteWriteChannel.writePacket(packet: Packet) {
			this.writeByte(packet.id.value)
			packet.writePacketContent(this)
		}

		suspend fun ByteReadChannel.readPacket(): Packet {
			val packetId = PacketId(this.readByte())
			val packetReader = packetReaders[packetId] ?: error("unknown packet ID: ${packetId.value}")
			return packetReader(this)
		}
	}
}
