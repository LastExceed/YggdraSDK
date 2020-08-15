package database.tables

import org.jetbrains.exposed.sql.Table

object TableUser : Table() {
	val id = long("id").autoIncrement().uniqueIndex()
	val email = varchar("email", Globals.emailSizeLimit).uniqueIndex()
	val password = varchar("password", Globals.passwordSizeLimit)

	override val primaryKey = PrimaryKey(id)
}
