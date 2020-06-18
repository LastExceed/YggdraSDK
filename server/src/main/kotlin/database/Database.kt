package database

import Node
import IdNode
import Snapshot
import IdUser
import database.tables.TableNode
import database.tables.TableSnapshot
import database.tables.TableUser
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.time.Instant
import org.jetbrains.exposed.sql.Database as ExposedDatabase

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

	fun createNode(authorID: Long, content: String, parent: IdNode?): Node {
		return transaction {
			if (parent != null && !TableNode.exists { TableNode.id eq parent.value })
				throw Exception("Parent does not exist")

			val snapshotPair = createSnapshot(content, parent = null)
			val nodeID = TableNode.insertIgnore {
				it[this.author] = authorID
				it[this.parent] = parent?.value
				it[this.lastSnapshot] = snapshotPair.second
			} get TableNode.id

			TableSnapshot.update(where = { TableSnapshot.id eq snapshotPair.second }) {
				it[this.node] = nodeID
			}
			return@transaction Node(
				IdNode(nodeID),
				IdUser(authorID),
				snapshotPair.first,
				IdNode(nodeID)
			)
		}
	}

	fun tryCreateUser(userName: String): Long {
		return transaction {
			val query = TableUser.select { TableUser.name eq userName }.firstOrNull()
				?: return@transaction TableUser.insert {
					it[name] = userName
				} get TableUser.id
			return@transaction query[TableUser.id]
		}
	}

	fun getUserID(name: String): Long {
		return transaction {
			val query = TableUser.select { TableUser.name eq name }
			return@transaction query.firstOrNull()?.get(TableUser.id) ?: throw Exception("User with name: $name does not exist")
		}
	}

	fun addSnapshot(content: String, parent: Long) = createSnapshot(content, parent)

	private fun createSnapshot(content: String, parent: Long?): Pair<Snapshot, Long> {
		val instant = Instant.now()
		val snapshotId = transaction {
			return@transaction TableSnapshot.insert {
				it[this.content] = content
				it[this.node] = parent
				it[this.timestamp] = instant
			} get TableSnapshot.id
		}
		return Snapshot(content, instant) to snapshotId
	}

	private fun FieldSet.exists(where: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
		return this.select { where() }.count() > 0
	}

	fun getNode(position: IdNode): Node? {
		return transaction {
			val query = TableNode.select { TableNode.id eq position.value }.firstOrNull()
				?: return@transaction null
			return@transaction Node(
				IdNode(position.value),
				IdUser(query[TableNode.author]),
				getSnapshot(query[TableNode.lastSnapshot])!!,
				query[TableNode.parent].let {
					if(it != null) IdNode(it) else null
				} //TODO fix this.rumhampelei
			)
		}
	}

	private fun getSnapshot(messageId: Long): Snapshot? {
		return transaction {
			val query = TableSnapshot.select { TableSnapshot.id eq messageId }.firstOrNull()
				?: return@transaction null
			return@transaction Snapshot(
				query[TableSnapshot.content],
				query[TableSnapshot.timestamp]
			)
		}
	}
}
