package frontend

import javafx.beans.property.*
import javafx.beans.value.*
import javafx.scene.control.*
import javafx.scene.control.ButtonBar.*
import javafx.scene.layout.*
import javafx.util.*
import tornadofx.*
import kotlin.Pair


class LoginDialog : Dialog<Pair<String, String>>() {
	val email = SimpleStringProperty()
	val password = SimpleStringProperty()

	init {
		title = "Login Dialog"

		val loginButtonType = ButtonType("Login", ButtonData.OK_DONE)
		dialogPane.buttonTypes.addAll(loginButtonType, ButtonType.CANCEL)

		val loginButton = dialogPane.lookupButton(loginButtonType)
		loginButton.isDisable = true

		dialogPane.content = VBox().apply {
			hbox {
				label("Email")
				spacer()
				textfield {
					promptText = "Email"
					email.bind(textProperty())
				}
			}
			hbox {
				label("Password")
				spacer()
				passwordfield {
					promptText = "Password"
					password.bind(textProperty())
				}
			}
		}

		val inputChangeListener =
			{ observable: ObservableValue<out String>, oldValue: String, newValue: String ->
				loginButton.isDisable = email.value.trim().isEmpty() || password.value.isEmpty()
			}
		email.addListener(inputChangeListener)
		password.addListener(inputChangeListener)

		resultConverter = Callback { dialogButton: ButtonType ->
			if (dialogButton === loginButtonType) {
				Pair(email.value, password.value)
			} else null
		}
	}
}

fun thing() {
		LoginDialog().showAndWait().ifPresent { (first, second) ->
		println("Username=$first, Password=$second")
	}
}