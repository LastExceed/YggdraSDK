import frontend.components.ObservableNode

//TODO: request root from server
object Tree {
	val root = ObservableNode(NodeId(1), "rootuser", "root of all evil", null)

	val allNodes = mutableMapOf(Pair(root.id, root))
}
