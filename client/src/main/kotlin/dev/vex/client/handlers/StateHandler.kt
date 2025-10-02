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

    fun handleInput(key: Int, action: Int): Boolean {
        if (action != GLFW_PRESS) return false

        when (currentState) {
            GameState.TitleScreen -> handleTitleScreenInput(key)
            GameState.Playing -> {
                if (key == GLFW_KEY_ESCAPE) {
                    currentState = GameState.Paused
                    selectedMenuItem = 0
                    return true
                }
            }
            GameState.Paused -> {
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
                when (selectedMenuItem) {
                    0 -> currentState = GameState.Playing // Play
                    1 -> { /* Options - not implemented yet */ }
                    2 -> glfwSetWindowShouldClose(0, true) // Quit
                }
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
                when (selectedMenuItem) {
                    0 -> currentState = GameState.Playing // Resume
                    1 -> { /* Options */ }
                    2 -> currentState = GameState.TitleScreen // Save and quit
                }
            }
        }
    }

    fun getMenuItems(): List<String> {
        return when (currentState) {
            GameState.TitleScreen -> mainMenuItems
            GameState.Paused -> pauseMenuItems
            else -> emptyList()
        }
    }

    fun getSelectedMenuItem() = selectedMenuItem

    fun isPlaying() = currentState == GameState.Playing
    fun isPaused() = currentState == GameState.Paused
    fun isTitleScreen() = currentState == GameState.TitleScreen
}