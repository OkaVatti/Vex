package dev.vex.client.menu

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Complete menu state system for the game
 */
sealed class MenuState {
    object MainMenu : MenuState()
    object SingleplayerMenu : MenuState()
    data class WorldList(val worlds: List<WorldInfo>) : MenuState()
    data class CreateWorld(
        var worldName: String = "New World",
        var seed: String = "",
        var biomeSize: String = "Normal",
        var structures: Boolean = true
    ) : MenuState()
    object MultiplayerMenu : MenuState()
    data class ServerList(val servers: List<ServerInfo>) : MenuState()
    data class AddServer(var serverName: String = "", var serverIP: String = "", var port: String = "25565") : MenuState()
    object LANMenu : MenuState()
    data class LANWorldList(val lanWorlds: List<LANWorldInfo>) : MenuState()
    object SettingsMenu : MenuState()
    object ControlsMenu : MenuState()
    data class Playing(val worldName: String) : MenuState()
    data class Paused(val worldName: String) : MenuState()
}

data class WorldInfo(
    val name: String,
    val seed: Long,
    val lastPlayed: LocalDateTime,
    val gameMode: String = "Survival",
    val folder: File
) {
    fun getLastPlayedFormatted(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return lastPlayed.format(formatter)
    }
}

data class ServerInfo(
    val name: String,
    val ip: String,
    val port: Int,
    var online: Boolean = false,
    var playerCount: Int = 0,
    var maxPlayers: Int = 20
)

data class LANWorldInfo(
    val hostName: String,
    val worldName: String,
    val ip: String,
    val port: Int,
    val playerCount: Int
)

/**
 * Settings configuration
 */
data class GameSettings(
    var fov: Float = 70f,
    var renderDistance: Int = 8,
    var vsync: Boolean = true,
    var mouseSensitivity: Float = 0.15f,

    // Controls
    var keyForward: Int = 87,      // W
    var keyLeft: Int = 65,         // A
    var keyBack: Int = 83,         // S
    var keyRight: Int = 68,        // D
    var keyJump: Int = 32,         // Space
    var keySprint: Int = 340,      // Left Shift
    var keySneak: Int = 341,       // Left Ctrl
    var keyCrawl: Int = 67,        // C
    var keyInventory: Int = 69,    // E
    var keyDrop: Int = 81,         // Q
    var keyMap: Int = 77,          // M
    var keyChat: Int = 84          // T
)

/**
 * World manager for loading/saving worlds
 */
class WorldManager {
    private val worldsFolder = File("worlds")

    init {
        if (!worldsFolder.exists()) {
            worldsFolder.mkdirs()
        }
    }

    fun listWorlds(): List<WorldInfo> {
        val worlds = mutableListOf<WorldInfo>()

        worldsFolder.listFiles()?.forEach { folder ->
            if (folder.isDirectory) {
                val levelFile = File(folder, "level.dat")
                if (levelFile.exists()) {
                    try {
                        // Read world info from level.dat
                        val name = folder.name
                        val seed = 0L // TODO: Read from file
                        val lastPlayed = LocalDateTime.now() // TODO: Read from file

                        worlds.add(WorldInfo(name, seed, lastPlayed, "Survival", folder))
                    } catch (e: Exception) {
                        println("Failed to load world: ${folder.name}")
                    }
                }
            }
        }

        return worlds.sortedByDescending { it.lastPlayed }
    }

    fun createWorld(name: String, seed: String, biomeSize: String, structures: Boolean): WorldInfo {
        val sanitizedName = name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val worldFolder = File(worldsFolder, sanitizedName)

        if (!worldFolder.exists()) {
            worldFolder.mkdirs()
        }

        // Create level.dat
        val levelFile = File(worldFolder, "level.dat")
        // TODO: Write world data to file
        levelFile.writeText("seed=$seed\nbiomeSize=$biomeSize\nstructures=$structures")

        val parsedSeed = if (seed.isEmpty()) System.currentTimeMillis() else {
            try {
                seed.toLong()
            } catch (e: NumberFormatException) {
                seed.hashCode().toLong()
            }
        }

        return WorldInfo(sanitizedName, parsedSeed, LocalDateTime.now(), "Survival", worldFolder)
    }

    fun deleteWorld(world: WorldInfo) {
        world.folder.deleteRecursively()
    }
}

/**
 * Server list manager
 */
class ServerManager {
    private val serversFile = File("servers.dat")
    private val servers = mutableListOf<ServerInfo>()

    init {
        loadServers()
    }

    private fun loadServers() {
        if (serversFile.exists()) {
            try {
                serversFile.readLines().forEach { line ->
                    val parts = line.split("|")
                    if (parts.size >= 3) {
                        servers.add(ServerInfo(
                            name = parts[0],
                            ip = parts[1],
                            port = parts[2].toIntOrNull() ?: 25565
                        ))
                    }
                }
            } catch (e: Exception) {
                println("Failed to load servers: ${e.message}")
            }
        }
    }

    fun getServers(): List<ServerInfo> = servers.toList()

    fun addServer(name: String, ip: String, port: Int) {
        val server = ServerInfo(name, ip, port)
        servers.add(server)
        saveServers()
    }

    fun removeServer(server: ServerInfo) {
        servers.remove(server)
        saveServers()
    }

    private fun saveServers() {
        try {
            serversFile.writeText(
                servers.joinToString("\n") { "${it.name}|${it.ip}|${it.port}" }
            )
        } catch (e: Exception) {
            println("Failed to save servers: ${e.message}")
        }
    }

    fun pingServer(server: ServerInfo) {
        // TODO: Implement actual server ping
        server.online = false
        server.playerCount = 0
    }
}

/**
 * LAN discovery manager
 */
class LANManager {
    private val discoveredWorlds = mutableListOf<LANWorldInfo>()

    fun startDiscovery() {
        // TODO: Implement UDP broadcast discovery
        println("Starting LAN discovery...")
    }

    fun stopDiscovery() {
        println("Stopping LAN discovery...")
    }

    fun getDiscoveredWorlds(): List<LANWorldInfo> = discoveredWorlds.toList()

    fun broadcastWorld(worldName: String, port: Int) {
        // TODO: Implement UDP broadcast
        println("Broadcasting world: $worldName on port $port")
    }
}