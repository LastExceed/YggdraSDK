data class User(
	val name: String,
	val readMessages: MutableList<IdNode> = mutableListOf()
)
