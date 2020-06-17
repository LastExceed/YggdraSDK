import frontend.components.ObservableNode

object Tree {
	val root = ObservableNode(IdNode(0), "rootuser", "root of the tree", null)

	val allNodes = mutableMapOf(Pair(root.id, root))
}
