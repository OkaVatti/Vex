package dev.vex.common

import kotlinx.serialization.Serializable

@Serializable
enum class BlockType(val id: Int) {
    AIR(0),
    GRASS(1),
    DIRT(2),
    STONE(3);

    companion object {
        private val byId = values().associateBy { it.id }
        fun fromId(id: Int): BlockType = byId[id] ?: AIR
    }
}
