package frontend.components

import IdNode
import tornadofx.observableListOf

data class ObservableNode(
	val id: IdNode,
	val author: String,
	val message: String,
	val parent: ObservableNode?
) {
	val children = observableListOf<ObservableNode>()
}
