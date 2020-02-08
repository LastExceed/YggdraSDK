package frontend

import javafx.scene.paint.*
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
	companion object {
		val heading by cssclass()
	}

	init {
		label and heading {
		}

		listView {
		}
	}
}