package dev.vex.client.world

/**
 * Complete block registry based on blockList.md
 * Maps block IDs to their properties and texture atlas coordinates
 */
object Blocks {
    // Atlas columns (used to compute texture index from textureX/textureY)
    private const val ATLAS_COLS = 16

    // Block ID counter
    private var nextId = 0
    private fun nextBlockId() = nextId++

    // Block properties
    data class BlockProperties(
        val id: Int,
        val name: String,
        val textureX: Int,
        val textureY: Int,
        val solid: Boolean = true,
        val transparent: Boolean = false,
        val luminance: Int = 0
    ) {
        /** Compute a single texture index from (x,y) in the atlas. */
        fun textureIndex(): Int = textureY * ATLAS_COLS + textureX
    }

    // Registry
    private val registry = mutableMapOf<Int, BlockProperties>()
    private val nameToId = mutableMapOf<String, Int>()

    // Natural blocks - Sky
    val AIR = register("air", 0, 0, solid = false, transparent = true)
    val CLOUDS = register("clouds", 1, 0, solid = false, transparent = true)
    val HEAVY_CLOUDS = register("heavy_clouds", 2, 0, solid = false, transparent = true)

    // Natural blocks - Ground
    val GRASS = register("grass", 0, 1)
    val DIRT = register("dirt", 1, 1)
    val SAND = register("sand", 2, 1)
    val SILT = register("silt", 3, 1)
    val GRAVEL = register("gravel", 4, 1)
    val CLAY = register("clay", 5, 1)
    val STONE = register("stone", 6, 1)
    val GRANITE = register("granite", 7, 1)
    // DIORITE added so WorldGenerator can reference it without missing-symbol errors.
    val DIORITE = register("diorite", 8, 1)
    val ANDESITE = register("andesite", 9, 1)
    val LIMESTONE = register("limestone", 10, 1)
    val SANDSTONE = register("sandstone", 11, 1)
    val SLATESTONE = register("slatestone", 12, 1)
    val PURESTONE = register("purestone", 13, 1)
    val LUMSTONE = register("lumstone", 14, 1, luminance = 10)

    // Bedrock
    val BEDROCK = register("bedrock", 0, 2)

    // Wood types - Logs (row 3)
    val OAK_LOG = register("oak_log", 0, 3)
    val BIRCH_LOG = register("birch_log", 1, 3)
    val SPRUCE_LOG = register("spruce_log", 2, 3)
    val MAPLE_LOG = register("maple_log", 3, 3)
    val WILLOW_LOG = register("willow_log", 4, 3)
    val RUBBER_LOG = register("rubber_log", 5, 3)
    val APPLEWOOD_LOG = register("applewood_log", 6, 3)
    val CHERRY_LOG = register("cherry_log", 7, 3)
    val LEMONWOOD_LOG = register("lemonwood_log", 8, 3)
    val LIMEWOOD_LOG = register("limewood_log", 9, 3)
    val MANDARIN_LOG = register("mandarin_log", 10, 3)

    // Wood types - Planks (row 4)
    val OAK_PLANKS = register("oak_planks", 0, 4)
    val BIRCH_PLANKS = register("birch_planks", 1, 4)
    val SPRUCE_PLANKS = register("spruce_planks", 2, 4)
    val MAPLE_PLANKS = register("maple_planks", 3, 4)
    val WILLOW_PLANKS = register("willow_planks", 4, 4)
    val RUBBER_PLANKS = register("rubber_planks", 5, 4)
    val APPLEWOOD_PLANKS = register("applewood_planks", 6, 4)
    val CHERRY_PLANKS = register("cherry_planks", 7, 4)
    val LEMONWOOD_PLANKS = register("lemonwood_planks", 8, 4)
    val LIMEWOOD_PLANKS = register("limewood_planks", 9, 4)
    val MANDARIN_PLANKS = register("mandarin_planks", 10, 4)

    // Leaves (row 5)
    val OAK_LEAVES = register("oak_leaves", 0, 5, transparent = true)
    val BIRCH_LEAVES = register("birch_leaves", 1, 5, transparent = true)
    val SPRUCE_LEAVES = register("spruce_leaves", 2, 5, transparent = true)
    val MAPLE_LEAVES = register("maple_leaves", 3, 5, transparent = true)
    val WILLOW_LEAVES = register("willow_leaves", 4, 5, transparent = true)

