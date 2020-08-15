package server

//import Networker
import Networker
import Server
import com.zaxxer.hikari.*
import database.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import packet.PacketLogin
import random.*
import java.lang.Integer.*
import java.net.InetSocketAddress
import kotlin.random.Random
import kotlin.test.assertEquals
import org.jetbrains.exposed.sql.Database as ExposedDatabase

class ServerTests {
	private val testRepeats: Int = 20
	private val scope = CoroutineScope(Dispatchers.IO)

	data class TestAccounts(
		val packet: PacketLogin = PacketLoginData().value,
		val registerUser: Boolean = Random.nextBoolean()
	)

	private val loginAmount: Int = 1000
	private val networkerAmount: Int = max(loginAmount / 100, 10)
	private val loginRandomUsersTestPort: Int = 12310
	@TestFactory
	fun loginRandomUsers () = runBlocking {
			val address = InetSocketAddress("127.0.0.1", loginRandomUsersTestPort)
			val server = createServerTest(address)
			launch {
				server.start()
			}

			val emails: Set<String> = (1..loginAmount)
				.map { CharPool.ASCII.value.random(Random.nextInt(1, Globals.emailSizeLimit)) }.toSet()

			val accounts = emails.map { email ->
				TestAccounts(PacketLogin(
					email,
					CharPool.ASCII.value.random(Random.nextInt(1, Globals.passwordSizeLimit)))
				)
			}
		
			val tests = accounts.map { account ->
				if(account.registerUser)
					server.registerUser(account.packet.email, account.packet.password)
				DynamicTest.dynamicTest("email: ${account.packet.email} " +
					"password: ${account.packet.password}") {
					scope.launch {
						val networker = Networker(address)
						val authenticated = networker.connect(account.packet.email, account.packet.password)
						assertEquals(authenticated, account.registerUser)
					}
				}
			}
			server.stop()
			return@runBlocking tests
	}
}

fun createServerTest(address: InetSocketAddress): Server {
	val cfg: HikariConfig = HikariConfig().apply {
		jdbcUrl = "jdbc:sqlite::memory:"
		maximumPoolSize = 6
	}
	val dataSource = HikariDataSource(cfg)
	val database = Database(ExposedDatabase.connect(dataSource))
	return Server(database, address)
}

inline class PacketLoginData(
	val value: PacketLogin = PacketLogin(
		CharPool.ASCII.value.random(Random.nextInt(1, Globals.emailSizeLimit)),
		CharPool.ASCII.value.random(Random.nextInt(1, Globals.passwordSizeLimit))
	)
)
