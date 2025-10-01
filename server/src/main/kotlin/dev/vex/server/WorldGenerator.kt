package dev.vex.server

import dev.vex.common.BlockType
import dev.vex.common.ChunkDto
import dev.vex.common.ChunkSectionDto
import dev.vex.common.SectionConstants

/**
 * Very small deterministic generator for demo.
 * Produces a flat-ish terrain with stone/dirt/grass layers.
 */
object WorldGenerator {
    fun generateChunk(chunkX: Int, chunkZ: Int, totalHeightBlocks: Int = 256): ChunkDto {
        val sectionsCount = (totalHeightBlocks + SectionConstants.SECTION_SIZE - 1) / SectionConstants.SECTION_SIZE
        val sections = mutableListOf<ChunkSectionDto>()

        for (sectionIdx in 0 until sectionsCount) {
            val blocks = ArrayList<Int>(SectionConstants.SECTION_VOLUME)

            val yBase = sectionIdx * SectionConstants.SECTION_SIZE
            for (localY in 0 until SectionConstants.SECTION_SIZE) {
                val worldY = yBase + localY
                for (z in 0 until SectionConstants.SECTION_SIZE) {
                    for (x in 0 until SectionConstants.SECTION_SIZE) {
                        val id = when {
                            worldY < 60 -> BlockType.STONE.id
                            worldY < 63 -> BlockType.DIRT.id
                            worldY == 63 -> BlockType.GRASS.id
                            else -> BlockType.AIR.id
                        }
                        blocks.add(id)
                    }
                }
            }
            sections.add(ChunkSectionDto(sectionIdx, blocks))
        }

        return ChunkDto(chunkX, chunkZ, totalHeightBlocks, sections)
    }
}
