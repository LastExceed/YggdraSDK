package random


fun List<Char>.random(length: Int): String {
	return (1..length).map { this[kotlin.random.Random.nextInt(0, this.size)] }
		.joinToString("")
}
