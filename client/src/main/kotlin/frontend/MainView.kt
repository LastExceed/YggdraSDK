package frontend

import Networker
import frontend.components.NodeCached
import frontend.components.YggdraListCell
import javafx.beans.property.SimpleListProperty
import javafx.scene.input.KeyCode
import kotlinx.coroutines.*
import tornadofx.*

class MainView : View("YggdraChat"), CoroutineScope by MainScope() {
	private val chatPath = SimpleListProperty(observableListOf(NodeCache.root))
	private val comments = SimpleListProperty(observableListOf(NodeCache.root.children))
	private val networker: Networker = Networker(Globals.serverAddress)

	override val root = form {
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
				text = text.trim()
				if (text.isEmpty()) {
					return@setOnKeyPressed
				}

				launch {
					withContext(Dispatchers.IO) {
						networker.createNode(chatPath.last(), text)
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

		launch(Dispatchers.IO) {
			networker.goTo(target)
		}
	}
}
