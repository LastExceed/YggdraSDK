package random

import kotlin.random.Random


fun List<Char>.random(length: Int): String {
	return (1..length).map { this[kotlin.random.Random.nextInt(0, this.size)] }
		.joinToString("")
}

fun List<Char>.randomLength(length: Int): String {
	return this.random(Random.nextInt(1, length))
}
