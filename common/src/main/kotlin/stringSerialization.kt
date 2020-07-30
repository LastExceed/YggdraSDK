import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlin.text.toByteArray

suspend fun ByteReadChannel.readString(): String {
	ByteArray(readInt()).let {
		readFully(it)
		return it.toString(Charset.forName("UTF-8"))
	}
}

suspend fun ByteWriteChannel.writeString(string: String) {
	string.toByteArray().let {
		writeInt(it.size)
		writeFully(it)
	}
}