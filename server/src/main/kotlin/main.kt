import database.Database
import org.jetbrains.exposed.sql.Database as ExposedDatabase

suspend fun main() {
	val database = Database(ExposedDatabase.connect("jdbc:sqlite:/data/database.db", "org.sqlite.JDBC"))
	val server = Server(database, Globals.serverAddress)

	server.start()
}
