import kotlinx.coroutines.*

suspend fun main() {
	val server = Server()
	server.start()
}