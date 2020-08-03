package packet

import io.ktor.utils.io.*

abstract class Packet(val id: PacketId) {
	protected abstract suspend fun writePacketContent(writer: ByteWriteChannel)

	companion object {
		suspend fun ByteWriteChannel.writePacket(packet: Packet) {
			this.writeByte(packet.id.value)
			packet.writePacketContent(this)
		}
	}
}
