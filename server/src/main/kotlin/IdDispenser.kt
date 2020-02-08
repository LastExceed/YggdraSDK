object IdDispenser {
	private var count = 0L
	fun next(): NodeId = NodeId(count++)
}