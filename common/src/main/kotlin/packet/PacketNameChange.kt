package packet

import kotlinx.coroutines.io.*

data class PacketNameChange(
	val name: String
) : Packet(PacketId.NAMECHANGE)

suspend fun ByteReadChannel.readPacketNameChange(): PacketNameChange {
	return PacketNameChange(this.readUTF8Line(32)!!)
}

suspend fun ByteWriteChannel.writePacketNameChange(packet: PacketNameChange) {
	this.writeByte(packet.id.value)
	this.writeStringUtf8(packet.name + '\n')
}