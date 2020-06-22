package frontend

import frontend.components.NodeCached
import frontend.components.YggdraListCell
import javafx.beans.property.SimpleListProperty
import javafx.scene.control.TextInputDialog
import javafx.scene.input.KeyCode
import kotlinx.coroutines.runBlocking
import tornadofx.*

class MainView : View("YggdraChat") {
	private val username: String
	private val chatPath = SimpleListProperty(observableListOf(NodeCache.root))
	private val comments = SimpleListProperty(observableListOf(NodeCache.root.children))

	init {
		val dialog = TextInputDialog().apply {
			headerText = "please choose a username"
			graphic = null
		}
		val dialogResult = dialog.showAndWait()
		username = dialogResult.get()

		runAsync {
			runBlocking {
				Networker.connect(username)
			}
		}
	}

	override val root = form {
//		menubar {
//			menu("options") {
//				checkmenuitem("dark mode")
//			}
//		}
		hbox {
			listview(chatPath) {
				setCellFactory {
					YggdraListCell()
				}
				onUserSelect {
					goTo(it)
				}
			}
			listview(comments) {
				setCellFactory {
					YggdraListCell()
				}
				onUserSelect {
					goTo(it)
				}
			}
		}
		textarea {
			setOnKeyPressed {
				if (it.code != KeyCode.ENTER || !it.isControlDown) {
					return@setOnKeyPressed
				}
				text = text.removeSuffix("\n")
				if (text == "") {
					return@EventHandler
				}
				runAsync {
					runBlocking {
						Networker.createNode(chatPath.last(), text)
					}
					text = ""
				}
			}
		}
	}

	private fun goTo(target: NodeCached) {
		chatPath.clear()
		target.children.clear()//these are outdated, server will provide current
		//todo: use timestamp for differential update

		fun addAfterParent(node: NodeCached) {
			if (node.parent != null) addAfterParent(node.parent)
			chatPath.add(node)
		}
		addAfterParent(target)

		comments.set(target.children)

		runAsync {
			runBlocking {
				Networker.goTo(target)
			}
		}
	}
}