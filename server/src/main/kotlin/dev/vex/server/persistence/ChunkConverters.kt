package dev.vex.server.persistence

import dev.vex.common.ChunkDto
import dev.vex.common.ChunkSectionDto
import dev.vex.common.SectionConstants
import dev.vex.server.ServerChunk
import dev.vex.server.ChunkState

private const val SECTION_HEIGHT = SectionConstants.SECTION_SIZE
private const val SECTION_VOLUME = SectionConstants.SECTION_VOLUME

fun serverChunkToDto(serverChunk: ServerChunk): ChunkDto {
    val sections = mutableListOf<ChunkSectionDto>()
    val numSections = (ServerChunk.HEIGHT + SECTION_HEIGHT - 1) / SECTION_HEIGHT

    for (s in 0 until numSections) {
        val yBase = s * SECTION_HEIGHT
        val blocksList = ArrayList<Int>(SECTION_VOLUME)

        for (y in 0 until SECTION_HEIGHT) {
            val yy = yBase + y
            for (z in 0 until ServerChunk.DEPTH) {
                for (x in 0 until ServerChunk.WIDTH) {
                    val id = if (yy in 0 until ServerChunk.HEIGHT) serverChunk.getBlock(x, yy, z) else 0
                    blocksList.add(id)
                }
            }
        }
        sections.add(ChunkSectionDto(s, blocksList))
    }

    return ChunkDto(
        chunkX = serverChunk.chunkX,
        chunkZ = serverChunk.chunkZ,
        height = ServerChunk.HEIGHT,
        sections = sections
    )
}

fun dtoToServerChunk(dto: ChunkDto): ServerChunk {
    val serverChunk = ServerChunk(dto.chunkX, dto.chunkZ)
    serverChunk.state = ChunkState.GENERATED
    val sectionHeight = SECTION_HEIGHT

    for (section in dto.sections) {
        val yBase = section.yIndex * sectionHeight
        val blocks = section.blocks
        var i = 0
        for (y in 0 until sectionHeight) {
            val yy = yBase + y
            for (z in 0 until ServerChunk.DEPTH) {
                for (x in 0 until ServerChunk.WIDTH) {
                    val id = if (i < blocks.size) blocks[i++] else 0
                    if (yy in 0 until ServerChunk.HEIGHT) {
                        serverChunk.setBlock(x, yy, z, id)
                    }
                }
            }
        }
    }
    serverChunk.state = ChunkState.READY
    return serverChunk
}
