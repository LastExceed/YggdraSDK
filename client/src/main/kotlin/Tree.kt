import frontend.components.ObservableNode

//TODO: request root from server
object Tree {
	val root = ObservableNode(IdNode(1), "rootuser", "root of all evil", null)

	val allNodes = mutableMapOf(Pair(root.id, root))
}
