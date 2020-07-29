package frontend.components

import javafx.scene.control.*
import tornadofx.*

class YggdraListCell : ListCell<NodeCached>() {
	override fun updateItem(item: NodeCached?, empty: Boolean) {
		super.updateItem(item, empty)

		if (item == null != empty) error("api error")
		
		contextMenu = null
		if (item == null) {
			graphic = null
			return
		}
		graphic = label(item.message) {
			if (item.own) {
				style(append = true) { backgroundColor += c("#00FF00", .5) }
			}
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