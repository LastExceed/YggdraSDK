package packet

inline class PacketId(val value: Byte) {
	//hack until we get enum inline classes
	companion object {
		val NODE_CREATE = PacketId(0)
		val NODE_REVEAL = PacketId(1)
		val GOTO = PacketId(2)
		val NAMECHANGE = PacketId(3)
	}
}