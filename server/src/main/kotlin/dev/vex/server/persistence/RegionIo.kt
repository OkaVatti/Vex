package dev.vex.server.persistence

import dev.vex.common.ChunkDto
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object RegionIo {
    private val json = Json { prettyPrint = false }

    /**
     * Save a chunk dto as JSON into regionDir/chunk_X_Z.json
     */
    fun saveChunk(regionDir: Path, chunk: ChunkDto) {
        Files.createDirectories(regionDir)
        val file = regionDir.resolve("chunk_${chunk.chunkX}_${chunk.chunkZ}.json").toFile()
        file.outputStream().bufferedWriter().use { writer ->
            writer.write(json.encodeToString(ChunkDto.serializer(), chunk))
        }
    }

    /**
     * Load chunk DTO if it exists, otherwise null.
     */
    fun loadChunk(regionDir: Path, chunkX: Int, chunkZ: Int): ChunkDto? {
        val file = regionDir.resolve("chunk_${chunkX}_${chunkZ}.json").toFile()
        if (!file.exists()) return null
        file.inputStream().bufferedReader().use { reader ->
            val text = reader.readText()
            return json.decodeFromString(ChunkDto.serializer(), text)
        }
    }
}
