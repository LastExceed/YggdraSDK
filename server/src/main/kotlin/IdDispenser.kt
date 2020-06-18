object IdDispenser {
	private var count = 0L
	fun next(): IdNode = IdNode(count++)
}
