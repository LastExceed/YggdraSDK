import frontend.LoginDialog
import frontend.MainApp
import kotlinx.coroutines.*
import tornadofx.launch

suspend fun main() {
	val networker = Networker(Globals.serverAddress)
	do {
		val dialogResult = LoginDialog().showAndWait()
		val (email, password) = dialogResult.get()//TODO: handle login abort
		val authenticated = withContext(Dispatchers.IO) {
			networker.connect(email, password)
		}
	} while (!authenticated)

	launch<MainApp>()
}
