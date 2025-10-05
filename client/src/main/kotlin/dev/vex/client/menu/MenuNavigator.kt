package dev.vex.client.menu

import org.lwjgl.glfw.GLFW.*

/**
 * Menu navigation and input handling based on a tree of MenuNodes.
 */
class MenuNavigator {
    private val root: MenuNode = MenuBuilder.createMainMenu()
    private val stack = ArrayDeque<MenuNode>()

    var current: MenuNode = root
        private set

    var selectedIndex: Int = 0
        private set

    var listener: MenuListener? = null

    interface MenuListener {
        fun onStartSingleplayer(name: String, seed: Long)
        fun onJoinServer(ip: String, port: Int)
        fun onQuit()
    }

    fun getMenuItems(): List<String> {
        return current.children.map { node ->
            if (node.isInput) {
                "${node.title}: ${node.inputBuffer}"
            } else {
                node.title
            }
        }
    }

    fun handleKeyPress(key: Int, action: Int): Boolean {
        if (action != GLFW_PRESS && action != GLFW_REPEAT) return false

        if (current.children.isEmpty()) return false

        val selectedNode = current.children.getOrNull(selectedIndex)

        when (key) {
            GLFW_KEY_UP -> moveSelection(-1)
            GLFW_KEY_DOWN -> moveSelection(1)
            GLFW_KEY_LEFT, GLFW_KEY_ESCAPE -> goBack()
            GLFW_KEY_RIGHT, GLFW_KEY_ENTER -> {
                if (selectedNode != null) {
                    if (selectedNode.isInput) return true // Input handled by char callback
                    if (selectedNode.children.isNotEmpty()) {
                        enterSubmenu(selectedNode)
                    } else {
                        handleLeafAction(selectedNode)
                    }
                }
            }
            GLFW_KEY_BACKSPACE -> {
                if (selectedNode != null && selectedNode.isInput && selectedNode.inputBuffer.isNotEmpty()) {
                    selectedNode.inputBuffer.deleteCharAt(selectedNode.inputBuffer.length - 1)
                }
            }
            else -> return false // Not a key we handle
        }
        return true
    }

    fun handleCharInput(ch: Char) {
        val node = current.children.getOrNull(selectedIndex) ?: return
        if (node.isInput && !ch.isISOControl()) {
            node.inputBuffer.append(ch)
        }
    }

    private fun moveSelection(delta: Int) {
        if (current.children.isEmpty()) return
        val numItems = current.children.size
        var newIndex = selectedIndex + delta
        // Wrap around
        if (newIndex < 0) newIndex = numItems - 1
        if (newIndex >= numItems) newIndex = 0

        // Skip non-selectable items
        var attempts = 0
        while (!current.children[newIndex].selectable && attempts < numItems) {
            newIndex = (newIndex + delta).let { if (it < 0) numItems - 1 else it % numItems }
            attempts++
        }
        selectedIndex = newIndex
    }

    private fun enterSubmenu(node: MenuNode) {
        stack.addLast(current)
        current = node
        selectedIndex = 0
    }

    private fun goBack() {
        if (stack.isNotEmpty()) {
            current = stack.removeLast()
            selectedIndex = 0
        }
    }

    private fun handleLeafAction(node: MenuNode) {
        when (node.id) {
            "start_singleplayer" -> {
                val worldCreateNode = findNodeInTree(root, "world_create")
                val nameNode = worldCreateNode?.children?.find { it.id == "world_name" }
                val seedNode = worldCreateNode?.children?.find { it.id == "world_seed" }
                val worldName = nameNode?.inputBuffer?.toString()?.ifEmpty { "New World" } ?: "New World"
                val seedText = seedNode?.inputBuffer?.toString()
                val seed = seedText?.toLongOrNull() ?: System.currentTimeMillis()
                listener?.onStartSingleplayer(worldName, seed)
            }
            "add_server_confirm" -> {
                val addServerNode = findNodeInTree(root, "add_server")
                val ipNode = addServerNode?.children?.find { it.id == "add_server_ip" }
                val portNode = addServerNode?.children?.find { it.id == "add_server_port" }
                val ip = ipNode?.inputBuffer?.toString()?.ifEmpty { "127.0.0.1" } ?: "127.0.0.1"
                val port = portNode?.inputBuffer?.toString()?.toIntOrNull() ?: 25565
                listener?.onJoinServer(ip, port)
            }
            "quit" -> listener?.onQuit()
        }
    }

    private fun findNodeInTree(startNode: MenuNode, id: String): MenuNode? {
        if (startNode.id == id) return startNode
        for (child in startNode.children) {
            val found = findNodeInTree(child, id)
            if (found != null) return found
        }
        return null
    }
}