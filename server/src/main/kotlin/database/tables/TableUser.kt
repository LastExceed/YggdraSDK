package database.tables

import org.jetbrains.exposed.sql.Table

object TableUser : Table() {
	val id = long("id").autoIncrement().uniqueIndex()
	val name = varchar("name", 24).uniqueIndex() //hardcoded

	override val primaryKey = PrimaryKey(id)
}
