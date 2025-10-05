package dev.vex.client.menu

import org.lwjgl.glfw.GLFW.*

/**
 * Menu navigation and input handling.
 *
 * This class:
 *  - exposes the current menu items via getMenuItems()
 *  - tracks selectedIndex
 *  - handles keyboard navigation and char input
 *  - supports a MenuListener to react to important events (create/start/join/quit)
 */
class MenuNavigator {
    // Build the tree
    private val root: MenuNode = MenuBuilder.createMainMenu()
    private val stack = ArrayDeque<MenuNode>()

    var current: MenuNode = root
        private set

    var selectedIndex: Int = 0
        private set

    var listener: MenuListener? = null

    // Public API used by UIRenderer / VexGame
    fun getMenuItems(): List<String> {
        // Show title + inputs inline
        return current.children.map { node ->
            if (node.isInput) {
                "${node.title}: ${node.inputBuffer}"
            } else {
                node.title
            }
        }
    }

    /**
     * Handle key presses.
     * Return true if the event was consumed by the menu (VexGame uses this).
     */
    fun handleKeyPress(key: Int, action: Int): Boolean {
        if (action != GLFW_PRESS && action != GLFW_REPEAT) return false

        // If current node has zero children, nothing to navigate
        if (current.children.isEmpty()) return false

        val selectedNode = current.children.getOrNull(selectedIndex)

        // Navigation keys (when not typing into input)
        when (key) {
            GLFW_KEY_UP -> {
                moveSelectionUp()
                return true
            }
            GLFW_KEY_DOWN -> {
                moveSelectionDown()
                return true
            }
            GLFW_KEY_LEFT, GLFW_KEY_ESCAPE -> {
                goBack()
                return true
            }
            GLFW_KEY_RIGHT, GLFW_KEY_ENTER -> {
                if (selectedNode != null) {
                    if (selectedNode.isInput) {
                        confirmInput(selectedNode)
                        return true
                    }
                    if (selectedNode.children.isNotEmpty()) {
                        enterSubmenu(selectedNode)
                        return true
                    }
                    selectedNode.action?.invoke()
                    handleLeafAction(selectedNode)
                    return true
                }
            }
            GLFW_KEY_BACKSPACE -> {
                if (selectedNode != null && selectedNode.isInput) {
                    if (selectedNode.inputBuffer.isNotEmpty()) {
                        selectedNode.inputBuffer.deleteCharAt(selectedNode.inputBuffer.length - 1)
                    }
                    return true
                }
            }
        }

        return false
    }

    /**
     * Handle character input (text nodes).
     */
    fun handleCharInput(ch: Char) {
        val node = current.children.getOrNull(selectedIndex) ?: return
        if (!node.isInput) return
        // Append printable characters (simple check)
        if (!ch.isISOControl()) {
            node.inputBuffer.append(ch)
        }
    }

    private fun moveSelectionUp() {
        if (current.children.isEmpty()) return
        var newIndex = selectedIndex - 1
        if (newIndex < 0) newIndex = current.children.size - 1
        // skip non-selectable nodes using explicit comparison to avoid unary '!' operator issues
        while (current.children[newIndex].selectable == false) {
            newIndex--
            if (newIndex < 0) newIndex = current.children.size - 1
        }
        selectedIndex = newIndex
    }

    private fun moveSelectionDown() {
        if (current.children.isEmpty()) return
        var newIndex = (selectedIndex + 1) % current.children.size
        while (current.children[newIndex].selectable == false) {
            newIndex = (newIndex + 1) % current.children.size
        }
        selectedIndex = newIndex
    }

    private fun enterSubmenu(node: MenuNode) {
        stack.addLast(current)
        current = node
        selectedIndex = 0
    }

    private fun goBack() {
        if (stack.isEmpty()) return
        current = stack.removeLast()
        selectedIndex = 0
    }

    private fun confirmInput(node: MenuNode) {
        when (node.id) {
            "world_name" -> {
                println("World name set: ${node.inputBuffer}")
            }
            "world_seed" -> {
                println("World seed set: ${node.inputBuffer}")
            }
            "add_server_ip" -> {
                println("Server IP: ${node.inputBuffer}")
            }
            "add_server_port" -> {
                println("Server Port: ${node.inputBuffer}")
            }
        }
    }

    private fun handleLeafAction(node: MenuNode) {
        when (node.id) {
            "start_singleplayer" -> {
                val worldCreateNode = findNodeInStackOrCurrent("world_create")
                val nameNode = worldCreateNode?.children?.find { it.id == "world_name" }
                val seedNode = worldCreateNode?.children?.find { it.id == "world_seed" }
                val worldName = nameNode?.inputBuffer?.toString() ?: "New World"
                val seedText = seedNode?.inputBuffer?.toString()
                val seed = seedText?.toLongOrNull() ?: (System.currentTimeMillis() and 0xffffffffL)
                listener?.onCreateSingleplayer(worldName, seed)
                listener?.onStartSingleplayer(worldName, seed)
            }
            "quit" -> {
                listener?.onQuit() ?: println("Quit requested (no listener set)")
            }
            "add_server_confirm" -> {
                val addServerNode = findNodeInStackOrCurrent("add_server")
                val ipNode = addServerNode?.children?.find { it.id == "add_server_ip" }
                val portNode = addServerNode?.children?.find { it.id == "add_server_port" }
                val ip = ipNode?.inputBuffer?.toString() ?: "127.0.0.1"
                val port = portNode?.inputBuffer?.toString()?.toIntOrNull() ?: 25565
                listener?.onJoinServer(ip, port)
            }
            else -> {
                // default: nothing
            }
        }
    }

    private fun findNodeInStackOrCurrent(id: String): MenuNode? {
        if (current.id == id) return current
        for (node in stack.reversed()) {
            if (node.id == id) return node
            val found = findInSubtree(node, id)
            if (found != null) return found
        }
        return findInSubtree(current, id)
    }

    private fun findInSubtree(node: MenuNode, id: String): MenuNode? {
        if (node.id == id) return node
        for (c in node.children) {
            val r = findInSubtree(c, id)
            if (r != null) return r
        }
        return null
    }

    /** Listener interface that VexGame should implement to act on selections */
    interface MenuListener {
        /** Called when user finalizes creation parameters for a singleplayer world. */
        fun onCreateSingleplayer(name: String, seed: Long) {}
        /** Called to start the singleplayer session (switch to playing state). */
        fun onStartSingleplayer(name: String, seed: Long) {}
        /** Called when user confirms joining a server. */
        fun onJoinServer(ip: String, port: Int) {}
        /** Called when the user chooses Quit. */
        fun onQuit() {}
    }
}
