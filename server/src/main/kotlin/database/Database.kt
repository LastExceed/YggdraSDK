package database

import NodeId
import database.tables.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database as ExposedDatabase
import org.jetbrains.exposed.sql.transactions.*
import java.sql.Connection

object Database {
	private val db: ExposedDatabase = ExposedDatabase.connect("jdbc:sqlite:/data/database.db", "org.sqlite.JDBC")

	init {
		db.useNestedTransactions = true
		TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED
		transaction {
			addLogger(StdOutSqlLogger)
			SchemaUtils.create(TableSnapshot, TableNode, TableUser)
		}
	}

	fun createNode(authorName: String, authorID: Long, content: String, parent: NodeId?) {
		transaction {
			if (parent != null && !TableNode.exists { TableNode.id eq parent.value })
				throw Exception("Parent does not exist")

			createUser(authorName, authorID)
			val snapshotID = createSnapshot(content, null)
			val nodeID = TableNode.insertIgnore {
				it[this.author] = authorID
				it[this.parent] = parent?.value
				it[this.lastMessage] = snapshotID
			} get TableNode.id

			TableSnapshot.update(where = { TableSnapshot.id eq snapshotID }) {
				it[this.node] = nodeID
			}
		}
	}

	fun createUser(userName: String, userID: Long) {
		transaction {
			TableUser.insertIgnore {
				it[id] = userID
				it[name] = userName
			}
		}
	}

	fun addSnapshot(content: String, parent: Long) = createSnapshot(content, parent)

	private fun createSnapshot(content: String, parent: Long?): Long {
		return transaction {
			return@transaction TableSnapshot.insert {
				it[this.content] = content
				it[this.node] = parent
			} get TableSnapshot.id
		}
	}

	private fun FieldSet.exists(where: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
		return this.select { where() }.count() > 0
	}
}
