import kotlinx.coroutines.*

fun main() {
	val server = Server()
	runBlocking {
		server.start()
	}
	val userInput = run {
		val userInput = readLine()!!
		//ensure its numeric
		userInput.toInt()
	}
}