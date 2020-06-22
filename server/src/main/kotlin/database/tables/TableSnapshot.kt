package database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

object TableSnapshot : Table() {
	val id = long("id").autoIncrement().uniqueIndex()
	val node = (long("node") references TableNode.id).nullable()
	val content = varchar("content", 2000) //hardcoded
	val timestamp = timestamp("timestamp")

	override val primaryKey = PrimaryKey(id)
}
