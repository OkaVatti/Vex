package dev.vex.client

import org.lwjgl.glfw.GLFW.*

/**
 * Comprehensive game state model and manager.
 * This implementation is self-contained and avoids references to undefined constants.
 * It provides:
 *  - Title screen and main menu
 *  - Submenus for Singleplayer / Multiplayer / LAN / Settings
 *  - Playing / Paused states
 *  - A simple requestExit flag instead of directly calling GLFW inside the manager
 */
sealed class GameState {
    object TitleScreen : GameState()
    object MainMenu : GameState()
    object SingleplayerMenu : GameState()
    object MultiplayerMenu : GameState()
    object LANMenu : GameState()
    object SettingsMenu : GameState()
    object ControlsMenu : GameState()

    data class WorldList(val worlds: List<String>) : GameState()
    data class CreateWorld(val defaultName: String = "New World") : GameState()

    data class ServerList(val servers: List<String>) : GameState()
    data class AddServer(val defaultName: String = "", val defaultIP: String = "") : GameState()

    data class LANWorldList(val lanWorlds: List<String>) : GameState()

    data class Playing(val worldName: String) : GameState()
    data class Paused(val worldName: String) : GameState()
}

/**
 * GameStateManager: keeps track of the active UI/game state and handles key input at a high level.
 *
 * NOTE: This class purposefully does not call GLFW window functions (like glfwSetWindowShouldClose)
 * because that couples it to window handles; instead it exposes requestExit so the caller (VexGame)
 * can close the window when appropriate.
 */
class GameStateManager {
    // The current state of the game. Start on the title screen.
    var currentState: GameState = GameState.TitleScreen
        private set

    private var selectedIndex = 0
    private var subSelectedIndex = 0

    // Expose whether the caller should exit the application
    var requestExit: Boolean = false
        private set

    // Public read helpers:
    fun getSelectedIndex() = selectedIndex
    fun getSubSelectedIndex() = subSelectedIndex
    fun getMenuItems(): List<String> {
        return when (val s = currentState) {
            GameState.TitleScreen -> listOf("Play", "Options", "Quit")
            GameState.MainMenu -> listOf("Singleplayer", "Multiplayer", "LAN Co-op", "Settings", "Quit Game")
            GameState.SingleplayerMenu -> listOf("World List", "Create New World", "Back")
            is GameState.WorldList -> s.worlds + listOf("Back")
            is GameState.CreateWorld -> listOf(
                "World Name: ${s.defaultName}",
                "Create",
                "Cancel"
            )
            GameState.MultiplayerMenu -> listOf("Server List", "Add Server", "Back")
            is GameState.ServerList -> s.servers + listOf("Back")
            is GameState.AddServer -> listOf("Name: ${s.defaultName}", "IP: ${s.defaultIP}", "Add", "Cancel")
            GameState.LANMenu -> listOf("Refresh LAN Worlds", "Back")
            is GameState.LANWorldList -> s.lanWorlds + listOf("Back")
            GameState.SettingsMenu -> listOf("Video", "Audio", "Controls", "Back")
            GameState.ControlsMenu -> listOf("Rebind Keys", "Back")
            is GameState.Playing -> listOf("Resume", "Options", "Save and Quit")
            is GameState.Paused -> listOf("Resume", "Options", "Save and Quit")
        }
    }

    fun getSelectedMenuItem() = selectedIndex

    /**
     * Handle input. Returns true if the input was handled by the state manager.
     * Signature matches your existing code: `handleInput(key, action)` from VexGame.
     */
    fun handleInput(key: Int, action: Int): Boolean {
        if (action != GLFW_PRESS) return false

        // Common navigation keys
        when (key) {
            GLFW_KEY_UP, GLFW_KEY_W -> {
                selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
                return true
            }
            GLFW_KEY_DOWN, GLFW_KEY_S -> {
                // ensure we don't go past available items
                val max = getMenuItems().size - 1
                selectedIndex = (selectedIndex + 1).coerceAtMost(max.coerceAtLeast(0))
                return true
            }
            GLFW_KEY_LEFT -> {
                // optional sub navigation
                subSelectedIndex = (subSelectedIndex - 1).coerceAtLeast(0)
                return true
            }
            GLFW_KEY_RIGHT -> {
                subSelectedIndex++
                return true
            }
            GLFW_KEY_ESCAPE -> {
                // Back out depending on current state
                when (currentState) {
                    GameState.TitleScreen -> {
                        // On title screen, ESC => exit
                        requestExit = true
                        return true
                    }
                    is GameState.Playing -> {
                        // Pause the game
                        currentState = GameState.Paused((currentState as GameState.Playing).worldName)
                        selectedIndex = 0
                        return true
                    }
                    is GameState.Paused -> {
                        // Unpause
                        currentState = GameState.Playing((currentState as GameState.Paused).worldName)
                        return true
                    }
                    GameState.MainMenu -> {
                        // Back to title
                        currentState = GameState.TitleScreen
                        selectedIndex = 0
                        return true
                    }
                    GameState.SingleplayerMenu, GameState.MultiplayerMenu, GameState.LANMenu, GameState.SettingsMenu, GameState.ControlsMenu -> {
                        currentState = GameState.MainMenu
                        selectedIndex = 0
                        return true
                    }
                    is GameState.WorldList, is GameState.CreateWorld -> {
                        currentState = GameState.SingleplayerMenu
                        selectedIndex = 0
                        return true
                    }
                    is GameState.ServerList, is GameState.AddServer -> {
                        currentState = GameState.MultiplayerMenu
                        selectedIndex = 0
                        return true
                    }
                    is GameState.LANWorldList -> {
                        currentState = GameState.LANMenu
                        selectedIndex = 0
                        return true
                    }
                    else -> {
                        // fallback: go to MainMenu
                        currentState = GameState.MainMenu
                        selectedIndex = 0
                        return true
                    }
                }
            }
            GLFW_KEY_ENTER, GLFW_KEY_SPACE -> {
                performSelection()
                return true
            }
        }

        return false
    }

