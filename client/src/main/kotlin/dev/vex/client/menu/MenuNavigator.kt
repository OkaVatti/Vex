package dev.vex.client.menu

import org.lwjgl.glfw.GLFW.*

/**
 * Handles menu navigation and input
 */
class MenuNavigator {
    var currentState: MenuState = MenuState.MainMenu
        private set

    var selectedIndex = 0
        private set

    private val worldManager = WorldManager()
    private val serverManager = ServerManager()
    private val lanManager = LANManager()
    val settings = GameSettings()

    // Text input state
    private var textInput = ""
    private var typing = false

    // Quit flag (UI asks menu navigator whether the game should exit)
    private var quitRequested = false

    fun handleKeyPress(key: Int, action: Int): Boolean {
        if (action != GLFW_PRESS) return false

        // Handle text input mode
        if (typing) {
            return handleTextInput(key)
        }

        // Navigate menus
        when (key) {
            GLFW_KEY_UP, GLFW_KEY_W -> {
                selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
                return true
            }
            GLFW_KEY_DOWN, GLFW_KEY_S -> {
                selectedIndex = (selectedIndex + 1).coerceAtMost(getMaxIndex())
                return true
            }
            GLFW_KEY_ENTER, GLFW_KEY_SPACE -> {
                handleSelection()
                return true
            }
            GLFW_KEY_ESCAPE -> {
                handleBack()
                return true
            }
        }

        return false
    }

    fun handleCharInput(char: Char) {
        if (typing) {
            textInput += char
        }
    }

    private fun handleTextInput(key: Int): Boolean {
        when (key) {
            GLFW_KEY_BACKSPACE -> {
                if (textInput.isNotEmpty()) {
                    textInput = textInput.dropLast(1)
                }
                return true
            }
            GLFW_KEY_ENTER -> {
                typing = false
                applyTextInput()
                return true
            }
            GLFW_KEY_ESCAPE -> {
                typing = false
                textInput = ""
                return true
            }
        }
        return false
    }

    private fun applyTextInput() {
        when (val state = currentState) {
            is MenuState.CreateWorld -> {
                when (selectedIndex) {
                    0 -> state.worldName = textInput
                    1 -> state.seed = textInput
                }
            }
            is MenuState.AddServer -> {
                when (selectedIndex) {
                    0 -> state.serverName = textInput
                    1 -> state.serverIP = textInput
                    2 -> state.port = textInput
                }
            }
            else -> {}
        }
        textInput = ""
    }

    // Public so UI can call it on mouse click
    fun handleSelection() {
        when (val state = currentState) {
            MenuState.MainMenu -> handleMainMenuSelection()
            MenuState.SingleplayerMenu -> handleSingleplayerSelection()
            is MenuState.WorldList -> handleWorldListSelection(state)
            is MenuState.CreateWorld -> handleCreateWorldSelection(state)
            MenuState.MultiplayerMenu -> handleMultiplayerSelection()
            is MenuState.ServerList -> handleServerListSelection(state)
            is MenuState.AddServer -> handleAddServerSelection(state)
            MenuState.LANMenu -> handleLANSelection()
            is MenuState.LANWorldList -> handleLANWorldListSelection(state)
            MenuState.SettingsMenu -> handleSettingsSelection()
            MenuState.ControlsMenu -> handleControlsSelection()
            else -> {}
        }
    }

    private fun handleMainMenuSelection() {
        when (selectedIndex) {
            0 -> { // Singleplayer
                currentState = MenuState.SingleplayerMenu
                selectedIndex = 0
            }
            1 -> { // Multiplayer
                currentState = MenuState.MultiplayerMenu
                selectedIndex = 0
            }
            2 -> { // LAN Co-op
                currentState = MenuState.LANMenu
                selectedIndex = 0
                lanManager.startDiscovery()
            }
            3 -> { // Settings
                currentState = MenuState.SettingsMenu
                selectedIndex = 0
            }
            4 -> { // Quit
                quitRequested = true
            }
        }
    }

