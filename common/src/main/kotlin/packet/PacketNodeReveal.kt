package packet

import Node
import NodeId
import io.ktor.utils.io.*

data class PacketNodeReveal(
	val node: Node
) : Packet(PacketId.NODE_REVEAL)

suspend fun ByteReadChannel.readPacketNodeReveal(): PacketNodeReveal {
	return PacketNodeReveal(
		Node(
			id = NodeId(this.readLong()),
			author = this.readUTF8Line(32)!!,
			message = this.readUTF8Line(999)!!,
			parentId = NodeId(this.readLong())
		)
	)
}

suspend fun ByteWriteChannel.writePacketNodeReveal(packet: PacketNodeReveal) {
	this.writeByte(packet.id.value)

	this.writeLong(packet.node.id.value)
	this.writeStringUtf8(packet.node.author + '\n')
	this.writeStringUtf8(packet.node.message + '\n')
	this.writeLong(packet.node.parentId.value)
}