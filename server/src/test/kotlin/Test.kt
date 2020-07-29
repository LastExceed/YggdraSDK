import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.*

class Calculator {
	fun add(a: Int, b: Int): Int {
		return a + b
	}

	fun div(a: Int, b: Int): Double {
		assert(b != 0) { "Division by Zero" }
		return a / b * 1.0
	}
}

class CalculatorTests {
	@Test
	fun `1 + 1 = 2`() {
		val calculator = Calculator()
		val computed = calculator.add(1, 1)
		assertEquals(2, computed)
	}

	@TestFactory
	fun selfSimilarity() = listOf(2, 3, 4, 5, 6).map {
		dynamicTest("self-similarity of $it") {
			assertEquals(it, it)
		}
	}

	@TestFactory
	fun addition(): List<DynamicTest> {
		data class MyClass(
			val a: Int,
			val b: Int,
			val expectedResult: Int
		)

		val elements = listOf(
			MyClass(1, 1, 2),
			MyClass(49, 51, 100),
			MyClass(0, 0, 0),
			MyClass(-1, -2, -3),
			MyClass(-1, 1, 0)
		)

		return elements.map {
			dynamicTest("${it.a} + ${it.b} = ${it.expectedResult}") {
				val calculator = Calculator()
				assertEquals(it.expectedResult, calculator.add(it.a, it.b))
			}
		}
	}
}
