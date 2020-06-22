package frontend.components

import NodeId
import tornadofx.observableListOf

data class NodeCached(
	val id: NodeId,
	val author: String,
	val message: String,
	val parent: NodeCached?
) {
	val children = observableListOf<NodeCached>()
}
