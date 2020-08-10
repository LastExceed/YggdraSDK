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
	//jdbc:sqlite:/data/database.db TODO: change to relative path

	init {
		db.useNestedTransactions = true
		TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED
		transaction(db) {
			addLogger(StdOutSqlLogger)
			SchemaUtils.create(TableSnapshot, TableNode, TableUser)
			if (!TableNode.exists { TableNode.id eq 1L }) {//TODO: dont hardcode root id
				val rootUserId = createUser("rootuser", "1234")
				createNode(getUser("rootuser","1234"), "root of all evil", null)
			}

		}
	}

	fun createNode(authorId: Long, content: String, parent: NodeId?): Node {
		return transaction(db) {
			if (parent != null && !TableNode.exists { TableNode.id eq parent.value })
				error("Parent does not exist")

			val snapshotPair = createAndReturnSnapshot(content, parent = null)
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

	fun getUser(email: String, password: String): UserId? {
		val userId = transaction(db) {
			val row = TableUser.select { (TableUser.email eq email) and (TableUser.password eq password) }.firstOrNull()
				?: return@transaction null
			row[TableUser.id]
		}
		return if(userId == null) null else UserId(userId)
	}

	fun createUser(email: String, password: String) {
		return transaction(db) {
			TableUser.insert {
				it[this.email] = email
				it[this.password] = password
			}
		}
	}

	fun addSnapshot(content: String, parent: Long) = createAndReturnSnapshot(content, parent)

	private fun createAndReturnSnapshot(content: String, parent: Long?): Pair<Snapshot, Long> {
		val instant = Instant.now()
		return Snapshot(content, instant) to createSnapshot(content, parent, instant)
	}

	private fun createSnapshot(content: String, parent: Long?, instant: Instant = Instant.now()): Long {
		return transaction(db) {
			TableSnapshot.insert {
				it[this.content] = content
				it[this.node] = parent
				it[this.timestamp] = instant
			} get TableSnapshot.id
		}
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

	fun updateNode(newContent: String, position: NodeId) {
		transaction(db) {
			if(!TableNode.exists{ TableNode.id eq position.value } ) {
				throw Exception("Node with ID ${position.value} does not exist")
			}
			val snapshotID = createSnapshot(newContent, position.value)
			TableNode.update ({ TableNode.id eq position.value }) {
				it[TableNode.lastSnapshot] = snapshotID
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
