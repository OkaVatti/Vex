package dev.vex.client.world

/**
 * Complete item registry based on itemList.md
 * Maps item IDs to their properties and texture atlas coordinates
 */
object Items {
    // Item ID counter
    private var nextId = 0
    private fun nextItemId() = nextId++

    // Item properties
    data class ItemProperties(
        val id: Int,
        val name: String,
        val textureX: Int,
        val textureY: Int,
        val stackSize: Int = 64,
        val category: String = "misc"
    )

    // Registry
    private val registry = mutableMapOf<Int, ItemProperties>()
    private val nameToId = mutableMapOf<String, Int>()

    // Natural items - Logs (row 0)
    val OAK_LOG = register("oak_log", 0, 0, 64, "natural")
    val BIRCH_LOG = register("birch_log", 1, 0, 64, "natural")
    val SPRUCE_LOG = register("spruce_log", 2, 0, 64, "natural")
    val MAPLE_LOG = register("maple_log", 3, 0, 64, "natural")
    val WILLOW_LOG = register("willow_log", 4, 0, 64, "natural")
    val RUBBER_LOG = register("rubber_log", 5, 0, 64, "natural")
    val APPLEWOOD_LOG = register("applewood_log", 6, 0, 64, "natural")
    val CHERRY_LOG = register("cherry_log", 7, 0, 64, "natural")
    val LEMONWOOD_LOG = register("lemonwood_log", 8, 0, 64, "natural")
    val LIMEWOOD_LOG = register("limewood_log", 9, 0, 64, "natural")
    val MANDARIN_LOG = register("mandarin_log", 10, 0, 64, "natural")

    // Ores (row 1-2)
    val COAL_ORE = register("coal_ore", 0, 1, 64, "ore")
    val COPPER_ORE = register("copper_ore", 1, 1, 64, "ore")
    val TIN_ORE = register("tin_ore", 2, 1, 64, "ore")
    val SILVER_ORE = register("silver_ore", 3, 1, 64, "ore")
    val GOLD_ORE = register("gold_ore", 4, 1, 64, "ore")
    val COBOLT_ORE = register("cobolt_ore", 5, 1, 64, "ore")
    val LITHIUM_ORE = register("lithium_ore", 6, 1, 64, "ore")
    val IRON_ORE = register("iron_ore", 7, 1, 64, "ore")
    val MAGNESIUM_ORE = register("magnesium_ore", 8, 1, 64, "ore")
    val PLATINUM_ORE = register("platinum_ore", 9, 1, 64, "ore")
    val TITANIUM_ORE = register("titanium_ore", 10, 1, 64, "ore")
    val MALACHITE_ORE = register("malachite_ore", 11, 1, 64, "ore")

    // Gems
    val SAPPHIRE_ORE = register("sapphire_ore", 0, 2, 64, "gem")
    val RUBY_ORE = register("ruby_ore", 1, 2, 64, "gem")
    val EMERALD_ORE = register("emerald_ore", 2, 2, 64, "gem")
    val DIAMOND_ORE = register("diamond_ore", 3, 2, 64, "gem")
    val BLACK_DIAMOND_ORE = register("black_diamond_ore", 4, 2, 64, "gem")
    val OPAL_ORE = register("opal_ore", 5, 2, 64, "gem")
    val OBSIDIAN = register("obsidian", 6, 2, 64, "gem")

    // Refined ores - Ingots (row 3)
    val COAL = register("coal", 0, 3, 64, "refined")
    val COPPER_INGOT = register("copper_ingot", 1, 3, 64, "refined")
    val TIN_INGOT = register("tin_ingot", 2, 3, 64, "refined")
    val SILVER_INGOT = register("silver_ingot", 3, 3, 64, "refined")
    val GOLD_INGOT = register("gold_ingot", 4, 3, 64, "refined")
    val COBOLT_DUST = register("cobolt_dust", 5, 3, 64, "refined")
    val LITHIUM_SALT = register("lithium_salt", 6, 3, 64, "refined")
    val IRON_INGOT = register("iron_ingot", 7, 3, 64, "refined")
    val MAGNESIUM_INGOT = register("magnesium_ingot", 8, 3, 64, "refined")
    val PLATINUM_INGOT = register("platinum_ingot", 9, 3, 64, "refined")
    val TITANIUM_INGOT = register("titanium_ingot", 10, 3, 64, "refined")
    val MALACHITE_INGOT = register("malachite_ingot", 11, 3, 64, "refined")

    // Refined gems (row 4)
    val SAPPHIRE = register("sapphire", 0, 4, 64, "refined")
    val RUBY = register("ruby", 1, 4, 64, "refined")
    val EMERALD = register("emerald", 2, 4, 64, "refined")
    val DIAMOND = register("diamond", 3, 4, 64, "refined")
    val BLACK_DIAMOND = register("black_diamond", 4, 4, 64, "refined")
    val OPAL = register("opal", 5, 4, 64, "refined")

    // Alloys (row 5)
    val BRONZE_INGOT = register("bronze_ingot", 0, 5, 64, "alloy")
    val ORICHALCUM_INGOT = register("orichalcum_ingot", 1, 5, 64, "alloy")
    val STEEL_INGOT = register("steel_ingot", 2, 5, 64, "alloy")
    val BLUE_STEEL_INGOT = register("blue_steel_ingot", 3, 5, 64, "alloy")
    val CHARCOAL = register("charcoal", 4, 5, 64, "refined")

