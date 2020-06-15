data class User(
	val name: String,
	val readMessages: MutableList<NodeId> = mutableListOf()
)