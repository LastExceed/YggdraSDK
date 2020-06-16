package database.tables

import org.jetbrains.exposed.sql.Table

object TableSnapshot : Table() {
	val id = long("id").autoIncrement().uniqueIndex()
	val node = (long("node") references TableNode.id).nullable()
	val content = varchar("content", 2000) //hardcoded

	override val primaryKey = PrimaryKey(id)
}
