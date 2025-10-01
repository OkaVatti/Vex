package dev.vex.server.net

import dev.vex.common.ChunkDto
import kotlinx.serialization.json.Json
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Paths
import dev.vex.server.persistence.RegionIo
import kotlin.concurrent.thread

/**
 * Very small TCP server.
 * On client connect it sends the chunk_0_0 JSON and then closes the connection.
 */
class ServerNetwork(private val port: Int) {
    private val json = Json { prettyPrint = false }

    fun start() {
        ServerSocket(port).use { serverSocket ->
            while (true) {
                val client = serverSocket.accept()
                thread { handleClient(client) }
            }
        }
    }

    private fun handleClient(socket: Socket) {
        socket.use { s ->
            val out = s.getOutputStream().bufferedWriter()
            // For demo, load chunk 0,0 from disk if present, else generate a new one
            val regionDir = Paths.get("server", "regions")
            val chunk: ChunkDto = RegionIo.loadChunk(regionDir, 0, 0)
                ?: run {
                    val gen = dev.vex.server.WorldGenerator.generateChunk(0, 0)
                    RegionIo.saveChunk(regionDir, gen)
                    gen
                }

            val payload = json.encodeToString(ChunkDto.serializer(), chunk)
            // send length prefix then payload to make client reading stable
            val bytes = payload.toByteArray(Charsets.UTF_8)
            val header = "${bytes.size}\n"
            s.getOutputStream().write(header.toByteArray(Charsets.UTF_8))
            s.getOutputStream().write(bytes)
            s.getOutputStream().flush()
        }
    }
}
