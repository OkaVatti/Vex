package dev.vex.client.world

/**
 * Complete block registry matching design document.
 * Each block has ID, name, solid flag, and texture indices for each face.
 */
data class Block(
    val id: Int,
    val name: String,
    val solid: Int,
    val topTexture: Int,
    val bottomTexture: Int = topTexture,
    val sideTexture: Int = topTexture,
    val isOpaque: Boolean = true,
    val isTransparent: Boolean = false
)

object Blocks {
    val AIR = Block(0, "Air", 0, 0, isOpaque = false)
    val GRASS = Block(1, "Grass", 1, 0, 2, 3)
    val DIRT = Block(2, "Dirt", 1, 2)
    val SAND = Block(3, "Sand", 1, 18)
    val SILT = Block(4, "Silt", 1, 19)
    val CLAY = Block(5, "Clay", 1, 68)
    val STONE = Block(6, "Stone", 1, 1)
    val COBBLE = Block(7, "Cobble", 1, 16)
    val GRANITE = Block(8, "Granite", 1, 32)
    val DIORITE = Block(9, "Diorite", 1, 33)
    val ANDESITE = Block(10, "Andesite", 1, 34)
    val LIMESTONE = Block(11, "Limestone", 1, 35)
    val SANDSTONE = Block(12, "Sandstone", 1, 36, 37, 38)
    val SLATESTONE = Block(13, "Slatestone", 1, 39)
    val PURESTONE = Block(14, "Purestone", 1, 40)
    val BEDROCK = Block(15, "Bedrock", 1, 17)

    // Ores
    val COAL_ORE = Block(16, "Coal Ore", 1, 48)
    val COPPER_ORE = Block(17, "Copper Ore", 1, 49)
    val TIN_ORE = Block(18, "Tin Ore", 1, 50)
    val SILVER_ORE = Block(19, "Silver Ore", 1, 51)
    val GOLD_ORE = Block(20, "Gold Ore", 1, 52)
    val COBOLT_ORE = Block(21, "Cobolt Ore", 1, 53)
    val LITHIUM_ORE = Block(22, "Lithium Ore", 1, 54)
    val IRON_ORE = Block(23, "Iron Ore", 1, 55)
    val PLATINUM_ORE = Block(24, "Platinum Ore", 1, 56)
    val TITANIUM_ORE = Block(25, "Titanium Ore", 1, 57)
    val MALACHITE_ORE = Block(26, "Malachite Ore", 1, 58)
    val OPAL_ORE = Block(27, "Opal Ore", 1, 59)
    val FIRE_OPAL_ORE = Block(28, "Fire Opal Ore", 1, 60)
    val MOONSTONE_ORE = Block(29, "Moonstone Ore", 1, 61)
    val SAPPHIRE_ORE = Block(30, "Sapphire Ore", 1, 62)
    val RUBY_ORE = Block(31, "Ruby Ore", 1, 63)
    val HEART_RUBY_ORE = Block(32, "Heart Ruby Ore", 1, 64)
    val AMETHYST_ORE = Block(33, "Amethyst Ore", 1, 65)
    val EMERALD_ORE = Block(34, "Emerald Ore", 1, 66)
    val DIAMOND_ORE = Block(35, "Diamond Ore", 1, 67)
    val BLACK_DIAMOND_ORE = Block(36, "Black Diamond Ore", 1, 68)
    val QUARTZ_ORE = Block(37, "Quartz Ore", 1, 69)
    val ROSE_QUARTZ_ORE = Block(38, "Rose Quartz Ore", 1, 70)
    val BLACK_QUARTZ_ORE = Block(39, "Black Quartz Ore", 1, 71)
    val ANTHRACITE_ORE = Block(40, "Anthracite Ore", 1, 72)
    val MAGNESITE_ORE = Block(41, "Magnesite Ore", 1, 73)
    val LUMINUM_ORE = Block(42, "Luminum Ore", 1, 74)

    // Wood logs
    val OAK_LOG = Block(43, "Oak Log", 1, 80, 81, 81)
    val BIRCH_LOG = Block(44, "Birch Log", 1, 82, 83, 83)
    val SPRUCE_LOG = Block(45, "Spruce Log", 1, 84, 85, 85)
    val PINE_LOG = Block(46, "Pine Log", 1, 86, 87, 87)
    val WILLOW_LOG = Block(47, "Willow Log", 1, 88, 89, 89)
    val MAPLE_LOG = Block(48, "Maple Log", 1, 90, 91, 91)
    val RUBBER_LOG = Block(49, "Rubber Log", 1, 92, 93, 93)

    // Planks
    val OAK_PLANKS = Block(56, "Oak Planks", 1, 96)
    val BIRCH_PLANKS = Block(57, "Birch Planks", 1, 97)
    val SPRUCE_PLANKS = Block(58, "Spruce Planks", 1, 98)
    val PINE_PLANKS = Block(59, "Pine Planks", 1, 99)
    val WILLOW_PLANKS = Block(60, "Willow Planks", 1, 100)
    val MAPLE_PLANKS = Block(61, "Maple Planks", 1, 101)
    val RUBBER_PLANKS = Block(62, "Rubber Planks", 1, 102)

    // Leaves (semi-transparent)
    val OAK_LEAVES = Block(69, "Oak Leaves", 1, 112, isOpaque = false, isTransparent = true)
    val BIRCH_LEAVES = Block(70, "Birch Leaves", 1, 113, isOpaque = false, isTransparent = true)
    val SPRUCE_LEAVES = Block(71, "Spruce Leaves", 1, 114, isOpaque = false, isTransparent = true)
    val PINE_LEAVES = Block(72, "Pine Leaves", 1, 115, isOpaque = false, isTransparent = true)
    val WILLOW_LEAVES = Block(73, "Willow Leaves", 1, 116, isOpaque = false, isTransparent = true)
    val MAPLE_LEAVES = Block(74, "Maple Leaves", 1, 117, isOpaque = false, isTransparent = true)
    val RUBBER_LEAVES = Block(75, "Rubber Leaves", 1, 118, isOpaque = false, isTransparent = true)

    // Glass (transparent)
    val GLASS = Block(203, "Glass", 1, 144, isOpaque = false, isTransparent = true)

    // Water (transparent, non-solid)
    val WATER = Block(278, "Water", 0, 160, isOpaque = false, isTransparent = true)

    // Block lookup by ID
    private val blockRegistry = mutableMapOf<Int, Block>()

    init {
        // Register all blocks
        for (field in Blocks::class.java.declaredFields) {
            if (field.type == Block::class.java) {
                field.isAccessible = true
                val block = field.get(null) as Block
                blockRegistry[block.id] = block
            }
        }
    }

    fun getById(id: Int): Block = blockRegistry[id] ?: AIR
}