    // Flowers (row 6-7)
    val CRIMSON_ROSALIA = register("crimson_rosalia", 0, 6, solid = false)
    val ORANGE_TULIP = register("orange_tulip", 1, 6, solid = false)
    val DANDELION = register("dandelion", 2, 6, solid = false)
    val LINDEN_FLOWER = register("linden_flower", 3, 6, solid = false)
    val CACTUS = register("cactus", 4, 6)
    val LUMI_LILY = register("lumi_lily", 5, 6, solid = false, luminance = 7)
    val AETHAE_HYDRANGEA = register("aethae_hydrangea", 6, 6, solid = false)
    val LOBELIA_AZURAE = register("lobelia_azurae", 7, 6, solid = false)
    val AZURE_ORCHIDAE = register("azure_orchidae", 8, 6, solid = false)
    val CORNFLOWER = register("cornflower", 9, 6, solid = false)
    val INDIGO_ROSALIA = register("indigo_rosalia", 10, 6, solid = false)
    val PURPLE_TULIP = register("purple_tulip", 11, 6, solid = false)
    val PETUNIAS = register("petunias", 12, 6, solid = false)
    val PINK_HIBISCUS = register("pink_hibiscus", 13, 6, solid = false)
    val ROSALIA_DE_LA_MUERTE = register("rosalia_de_la_muerte", 14, 6, solid = false)

    // Tall grass / vegetation
    val TALL_GRASS = register("tall_grass", 0, 7, solid = false, transparent = true)

    // Ores (row 8-10)
    val COAL_ORE = register("coal_ore", 0, 8)
    val COPPER_ORE = register("copper_ore", 1, 8)
    val TIN_ORE = register("tin_ore", 2, 8)
    val SILVER_ORE = register("silver_ore", 3, 8)
    val GOLD_ORE = register("gold_ore", 4, 8)
    val COBOLT_ORE = register("cobolt_ore", 5, 8)
    val LITHIUM_ORE = register("lithium_ore", 6, 8)
    val IRON_ORE = register("iron_ore", 7, 8)
    val PLATINUM_ORE = register("platinum_ore", 8, 8)
    val TITANIUM_ORE = register("titanium_ore", 9, 8)
    val MALACHITE_ORE = register("malachite_ore", 10, 8)

    // Gems
    val DIAMOND_ORE = register("diamond_ore", 0, 9)
    val EMERALD_ORE = register("emerald_ore", 1, 9)
    val RUBY_ORE = register("ruby_ore", 2, 9)
    val SAPPHIRE_ORE = register("sapphire_ore", 3, 9)
    val AMETHYST_ORE = register("amethyst_ore", 4, 9)
    val OPAL_ORE = register("opal_ore", 5, 9)

    // Functional blocks (row 11-12)
    val WORKBENCH = register("workbench", 0, 11)
    val KILN = register("kiln", 1, 11)
    val CHEST = register("chest", 2, 11)

    // Glass
    val GLASS = register("glass", 0, 12, transparent = true)
    val WATER = register("water", 1, 12, solid = false, transparent = true)

    // Helper functions
    private fun register(
        name: String,
        textureX: Int,
        textureY: Int,
        solid: Boolean = true,
        transparent: Boolean = false,
        luminance: Int = 0
    ): BlockProperties {
        val id = nextBlockId()
        val props = BlockProperties(id, name, textureX, textureY, solid, transparent, luminance)
        registry[id] = props
        nameToId[name] = id
        return props
    }

    /**
     * Return a non-null BlockProperties for id. If id unknown, return AIR.
     * This avoids nullable callers and simplifies rendering logic.
     */
    fun getById(id: Int): BlockProperties {
        return registry[id] ?: registry[AIR.id]!!
    }

    /**
     * Return a non-null BlockProperties by name (fallback to AIR).
     */
    fun getByName(name: String): BlockProperties {
        val id = nameToId[name]
        return if (id != null) registry[id]!! else registry[AIR.id]!!
    }

    fun getAllBlocks(): Collection<BlockProperties> = registry.values
}
