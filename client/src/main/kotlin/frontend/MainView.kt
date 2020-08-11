package frontend

import frontend.components.NodeCached
import frontend.components.YggdraListCell
import javafx.beans.property.SimpleListProperty
import javafx.scene.input.KeyCode
import kotlinx.coroutines.*
import tornadofx.*

class MainView : View("YggdraChat"), CoroutineScope by MainScope() {
	private val chatPath = SimpleListProperty(observableListOf(NodeCache.root))
	private val comments = SimpleListProperty(observableListOf(NodeCache.root.children))

	init {
		var authenticated = false
		while (!authenticated) { val dialogResult = LoginDialog().showAndWait()
			val (email, password) = dialogResult.get()//TODO: handle login abort
			authenticated = runBlocking(Dispatchers.IO) {
				Networker.connect(email, password)
			}
		}
		launch(Dispatchers.IO) {
			Networker.handlePackets()
		}
	}

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

		launch(Dispatchers.IO) {
			Networker.goTo(target)
		}
	}
}
