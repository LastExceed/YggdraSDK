import frontend.components.ObservableNode

object Tree {
	val root = ObservableNode(NodeId(0), "rootuser", "root of the tree", null)

	val allNodes = mutableMapOf(Pair(root.id, root))
}