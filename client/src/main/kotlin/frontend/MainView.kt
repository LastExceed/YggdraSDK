package frontend

import frontend.components.NodeCached
import frontend.components.YggdraListCell
import javafx.event.EventHandler
import javafx.scene.control.ListView
import javafx.scene.control.TextInputDialog
import javafx.scene.input.KeyCode
import kotlinx.coroutines.runBlocking
import tornadofx.*

class MainView : View("YggdraChat") {
	private val username: String
	private val chatPath = observableListOf(Tree.root)
	private lateinit var commentsListView: ListView<ObservableNode>

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
			commentsListView = listview(chatPath.last().children) {
				setCellFactory {
					YggdraListCell()
				}
				onUserSelect {
					goTo(it)
				}
			}
		}
		textarea {
			onKeyPressed = EventHandler {
				if (it.code != KeyCode.ENTER || !it.isControlDown) {
					return@EventHandler
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

		commentsListView.items = target.children

		runAsync {
			runBlocking {
				Networker.goTo(target)
			}
		}
	}
}