    // Electronics - Cables (row 6)
    val COPPER_WIRE = register("copper_wire", 0, 6, 64, "electronics")
    val CABLE = register("cable", 1, 6, 64, "electronics")
    val THICK_CABLE = register("thick_cable", 2, 6, 64, "electronics")
    val COMMUNICATION_CABLE = register("communication_cable", 3, 6, 64, "electronics")
    val NETWORK_CABLE = register("network_cable", 4, 6, 64, "electronics")
    val FIBER_NETWORK_CABLE = register("fiber_network_cable", 5, 6, 64, "electronics")
    val UNIVERSAL_CABLE = register("universal_cable", 6, 6, 64, "electronics")
    val ADVANCED_UNIVERSAL_CABLE = register("advanced_universal_cable", 7, 6, 64, "electronics")

    // Electronics - PCBs (row 7)
    val BLANK_PCB = register("blank_pcb", 0, 7, 16, "electronics")
    val MINI_PCB = register("mini_pcb", 1, 7, 16, "electronics")
    val SMALL_PCB = register("small_pcb", 2, 7, 16, "electronics")
    val MEDIUM_PCB = register("medium_pcb", 3, 7, 16, "electronics")
    val LARGE_PCB = register("large_pcb", 4, 7, 16, "electronics")
    val XL_PCB = register("xl_pcb", 5, 7, 16, "electronics")

    // Electronics - Components (row 8)
    val RESISTOR = register("resistor", 0, 8, 64, "electronics")
    val TRANSISTOR = register("transistor", 1, 8, 64, "electronics")

    // Tools - Pickaxes (row 9)
    val WOOD_PICKAXE = register("wood_pickaxe", 0, 9, 1, "tool")
    val STONE_PICKAXE = register("stone_pickaxe", 1, 9, 1, "tool")
    val COPPER_PICKAXE = register("copper_pickaxe", 2, 9, 1, "tool")
    val IRON_PICKAXE = register("iron_pickaxe", 3, 9, 1, "tool")
    val STEEL_PICKAXE = register("steel_pickaxe", 4, 9, 1, "tool")
    val DIAMOND_PICKAXE = register("diamond_pickaxe", 5, 9, 1, "tool")

    // Tools - Axes (row 10)
    val WOOD_AXE = register("wood_axe", 0, 10, 1, "tool")
    val STONE_AXE = register("stone_axe", 1, 10, 1, "tool")
    val COPPER_AXE = register("copper_axe", 2, 10, 1, "tool")
    val IRON_AXE = register("iron_axe", 3, 10, 1, "tool")
    val STEEL_AXE = register("steel_axe", 4, 10, 1, "tool")
    val DIAMOND_AXE = register("diamond_axe", 5, 10, 1, "tool")

    // Tools - Shovels (row 11)
    val WOOD_SHOVEL = register("wood_shovel", 0, 11, 1, "tool")
    val STONE_SHOVEL = register("stone_shovel", 1, 11, 1, "tool")
    val COPPER_SHOVEL = register("copper_shovel", 2, 11, 1, "tool")
    val IRON_SHOVEL = register("iron_shovel", 3, 11, 1, "tool")
    val STEEL_SHOVEL = register("steel_shovel", 4, 11, 1, "tool")
    val DIAMOND_SHOVEL = register("diamond_shovel", 5, 11, 1, "tool")

    // Food - Fruits (row 12)
    val APPLE = register("apple", 0, 12, 16, "food")
    val CHERRY = register("cherry", 1, 12, 16, "food")
    val LEMON = register("lemon", 2, 12, 16, "food")
    val LIME = register("lime", 3, 12, 16, "food")
    val MANDARIN = register("mandarin", 4, 12, 16, "food")
    val BLACKBERRY = register("blackberry", 5, 12, 16, "food")
    val BLUEBERRY = register("blueberry", 6, 12, 16, "food")
    val RASPBERRY = register("raspberry", 7, 12, 16, "food")
    val STRAWBERRY = register("strawberry", 8, 12, 16, "food")
    val SWEETBERRY = register("sweetberry", 9, 12, 16, "food")

    // Food - Vegetables (row 13)
    val WHEAT = register("wheat", 0, 13, 64, "food")
    val CARROT = register("carrot", 1, 13, 64, "food")
    val POTATO = register("potato", 2, 13, 64, "food")
    val TOMATO = register("tomato", 3, 13, 64, "food")
    val ONION = register("onion", 4, 13, 64, "food")
    val GARLIC = register("garlic", 5, 13, 64, "food")
    val PUMPKIN = register("pumpkin", 6, 13, 64, "food")

    // Helper functions
    private fun register(
        name: String,
        textureX: Int,
        textureY: Int,
        stackSize: Int = 64,
        category: String = "misc"
    ): ItemProperties {
        val id = nextItemId()
        val props = ItemProperties(id, name, textureX, textureY, stackSize, category)
        registry[id] = props
        nameToId[name] = id
        return props
    }

    fun getById(id: Int): ItemProperties? = registry[id]
    fun getByName(name: String): ItemProperties? = nameToId[name]?.let { registry[it] }
    fun getAllItems(): Collection<ItemProperties> = registry.values
}