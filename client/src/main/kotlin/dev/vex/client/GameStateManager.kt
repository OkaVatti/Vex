package dev.vex.client

import org.lwjgl.glfw.GLFW.*

/**
 * Manages game states (title screen, playing, paused)
 */
sealed class GameState {
    object TitleScreen : GameState()
    object Playing : GameState()
    object Paused : GameState()
}

class GameStateManager {
    var currentState: GameState = GameState.TitleScreen
        private set

    private var showMainMenu = true
    private var selectedMenuItem = 0
    private val mainMenuItems = listOf("Play", "Options", "Quit")
    private val pauseMenuItems = listOf("Resume", "Options", "Save and Quit")

    // small flag to request closing the game (used by UI click)
    private var quitRequested = false

    /**
     * Called from keyboard callbacks.
     * Returns true if the state manager handled the key (e.g., Enter on menu).
     */
    fun handleInput(key: Int, action: Int): Boolean {
        if (action != GLFW_PRESS) return false

        when (currentState) {
            is GameState.TitleScreen -> {
                handleTitleScreenInput(key)
            }
            is GameState.Playing -> {
                if (key == GLFW_KEY_ESCAPE) {
                    currentState = GameState.Paused
                    selectedMenuItem = 0
                    return true
                }
            }
            is GameState.Paused -> {
                if (key == GLFW_KEY_ESCAPE) {
                    currentState = GameState.Playing
                    return true
                }
                handlePauseMenuInput(key)
            }
        }
        return false
    }

    private fun handleTitleScreenInput(key: Int) {
        when (key) {
            GLFW_KEY_UP, GLFW_KEY_W -> {
                selectedMenuItem = (selectedMenuItem - 1).coerceAtLeast(0)
            }
            GLFW_KEY_DOWN, GLFW_KEY_S -> {
                selectedMenuItem = (selectedMenuItem + 1).coerceAtMost(mainMenuItems.size - 1)
            }
            GLFW_KEY_ENTER, GLFW_KEY_SPACE -> {
                activateSelected()
            }
        }
    }

    private fun handlePauseMenuInput(key: Int) {
        when (key) {
            GLFW_KEY_UP, GLFW_KEY_W -> {
                selectedMenuItem = (selectedMenuItem - 1).coerceAtLeast(0)
            }
            GLFW_KEY_DOWN, GLFW_KEY_S -> {
                selectedMenuItem = (selectedMenuItem + 1).coerceAtMost(pauseMenuItems.size - 1)
            }
            GLFW_KEY_ENTER, GLFW_KEY_SPACE -> {
                activateSelected()
            }
        }
    }

    fun getMenuItems(): List<String> {
        return when (currentState) {
            is GameState.TitleScreen -> mainMenuItems
            is GameState.Paused -> pauseMenuItems
            else -> emptyList()
        }
    }

    fun getSelectedMenuItem() = selectedMenuItem

    fun isPlaying() = currentState == GameState.Playing
    fun isPaused() = currentState == GameState.Paused
    fun isTitleScreen() = currentState == GameState.TitleScreen

    // --- Public helpers used by UI renderer / mouse clicks ---

    fun setSelectedMenuItem(index: Int) {
        val max = getMenuItems().size - 1
        selectedMenuItem = index.coerceIn(0, if (max >= 0) max else 0)
    }

    /**
     * Activate (press) the currently selected item. This mirrors pressing ENTER.
     * Returns true if an action occurred (handy for callers).
     */
    fun activateSelected(): Boolean {
        when (currentState) {
            is GameState.TitleScreen -> {
                when (selectedMenuItem) {
                    0 -> { // Play
                        currentState = GameState.Playing
                        return true
                    }
                    1 -> { // Options - not implemented
                        // For now do nothing
                        return true
                    }
                    2 -> { // Quit
                        quitRequested = true
                        return true
                    }
                }
            }
            is GameState.Paused -> {
                when (selectedMenuItem) {
                    0 -> { // Resume
                        currentState = GameState.Playing
                        return true
                    }
                    1 -> { /* Options - not implemented */ return true }
                    2 -> { // Save and quit -> back to title screen
                        currentState = GameState.TitleScreen
                        return true
                    }
                }
            }
            else -> {}
        }
        return false
    }

    fun shouldQuit(): Boolean = quitRequested

    /**
     * Reset quit request flag (caller may call after responding).
     */
    fun clearQuitRequest() {
        quitRequested = false
    }
}