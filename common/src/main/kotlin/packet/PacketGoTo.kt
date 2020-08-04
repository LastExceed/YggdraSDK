package packet

import NodeId
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import java.net.InetSocketAddress

data class PacketGoTo(
	val position: NodeId
) : Packet(PacketId.GOTO) {
	override suspend fun writePacketContent(writer: ByteWriteChannel) {
		writer.writeLong(position.value)
	}
}

suspend fun ByteReadChannel.readPacketGoTo(): PacketGoTo {
	return PacketGoTo(NodeId(this.readLong()))
}
