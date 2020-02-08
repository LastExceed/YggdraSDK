package frontend.components

import NodeId
import tornadofx.observableListOf

data class ObservableNode(
	val id: NodeId,
	val author: String,
	val message: String,
	val parent: ObservableNode?
) {
	val children = observableListOf<ObservableNode>()
}