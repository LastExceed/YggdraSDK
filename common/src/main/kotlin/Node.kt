import java.time.Instant

data class Node(
	val id: NodeId,
	val author: UserId,
	val latestSnapshot: Snapshot,
	val parentId: NodeId?,
	val children: List<NodeId>
)

inline class NodeId(val value: Long)

inline class UserId(val value: Long)

data class Snapshot(val content: String, val date: Instant)
