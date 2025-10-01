package dev.vex.server

import dev.vex.server.persistence.RegionIo
import java.nio.file.Paths
import kotlin.concurrent.thread
import dev.vex.server.net.ServerNetwork

fun main() {
    println("Vex server starting (demo)")
    val regionDir = Paths.get("server", "regions")
    val chunk = WorldGenerator.generateChunk(0, 0)
    println("Generated chunk (0,0) sections=${chunk.sections.size}")
    RegionIo.saveChunk(regionDir, chunk)
    println("Saved chunk to disk")
    val loaded = RegionIo.loadChunk(regionDir, 0, 0)
    println("Loaded chunk from disk: ${loaded != null}")
    val server = ServerNetwork(4444)
    thread(name = "server-net") { server.start() }
    println("Server network started on port 4444 - waiting for clients")
    Thread.currentThread().join()
}
