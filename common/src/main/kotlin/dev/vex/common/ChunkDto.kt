package dev.vex.common

import kotlinx.serialization.Serializable

@Serializable
data class ChunkDto(
    val chunkX: Int,
    val chunkZ: Int,
    val height: Int,
    val sections: List<ChunkSectionDto> = emptyList()
)
