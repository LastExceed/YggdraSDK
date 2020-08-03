package packet

import io.ktor.utils.io.*
import packet.Packet.Companion.readPacket

abstract class Packet(val id: PacketId) {
	protected abstract suspend fun writePacketContent(writer: ByteWriteChannel)

	companion object {
		suspend fun ByteWriteChannel.writePacket(packet: Packet) {
			this.writeByte(packet.id.value)
			packet.writePacketContent(this)
		}

		private val map = mapOf(
			PacketId.NODE_CREATE to ByteReadChannel::readPacketNodeCreate,
			PacketId.GOTO to ByteReadChannel::readPacketGoTo,
			PacketId.NAMECHANGE to ByteReadChannel::readPacketNameChange,
			PacketId.NODE_REVEAL to ByteReadChannel::readPacketNodeReveal
		)

		suspend fun ByteReadChannel.readPacket(): Packet {
			val packetId = PacketId(this.readByte())
			val packetHandler = map[packetId] ?: error("unknown packet ID: ${packetId.value}")
			return packetHandler(this)
		}
	}
}
