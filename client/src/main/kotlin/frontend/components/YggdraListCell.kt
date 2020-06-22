package frontend.components

import javafx.scene.control.*
import tornadofx.*

class YggdraListCell : ListCell<NodeCached>() {
	override fun updateItem(item: NodeCached?, empty: Boolean) {
		super.updateItem(item, empty)

		contextMenu = null
		if (empty) {
			graphic = null
			return
		}
		item!!
		graphic = vbox {
			label(item.author)
			label(item.message)
		}
		contextMenu = contextmenu {
			item("edit") {
				action {
					println("trying to edit sth")
				}
			}
			item("view edit history") {
				action {
					println("edit history requested")
				}
			}
			item("invalidate") {
				action {
					println("trying to invalidate")
				}
			}
		}
	}
}