    private fun handleSingleplayerSelection() {
        when (selectedIndex) {
            0 -> { // World List
                val worlds = worldManager.listWorlds()
                currentState = MenuState.WorldList(worlds)
                selectedIndex = 0
            }
            1 -> { // Create New World
                currentState = MenuState.CreateWorld()
                selectedIndex = 0
            }
            2 -> { // Back
                currentState = MenuState.MainMenu
                selectedIndex = 0
            }
        }
    }

    private fun handleWorldListSelection(state: MenuState.WorldList) {
        if (selectedIndex < state.worlds.size) {
            val world = state.worlds[selectedIndex]
            currentState = MenuState.Playing(world.name)
        } else if (selectedIndex == state.worlds.size) {
            currentState = MenuState.SingleplayerMenu
            selectedIndex = 0
        }
    }

    private fun handleCreateWorldSelection(state: MenuState.CreateWorld) {
        when (selectedIndex) {
            0 -> { typing = true; textInput = state.worldName }
            1 -> { typing = true; textInput = state.seed }
            2 -> { state.biomeSize = when (state.biomeSize) { "Small" -> "Normal"; "Normal" -> "Large"; else -> "Small" } }
            3 -> { state.structures = !state.structures }
            4 -> {
                val world = worldManager.createWorld(state.worldName, state.seed, state.biomeSize, state.structures)
                currentState = MenuState.Playing(world.name)
            }
            5 -> { currentState = MenuState.SingleplayerMenu; selectedIndex = 0 }
        }
    }

    private fun handleMultiplayerSelection() {
        when (selectedIndex) {
            0 -> { val servers = serverManager.getServers(); currentState = MenuState.ServerList(servers); selectedIndex = 0 }
            1 -> { currentState = MenuState.AddServer(); selectedIndex = 0 }
            2 -> { currentState = MenuState.MainMenu; selectedIndex = 0 }
        }
    }

    private fun handleServerListSelection(state: MenuState.ServerList) {
        if (selectedIndex < state.servers.size) {
            val server = state.servers[selectedIndex]
            println("Connecting to ${server.name} at ${server.ip}:${server.port}")
        } else if (selectedIndex == state.servers.size) {
            currentState = MenuState.MultiplayerMenu
            selectedIndex = 0
        }
    }

    private fun handleAddServerSelection(state: MenuState.AddServer) {
        when (selectedIndex) {
            0 -> { typing = true; textInput = state.serverName }
            1 -> { typing = true; textInput = state.serverIP }
            2 -> { typing = true; textInput = state.port }
            3 -> { val port = state.port.toIntOrNull() ?: 25565; serverManager.addServer(state.serverName, state.serverIP, port); currentState = MenuState.MultiplayerMenu; selectedIndex = 0 }
            4 -> { println("Connecting to ${state.serverIP}:${state.port}") }
            5 -> { currentState = MenuState.MultiplayerMenu; selectedIndex = 0 }
        }
    }

    private fun handleLANSelection() {
        when (selectedIndex) {
            0 -> { val lanWorlds = lanManager.getDiscoveredWorlds(); currentState = MenuState.LANWorldList(lanWorlds); selectedIndex = 0 }
            1 -> { lanManager.stopDiscovery(); currentState = MenuState.MainMenu; selectedIndex = 0 }
        }
    }

    private fun handleLANWorldListSelection(state: MenuState.LANWorldList) {
        if (selectedIndex < state.lanWorlds.size) {
            val world = state.lanWorlds[selectedIndex]
            println("Joining LAN world: ${world.worldName} at ${world.ip}:${world.port}")
        } else if (selectedIndex == state.lanWorlds.size) {
            currentState = MenuState.LANMenu
            selectedIndex = 0
        }
    }

    private fun handleSettingsSelection() {
        when (selectedIndex) {
            0 -> {}
            1 -> { settings.renderDistance = when (settings.renderDistance) { 4 -> 8; 8 -> 12; 12 -> 16; else -> 4 } }
            2 -> { settings.vsync = !settings.vsync }
            3 -> { currentState = MenuState.ControlsMenu; selectedIndex = 0 }
            4 -> { currentState = MenuState.MainMenu; selectedIndex = 0 }
        }
    }

