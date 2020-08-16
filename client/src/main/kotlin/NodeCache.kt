import frontend.components.NodeCached

//TODO: request root from server
//TODO: make instantiable for testing
object NodeCache {
	val root = NodeCached(Globals.rootId, false, "root of all evil", null)

	val allNodes = mutableMapOf(root.id to root)
}
