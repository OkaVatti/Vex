package dev.vex.client.world

/**
 * Defines the properties of a single block type in the world.
 *
 * @property id The unique numeric ID for the block.
 * @property name The human-readable name of the block.
 * @property solid If true, the block is considered solid for physics and collision.
 * @property topTexture The texture atlas index for the top face of the block.
 * @property bottomTexture The texture atlas index for the bottom face. Defaults to topTexture.
 * @property sideTexture The texture atlas index for all vertical faces (N, S, E, W). Defaults to topTexture.
 * @property isOpaque If true, the block fully blocks vision. The renderer will not render faces behind it (face culling).
 * @property isTransparent If true, the block requires alpha blending (e.g., water, glass). Often used for blocks where `isOpaque` is false.
 */
data class Block(
    val id: Int,
    val name: String,
    val solid: Boolean,
    val topTexture: Int,
    val bottomTexture: Int = topTexture,
    val sideTexture: Int = topTexture,
    val isOpaque: Boolean = true,
    val isTransparent: Boolean = false
)

/**
 * A static registry of all blocks available in the game.
 */
object Blocks {
    // Basic Terrain
    val AIR = Block(0, "Air", solid = false, -1, isOpaque = false) // -1 texture index for no texture
    val GRASS = Block(1, "Grass", solid = true, 0, bottomTexture = 2, sideTexture = 3)
    val DIRT = Block(2, "Dirt", solid = true, 2)
    val SAND = Block(3, "Sand", solid = true, 18)
    val SILT = Block(4, "Silt", solid = true, 19)
    val CLAY = Block(5, "Clay", solid = true, 68)
    val STONE = Block(6, "Stone", solid = true, 1)
    val COBBLE = Block(7, "Cobblestone", solid = true, 16)
    val GRANITE = Block(8, "Granite", solid = true, 32)
    val DIORITE = Block(9, "Diorite", solid = true, 33)
    val ANDESITE = Block(10, "Andesite", solid = true, 34)
    val LIMESTONE = Block(11, "Limestone", solid = true, 35)
    val SANDSTONE = Block(12, "Sandstone", solid = true, 36, bottomTexture = 37, sideTexture = 38)
    val SLATESTONE = Block(13, "Slatestone", solid = true, 39)
    val PURESTONE = Block(14, "Purestone", solid = true, 40)
    val BEDROCK = Block(15, "Bedrock", solid = true, 17)

    // Ores
    val COAL_ORE = Block(16, "Coal Ore", solid = true, 48)
    val COPPER_ORE = Block(17, "Copper Ore", solid = true, 49)
    val TIN_ORE = Block(18, "Tin Ore", solid = true, 50)
    val SILVER_ORE = Block(19, "Silver Ore", solid = true, 51)
    val GOLD_ORE = Block(20, "Gold Ore", solid = true, 52)
    val COBOLT_ORE = Block(21, "Cobalt Ore", solid = true, 53)
    val LITHIUM_ORE = Block(22, "Lithium Ore", solid = true, 54)
    val IRON_ORE = Block(23, "Iron Ore", solid = true, 55)
    val PLATINUM_ORE = Block(24, "Platinum Ore", solid = true, 56)
    val TITANIUM_ORE = Block(25, "Titanium Ore", solid = true, 57)
    val MALACHITE_ORE = Block(26, "Malachite Ore", solid = true, 58)
    val OPAL_ORE = Block(27, "Opal Ore", solid = true, 59)
    val FIRE_OPAL_ORE = Block(28, "Fire Opal Ore", solid = true, 60)
    val MOONSTONE_ORE = Block(29, "Moonstone Ore", solid = true, 61)
    val SAPPHIRE_ORE = Block(30, "Sapphire Ore", solid = true, 62)
    val RUBY_ORE = Block(31, "Ruby Ore", solid = true, 63)
    val HEART_RUBY_ORE = Block(32, "Heart Ruby Ore", solid = true, 64)
    val AMETHYST_ORE = Block(33, "Amethyst Ore", solid = true, 65)
    val EMERALD_ORE = Block(34, "Emerald Ore", solid = true, 66)
    val DIAMOND_ORE = Block(35, "Diamond Ore", solid = true, 67)
    val BLACK_DIAMOND_ORE = Block(36, "Black Diamond Ore", solid = true, 68)
    val QUARTZ_ORE = Block(37, "Quartz Ore", solid = true, 69)
    val ROSE_QUARTZ_ORE = Block(38, "Rose Quartz Ore", solid = true, 70)
    val BLACK_QUARTZ_ORE = Block(39, "Black Quartz Ore", solid = true, 71)
    val ANTHRACITE_ORE = Block(40, "Anthracite Ore", solid = true, 72)
    val MAGNESITE_ORE = Block(41, "Magnesite Ore", solid = true, 73)
    val LUMINUM_ORE = Block(42, "Luminum Ore", solid = true, 74)

