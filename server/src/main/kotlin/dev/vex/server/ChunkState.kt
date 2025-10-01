package dev.vex.server

/**
 * Very small chunk lifecycle states used by the simple server.
 */
enum class ChunkState {
    NEW,        // created but not generated
    GENERATED,  // generation finished but not yet ready for use
    READY       // fully ready (lighting/meshing done)
}
