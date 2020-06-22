package database.tables

import org.jetbrains.exposed.sql.Table

object TableNode : Table() {
	val id = long("id").autoIncrement().uniqueIndex()
	val author = long("author") references TableUser.id
	val parent = (long("parent") references id).nullable() //root has no parent
	val lastSnapshot = long("lastMessage") references TableSnapshot.id

	override val primaryKey = PrimaryKey(id)
}
