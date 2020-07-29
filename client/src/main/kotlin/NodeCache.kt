import frontend.components.NodeCached

//TODO: request root from server
object NodeCache {
	val root = NodeCached(NodeId(1), false, "root of all evil", null)

	val allNodes = mutableMapOf(root.id to root)
}