    private fun handleControlsSelection() {
        if (selectedIndex == 12) {
            currentState = MenuState.SettingsMenu
            selectedIndex = 0
        }
    }

    private fun handleBack() {
        currentState = when (currentState) {
            MenuState.SingleplayerMenu, MenuState.MultiplayerMenu, MenuState.LANMenu, MenuState.SettingsMenu -> {
                MenuState.MainMenu.also { selectedIndex = 0 }
            }
            is MenuState.WorldList, is MenuState.CreateWorld -> {
                MenuState.SingleplayerMenu.also { selectedIndex = 0 }
            }
            is MenuState.ServerList, is MenuState.AddServer -> {
                MenuState.MultiplayerMenu.also { selectedIndex = 0 }
            }
            is MenuState.LANWorldList -> {
                MenuState.LANMenu.also { selectedIndex = 0 }
            }
            MenuState.ControlsMenu -> {
                MenuState.SettingsMenu.also { selectedIndex = 0 }
            }
            is MenuState.Paused -> {
                (currentState as MenuState.Paused).let {
                    MenuState.Playing(it.worldName)
                }
            }
            else -> currentState
        }
    }

    private fun getMaxIndex(): Int {
        return when (val state = currentState) {
            MenuState.MainMenu -> 4
            MenuState.SingleplayerMenu -> 2
            is MenuState.WorldList -> state.worlds.size
            is MenuState.CreateWorld -> 5
            MenuState.MultiplayerMenu -> 2
            is MenuState.ServerList -> state.servers.size
            is MenuState.AddServer -> 5
            MenuState.LANMenu -> 1
            is MenuState.LANWorldList -> state.lanWorlds.size
            MenuState.SettingsMenu -> 4
            MenuState.ControlsMenu -> 12
            else -> 0
        }
    }

    fun getMenuItems(): List<String> {
        return when (val state = currentState) {
            MenuState.MainMenu -> listOf("Singleplayer", "Multiplayer", "LAN Co-op", "Settings", "Quit Game")
            MenuState.SingleplayerMenu -> listOf("World List", "Create New World", "Back")
            is MenuState.WorldList -> state.worlds.map { it.name } + "Back"
            is MenuState.CreateWorld -> listOf(
                "World Name: ${state.worldName}",
                "Seed: ${if (state.seed.isEmpty()) "Random" else state.seed}",
                "Biome Size: ${state.biomeSize}",
                "Structures: ${if (state.structures) "ON" else "OFF"}",
                "Create World",
                "Cancel"
            )
            MenuState.MultiplayerMenu -> listOf("Server List", "Add Server", "Back")
            is MenuState.ServerList -> state.servers.map { "${it.name} - ${it.ip}:${it.port}" } + "Back"
            is MenuState.AddServer -> listOf(
                "Server Name: ${state.serverName}",
                "IP Address: ${state.serverIP}",
                "Port: ${state.port}",
                "Add Server",
                "Connect",
                "Cancel"
            )
            MenuState.LANMenu -> listOf("Refresh LAN Worlds", "Back")
            is MenuState.LANWorldList -> state.lanWorlds.map { "${it.worldName} (${it.hostName})" } + "Back"
            MenuState.SettingsMenu -> listOf(
                "FOV: ${settings.fov.toInt()}",
                "Render Distance: ${settings.renderDistance}",
                "VSync: ${if (settings.vsync) "ON" else "OFF"}",
                "Controls",
                "Back"
            )
            MenuState.ControlsMenu -> listOf(
                "Forward", "Left", "Back", "Right", "Jump", "Sprint", "Sneak", "Crawl",
                "Inventory", "Drop", "Map", "Chat", "Back"
            )
            else -> emptyList()
        }
    }

    fun isPlaying() = currentState is MenuState.Playing
    fun isPaused() = currentState is MenuState.Paused
    fun isTyping() = typing
    fun getCurrentInput() = textInput

    // Public small helpers for UI
    fun setSelectedIndex(index: Int) {
        selectedIndex = index
    }

    fun isQuitRequested(): Boolean = quitRequested
}
