package random

enum class CharPool(val value: List<Char>) {
	Numbers(('0'..'9').toList()),
	Alphabet((('a'..'z') + ('A'..'Z')).toList()),
	AlphaNumeric(Numbers.value + Alphabet.value),
	Symbol(listOf('!', '"', '#', '$', '%', '&', '\'', '(', ')', '*'	, '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~')),
	ASCII(AlphaNumeric.value + Symbol.value)
}
