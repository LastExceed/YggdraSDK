package server

import com.zaxxer.hikari.*
import database.Database
import org.junit.jupiter.api.DynamicTest
import kotlin.test.*
import org.junit.jupiter.api.*
import random.*
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


	@TestFactory
	fun createAndReadNode() = (1..20).toList().map {
		val authorID = Random.nextLong()
		val charPool = CharPool.ASCII.value + '\n'
		val content = charPool.random(Random.nextInt(1,2000))
		val parentID = null

		DynamicTest.dynamicTest("ID: $authorID content: $content") {
			val nodeID = database.createNode(authorID, content, parentID).id
			val node = database.getNode(nodeID)
			assertNotNull(node)
			assertEquals(node.author.value, authorID)
			assertEquals(node.latestSnapshot.content, content)
			println("end")
			println(content)
			println(node.latestSnapshot.content)
			assertEquals(node.parentId, parentID)
		}
	}
}
