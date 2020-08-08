package server

import NodeId
import com.zaxxer.hikari.*
import database.Database
import org.junit.jupiter.api.DynamicTest
import kotlin.test.*
import org.junit.jupiter.api.*
import random.CharPool
import random.random
import kotlin.random.Random
import org.jetbrains.exposed.sql.Database as ExposedDatabase

class DatabaseTests {
	private val database: Database

	init {
		val cfg: HikariConfig = HikariConfig().apply {
			jdbcUrl = "jdbc:sqlite::memory:"
			maximumPoolSize = 6
		}
		val dataSource = HikariDataSource(cfg)
		database = Database(ExposedDatabase.connect(dataSource))
	}

	data class NodeData(
		val authorID: Long =  Random.nextLong(),
		val content: String = (CharPool.ASCII.value + '\n').random(Random.nextInt(1,2000)),
		val parentID: NodeId? = null
	)

	@TestFactory
	fun createAndReadNode() = (1..20).toList().map {
		val nodeData = NodeData()

		DynamicTest.dynamicTest("ID: ${nodeData.authorID} content: ${nodeData.content}") {
			val nodeID = database.createNode(nodeData.authorID, nodeData.content, nodeData.parentID).id
			val node = database.getNode(nodeID)
			assertNotNull(node)
			assertEquals(node.author.value, nodeData.authorID)
			assertEquals(node.latestSnapshot.content, nodeData.content)
			assertEquals(node.parentId, nodeData.parentID)
		}
	}

	@TestFactory
	fun createAndDeleteNode() = (1..20).toList().map {
		val nodeData = NodeData()

		DynamicTest.dynamicTest("ID: ${nodeData.authorID} content: ${nodeData.content}") {
			val nodeID = database.createNode(nodeData.authorID, nodeData.content, nodeData.parentID).id
			assertNotNull(database.getNode(nodeID))
			database.deleteNode(nodeID)
			assertNull(database.getNode(nodeID))
		}
	}

	@TestFactory
	fun createAndUpdateNode() = (1..20).toList().map {
		val nodeData = NodeData()
		val updateData = (CharPool.ASCII.value + '\n').random(Random.nextInt(1,2000))

		DynamicTest.dynamicTest("ID: ${nodeData.authorID} content: ${nodeData.content}") {
			val nodeID = database.createNode(nodeData.authorID, nodeData.content, nodeData.parentID).id
			database.updateNode(updateData, nodeID)
			val node = database.getNode(nodeID)
			assertNotNull(node)
			assertEquals(node.author.value, nodeData.authorID)
			assertEquals(node.latestSnapshot.content, updateData)
			assertEquals(node.parentId, nodeData.parentID)
		}
	}
}
