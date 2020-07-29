package database

import Node
import NodeId
import Snapshot
import UserId
import database.tables.TableNode
import database.tables.TableSnapshot
import database.tables.TableUser
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception
import java.sql.Connection
import java.sql.SQLException
import java.time.Instant
import org.jetbrains.exposed.sql.Database as ExposedDatabase


//TODO: add function to explicitly initialize this object
class Database(private val db: ExposedDatabase) {
	//private val db: ExposedDatabase = ExposedDatabase.connect("jdbc:sqlite:/data/database.db", "org.sqlite.JDBC") //TODO: change to relative path

	init {
		db.useNestedTransactions = true
		TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED
		transaction(db) {
			addLogger(StdOutSqlLogger)
			SchemaUtils.create(TableSnapshot, TableNode, TableUser)
			if (!TableNode.exists { TableNode.id eq 1L }) {//TODO: dont hardcode root id
				val rootUserId = getOrCreateUser("rootuser")
				createNode(rootUserId, "root of all evil", null)
			}

		}
	}

	fun createNode(authorId: Long, content: String, parent: NodeId?): Node {
		return transaction(db) {
			if (parent != null && !TableNode.exists { TableNode.id eq parent.value })
				error("Parent does not exist")

			val snapshotPair = createSnapshot(content, parent = null)
			val nodeId = TableNode.insert {
				it[this.author] = authorId
				it[this.parent] = parent?.value
				it[this.lastSnapshot] = snapshotPair.second
			} get TableNode.id
			TableSnapshot.update(where = { TableSnapshot.id eq snapshotPair.second }) {
				it[this.node] = nodeId
			}
			Node(
				NodeId(nodeId),
				UserId(authorId),
				snapshotPair.first,
				parent
			)
		}
	}

	fun getOrCreateUser(userName: String): Long {
		return transaction(db) {
			val query = TableUser.select { TableUser.name eq userName }.firstOrNull()
				?: return@transaction TableUser.insert {
					it[name] = userName
				} get TableUser.id
			query[TableUser.id]
		}
	}

	fun getUserID(name: String): Long {
		return transaction(db) {
			val query = TableUser.select { TableUser.name eq name }
			query.firstOrNull()?.get(TableUser.id) ?: error("User with name: $name does not exist")
		}
	}

	fun addSnapshot(content: String, parent: Long) = createSnapshot(content, parent)

	private fun createSnapshot(content: String, parent: Long?): Pair<Snapshot, Long> {
		val instant = Instant.now()
		val snapshotId = transaction(db) {
			TableSnapshot.insert {
				it[this.content] = content
				it[this.node] = parent
				it[this.timestamp] = instant
			} get TableSnapshot.id
		}
		return Snapshot(content, instant) to snapshotId
	}

	private fun FieldSet.exists(where: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
		return select { where() }.count() > 0
	}

	fun getNode(position: NodeId): Node? {
		return transaction(db) {
			val query = TableNode.select { TableNode.id eq position.value }.firstOrNull()
				?: return@transaction null
			val node = Node(
				NodeId(position.value),
				UserId(query[TableNode.author]),
				getSnapshot(query[TableNode.lastSnapshot])!!,
				query[TableNode.parent].let {
					if(it != null) NodeId(it) else null
				} //TODO fix this.rumhampelei
			)

			val query2 = TableNode.select { TableNode.parent eq node.id.value }//TODO: only query IDs instead of whole nodes
			val children = query2.map { NodeId(it[TableNode.id]) }
			node.children.addAll(children)
			node
		}
	}

	fun deleteNode(position: NodeId) {
		transaction(db) {
			try {
				TableNode.deleteWhere { TableNode.id eq position.value }
			} catch (e: SQLException) {
				throw SQLException("Error while deleting Node with ID ${position.value} error message: ${e.message}" )
			}
		}
	}

	private fun getSnapshot(messageId: Long): Snapshot? {
		return transaction(db) {
			val query = TableSnapshot.select { TableSnapshot.id eq messageId }.firstOrNull()
				?: return@transaction null
			Snapshot(
				query[TableSnapshot.content],
				query[TableSnapshot.timestamp]
			)
		}
	}
}