    // Wood logs
    val OAK_LOG = Block(43, "Oak Log", solid = true, 80, bottomTexture = 80, sideTexture = 81)
    val BIRCH_LOG = Block(44, "Birch Log", solid = true, 82, bottomTexture = 82, sideTexture = 83)
    val SPRUCE_LOG = Block(45, "Spruce Log", solid = true, 84, bottomTexture = 84, sideTexture = 85)
    val PINE_LOG = Block(46, "Pine Log", solid = true, 86, bottomTexture = 86, sideTexture = 87)
    val WILLOW_LOG = Block(47, "Willow Log", solid = true, 88, bottomTexture = 88, sideTexture = 89)
    val MAPLE_LOG = Block(48, "Maple Log", solid = true, 90, bottomTexture = 90, sideTexture = 91)
    val RUBBER_LOG = Block(49, "Rubber Log", solid = true, 92, bottomTexture = 92, sideTexture = 93)

    // Planks
    val OAK_PLANKS = Block(56, "Oak Planks", solid = true, 96)
    val BIRCH_PLANKS = Block(57, "Birch Planks", solid = true, 97)
    val SPRUCE_PLANKS = Block(58, "Spruce Planks", solid = true, 98)
    val PINE_PLANKS = Block(59, "Pine Planks", solid = true, 99)
    val WILLOW_PLANKS = Block(60, "Willow Planks", solid = true, 100)
    val MAPLE_PLANKS = Block(61, "Maple Planks", solid = true, 101)
    val RUBBER_PLANKS = Block(62, "Rubber Planks", solid = true, 102)

    // Leaves
    val OAK_LEAVES = Block(69, "Oak Leaves", solid = true, 112, isOpaque = false, isTransparent = true)
    val BIRCH_LEAVES = Block(70, "Birch Leaves", solid = true, 113, isOpaque = false, isTransparent = true)
    val SPRUCE_LEAVES = Block(71, "Spruce Leaves", solid = true, 114, isOpaque = false, isTransparent = true)
    val PINE_LEAVES = Block(72, "Pine Leaves", solid = true, 115, isOpaque = false, isTransparent = true)
    val WILLOW_LEAVES = Block(73, "Willow Leaves", solid = true, 116, isOpaque = false, isTransparent = true)
    val MAPLE_LEAVES = Block(74, "Maple Leaves", solid = true, 117, isOpaque = false, isTransparent = true)
    val RUBBER_LEAVES = Block(75, "Rubber Leaves", solid = true, 118, isOpaque = false, isTransparent = true)

    // Flora
    val TALL_GRASS = Block(76, "Tall Grass", solid = false, 119, isOpaque = false)

    // Misc
    val GLASS = Block(203, "Glass", solid = true, 144, isOpaque = false, isTransparent = true)
    val WATER = Block(278, "Water", solid = false, 160, isOpaque = false, isTransparent = true)

    // --- Block Registry ---

    // A list of all block instances for easy registration.
    private val allBlocks = listOf(
        AIR, GRASS, DIRT, SAND, SILT, CLAY, STONE, COBBLE, GRANITE, DIORITE, ANDESITE, LIMESTONE,
        SANDSTONE, SLATESTONE, PURESTONE, BEDROCK, COAL_ORE, COPPER_ORE, TIN_ORE, SILVER_ORE,
        GOLD_ORE, COBOLT_ORE, LITHIUM_ORE, IRON_ORE, PLATINUM_ORE, TITANIUM_ORE, MALACHITE_ORE,
        OPAL_ORE, FIRE_OPAL_ORE, MOONSTONE_ORE, SAPPHIRE_ORE, RUBY_ORE, HEART_RUBY_ORE,
        AMETHYST_ORE, EMERALD_ORE, DIAMOND_ORE, BLACK_DIAMOND_ORE, QUARTZ_ORE, ROSE_QUARTZ_ORE,
        BLACK_QUARTZ_ORE, ANTHRACITE_ORE, MAGNESITE_ORE, LUMINUM_ORE, OAK_LOG, BIRCH_LOG,
        SPRUCE_LOG, PINE_LOG, WILLOW_LOG, MAPLE_LOG, RUBBER_LOG, OAK_PLANKS, BIRCH_PLANKS,
        SPRUCE_PLANKS, PINE_PLANKS, WILLOW_PLANKS, MAPLE_PLANKS, RUBBER_PLANKS, OAK_LEAVES,
        BIRCH_LEAVES, SPRUCE_LEAVES, PINE_LEAVES, WILLOW_LEAVES, MAPLE_LEAVES, RUBBER_LEAVES,
        TALL_GRASS, GLASS, WATER
    )

    // The registry map, built from the list for fast lookups.
    private val blockRegistry: Map<Int, Block> = allBlocks.associateBy { it.id }

    /**
     * Retrieves a block by its numeric ID.
     * @param id The ID of the block to retrieve.
     * @return The corresponding [Block] instance, or [AIR] if the ID is not found.
     */
    fun getById(id: Int): Block = blockRegistry[id] ?: AIR
}