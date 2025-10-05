package dev.vex.client.menu

import java.util.Locale
import java.util.Locale.getDefault

/**
 * Menu node model used by MenuNavigator.
 *
 * - `title` is what is displayed.
 * - `children` are submenu entries (if any).
 * - `action` is run when the node is activated (Enter on leaf).
 * - `isInput` indicates a text input field (name/seed) that accepts characters.
 * - `selectable` toggles whether the node can be focused (useful for headers).
 */
data class MenuNode(
    val id: String,
    val title: String,
    val children: MutableList<MenuNode> = mutableListOf(),
    var action: (() -> Unit)? = null,
    var isInput: Boolean = false,
    var inputBuffer: StringBuilder = StringBuilder(),
    var selectable: Boolean = true
)

/**
 * Small builder that creates the full menu tree for the main menu and submenus.
 * The tree intentionally contains placeholders for lists (worlds, servers, texture packs, mods, features).
 *
 * The actual logic for creating worlds, joining servers, executing game, etc. is
 * performed through callbacks attached by MenuNavigator's listener.
 */
object MenuBuilder {
    fun createMainMenu(): MenuNode {
        val root = MenuNode("root", "Main Menu")

        // --- Singleplayer ---
        val singleplayer = MenuNode("singleplayer", "Singleplayer")
        val worldList = MenuNode("world_list", "World List") // placeholder to be filled by the game
        val worldSelection = MenuNode("world_select", "World Selection") // placeholder
        val worldCreation = MenuNode("world_create", "World Creation")
        val worldNameInput = MenuNode("world_name", "World Name", isInput = true)
        val worldSeedInput = MenuNode("world_seed", "World Seed", isInput = true)
        val options = MenuNode("world_options", "Options")
        val cheatToggle = MenuNode("toggle_cheats", "Enable Cheats") // toggled by action
        val featureSelector = MenuNode("feature_selector", "Enable/Disable Features")
        // Placeholder features list
        val featureList = MenuNode("feature_list", "Feature List")
        val featureA = MenuNode("feature_a", "Infinite Day/Night")
        val featureB = MenuNode("feature_b", "No Fall Damage")
        // set actions explicitly
        featureA.action = { println("Toggled feature: ${featureA.title}") }
        featureB.action = { println("Toggled feature: ${featureB.title}") }
        featureList.children.addAll(listOf(featureA, featureB))

        val useScripts = MenuNode("use_scripts", "Use Scripts")
        val dragDropScripts = MenuNode("script_drag_drop", "Drag & Drop scripts (.vex/.vexscript/.kts)")
        dragDropScripts.selectable = false

        // Add structure
        useScripts.children.add(dragDropScripts)
        featureSelector.children.add(featureList)
        options.children.addAll(listOf(cheatToggle, featureSelector, useScripts))
        worldCreation.children.addAll(listOf(worldNameInput, worldSeedInput, options))
        singleplayer.children.addAll(listOf(worldList, worldSelection, worldCreation))

        // Generation + play
        val worldGeneration = MenuNode("world_generation", "World Generation")
        val playNow = MenuNode("start_singleplayer", "Play (Start Selected / New World)")
        singleplayer.children.addAll(listOf(worldGeneration, playNow))

        // --- Multiplayer ---
        val multiplayer = MenuNode("multiplayer", "Multiplayer (P2P)")
        val serverList = MenuNode("server_list", "Server List")
        val serverSelection = MenuNode("server_select", "Server Selection")
        val serverCreation = MenuNode("server_create", "Server Creation")
        val serverFeatureSelector = MenuNode("server_feature_selector", "Server Feature Selector")
        val serverWorldGeneration = MenuNode("server_world_gen", "Server World Generation")
        val adminConsole = MenuNode("admin_console", "Admin Console")
        serverCreation.children.addAll(listOf(serverFeatureSelector, serverWorldGeneration, adminConsole))
        val joinServer = MenuNode("join_server", "Join Server")
        val addServer = MenuNode("add_server", "Add New Server")
        val addServerIP = MenuNode("add_server_ip", "Server IP", isInput = true)
        val addServerPort = MenuNode("add_server_port", "Server Port", isInput = true)
        val addServerConfirm = MenuNode("add_server_confirm", "Join Server")
        addServerConfirm.action = { println("Joining server... (placeholder action)") }
        addServer.children.addAll(listOf(addServerIP, addServerPort, addServerConfirm))
        multiplayer.children.addAll(listOf(serverList, serverSelection, serverCreation, joinServer, addServer))

        // --- LAN ---
        val lan = MenuNode("lan", "LAN Co-op")
        val lanList = MenuNode("lan_list", "LAN World List")
        val lanSelection = MenuNode("lan_select", "LAN World Selection")
        val joinLan = MenuNode("join_lan", "Join LAN World")
        lan.children.addAll(listOf(lanList, lanSelection, joinLan))

        // --- Settings ---
        val settings = MenuNode("settings", "Settings")
        val controls = MenuNode("controls", "Controls")
        val controlNames = listOf(
            "Walk Forwards", "Strafe Left", "Strafe Right", "Walk Backwards", "Lunge",
            "Run", "Crawl", "Inventory", "Interact", "Punch", "Drop", "Camera Toggle"
        )
        controls.children.addAll(controlNames.map { MenuNode("control_${it.lowercase(getDefault()).replace(" ", "_")}", it) })
        val texturePacks = MenuNode("texture_packs", "Texture Packs")
        val textureList = MenuNode("texture_list", "Texture Pack List")
        val mods = MenuNode("mods", "Mods")
        val modList = MenuNode("mod_list", "Mod List")
        texturePacks.children.add(textureList)
        mods.children.add(modList)
        settings.children.addAll(listOf(controls, texturePacks, mods))

        // --- Quit ---
        val quit = MenuNode("quit", "Quit Game")
        quit.action = { println("QUIT selected") } // actual quit triggered via MenuNavigator listener

        // Assemble root
        root.children.addAll(listOf(singleplayer, multiplayer, lan, settings, quit))
        return root
    }
}
