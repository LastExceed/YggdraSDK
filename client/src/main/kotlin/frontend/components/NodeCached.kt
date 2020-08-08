package frontend.components

import NodeId
import tornadofx.observableListOf

data class NodeCached(
	val id: NodeId,
	val own: Boolean,
	val message: String,
	val parent: NodeCached?
) {
	val children = observableListOf<NodeCached>()
}
