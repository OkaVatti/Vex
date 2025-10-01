package dev.vex.common

import kotlinx.serialization.Serializable

object SectionConstants {
    const val SECTION_SIZE = 16
    const val SECTION_VOLUME = SECTION_SIZE * SECTION_SIZE * SECTION_SIZE
}

@Serializable
data class ChunkSectionDto(
    val yIndex: Int,
    val blocks: List<Int> = emptyList()
)
