import java.time.Instant

data class Node(
	val id: IdNode,
	val author: IdUser,
	val snapshot: Snapshot,
	val parentId: IdNode?
) {
	val children = mutableListOf<Node>()
}

inline class IdNode(val value: Long)

inline class IdUser(val value: Long)

data class Snapshot(val content: String, val date: Instant)