    private fun performSelection() {
        when (val s = currentState) {
            GameState.TitleScreen -> {
                when (selectedIndex) {
                    0 -> currentState = GameState.MainMenu
                    1 -> currentState = GameState.SettingsMenu
                    2 -> requestExit = true
                }
                selectedIndex = 0
            }
            GameState.MainMenu -> {
                when (selectedIndex) {
                    0 -> currentState = GameState.SingleplayerMenu
                    1 -> currentState = GameState.MultiplayerMenu
                    2 -> currentState = GameState.LANMenu
                    3 -> currentState = GameState.SettingsMenu
                    4 -> requestExit = true
                }
                selectedIndex = 0
            }
            GameState.SingleplayerMenu -> {
                when (selectedIndex) {
                    0 -> { // world list - we don't have world scanning here, caller should push a real list
                        currentState = GameState.WorldList(emptyList())
                    }
                    1 -> currentState = GameState.CreateWorld()
                    2 -> currentState = GameState.MainMenu
                }
                selectedIndex = 0
            }
            is GameState.WorldList -> {
                val worlds = s.worlds
                if (selectedIndex < worlds.size) {
                    val worldName = worlds[selectedIndex]
                    currentState = GameState.Playing(worldName)
                } else {
                    currentState = GameState.SingleplayerMenu
                }
                selectedIndex = 0
            }
            is GameState.CreateWorld -> {
                when (selectedIndex) {
                    0 -> {
                        // Name entry: in a real UI you'd switch to text entry mode
                    }
                    1 -> {
                        // Create with default name
                        currentState = GameState.Playing(s.defaultName)
                    }
                    2 -> {
                        currentState = GameState.SingleplayerMenu
                    }
                }
                selectedIndex = 0
            }
            GameState.MultiplayerMenu -> {
                when (selectedIndex) {
                    0 -> currentState = GameState.ServerList(emptyList())
                    1 -> currentState = GameState.AddServer()
                    2 -> currentState = GameState.MainMenu
                }
                selectedIndex = 0
            }
            is GameState.ServerList -> {
                // connect or back
                if (selectedIndex < s.servers.size) {
                    // TODO: connect logic handled by caller/network code
                } else {
                    currentState = GameState.MultiplayerMenu
                }
                selectedIndex = 0
            }
            is GameState.AddServer -> {
                when (selectedIndex) {
                    2 -> currentState = GameState.MultiplayerMenu // Add
                    3 -> currentState = GameState.MultiplayerMenu // Cancel
                }
                selectedIndex = 0
            }
            GameState.LANMenu -> {
                when (selectedIndex) {
                    0 -> currentState = GameState.LANWorldList(emptyList())
                    1 -> currentState = GameState.MainMenu
                }
                selectedIndex = 0
            }
            is GameState.LANWorldList -> {
                if (selectedIndex < s.lanWorlds.size) {
                    // TODO: network join
                } else {
                    currentState = GameState.LANMenu
                }
                selectedIndex = 0
            }
            GameState.SettingsMenu -> {
                when (selectedIndex) {
                    0 -> { /* Video settings */ }
                    1 -> { /* Audio settings */ }
                    2 -> currentState = GameState.ControlsMenu
                    3 -> currentState = GameState.MainMenu
                }
                selectedIndex = 0
            }
            GameState.ControlsMenu -> {
                if (selectedIndex == getMenuItems().lastIndex) {
                    currentState = GameState.SettingsMenu
                }
                selectedIndex = 0
            }
            is GameState.Playing -> {
                // If playing and pressed enter/space on menu, open pause
                currentState = GameState.Paused(s.worldName)
                selectedIndex = 0
            }
            is GameState.Paused -> {
                when (selectedIndex) {
                    0 -> currentState = GameState.Playing(s.worldName)
                    1 -> currentState = GameState.SettingsMenu
                    2 -> currentState = GameState.TitleScreen
                }
                selectedIndex = 0
            }
        }
    }

    fun isPlaying(): Boolean = currentState is GameState.Playing
    fun isPaused(): Boolean = currentState is GameState.Paused
    fun isTitleScreen(): Boolean = currentState is GameState.TitleScreen

    /**
     * Reset the exit request flag (caller may call this after handling exit)
     */
    fun clearExitRequest() {
        requestExit = false
    }
}
