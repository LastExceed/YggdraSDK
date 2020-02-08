data class Node(
	val id: NodeId,
	val author: String,
	val message: String,
	val parentId: NodeId
) {
	val children = mutableListOf<Node>()
}

inline class NodeId(val value: Long)