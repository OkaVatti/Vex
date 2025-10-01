package dev.vex.client.net

import dev.vex.common.ChunkDto
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import java.net.Socket

/**
 * Connects to server, reads a header line with payload length, then reads the JSON payload.
 * Returns the deserialized ChunkDto or null on failure.
 */
class ClientNetwork(private val host: String = "127.0.0.1", private val port: Int = 25565) {
    private val json = Json { ignoreUnknownKeys = true }

    fun fetchChunk(): ChunkDto? {
        Socket(host, port).use { sock ->
            val input = BufferedInputStream(sock.getInputStream())
            // read header until newline
            val headerSb = StringBuilder()
            while (true) {
                val b = input.read()
                if (b == -1) return null
                if (b.toChar() == '\n') break
                headerSb.append(b.toChar())
            }
            val len = headerSb.toString().trim().toIntOrNull() ?: return null
            val buffer = ByteArray(len)
            var read = 0
            while (read < len) {
                val r = input.read(buffer, read, len - read)
                if (r == -1) break
                read += r
            }
            val text = String(buffer, 0, read, Charsets.UTF_8)
            return json.decodeFromString(ChunkDto.serializer(), text)
        }
    }
}
