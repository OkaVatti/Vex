package dev.vex.client.world

import dev.vex.client.world.Blocks
import kotlin.math.*
import kotlin.random.Random

/**
 * Generates Beta 1.7.3 style terrain with modern improvements.
 */
class WorldGenerator(private val seed: Long = Random.nextLong()) {
    private val random = Random(seed)

    // Noise generators
    private val terrainNoise = PerlinNoise(seed)
    private val detailNoise = PerlinNoise(seed + 1)
    private val caveNoise = PerlinNoise(seed + 2)
    private val biomeTemperatureNoise = PerlinNoise(seed + 3)
    private val biomeHumidityNoise = PerlinNoise(seed + 4)

    fun generateChunk(chunk: Chunk) {
        // Phase 1: Determine biomes
        val biomeMap = generateBiomeMap(chunk)

        // Phase 2: Generate base terrain
        generateTerrain(chunk, biomeMap)

        // Phase 3: Generate caves
        generateCaves(chunk)

        // Phase 4: Place ores
        placeOres(chunk)

        // Phase 5: Surface decoration (trees, grass, etc.)
        decorateSurface(chunk, biomeMap)

        chunk.state = Chunk.ChunkState.READY
    }

    private fun generateBiomeMap(chunk: Chunk): Array<Array<Biome>> {
        val biomes = Array(16) { Array(16) { Biome.PLAINS } }

        for (x in 0 until 16) {
            for (z in 0 until 16) {
                val worldX = chunk.x * 16 + x
                val worldZ = chunk.z * 16 + z

                val temperature = biomeTemperatureNoise.noise(
                    worldX * 0.0012,
                    worldZ * 0.0012
                )
                val humidity = biomeHumidityNoise.noise(
                    worldX * 0.0014,
                    worldZ * 0.0014
                )

                biomes[x][z] = determineBiome(temperature, humidity)
            }
        }

        return biomes
    }

    private fun determineBiome(temperature: Double, humidity: Double): Biome {
        return when {
            temperature < -0.3 -> Biome.TUNDRA
            temperature < 0.0 -> Biome.TAIGA
            temperature < 0.4 -> if (humidity > 0.5) Biome.SWAMP else Biome.PLAINS
            temperature < 0.7 -> Biome.FOREST
            temperature < 0.9 -> Biome.JUNGLE
            else -> Biome.DESERT
        }
    }

    private fun generateTerrain(chunk: Chunk, biomeMap: Array<Array<Biome>>) {
        for (x in 0 until 16) {
            for (z in 0 until 16) {
                val worldX = chunk.x * 16 + x
                val worldZ = chunk.z * 16 + z

                // Multi-octave noise for natural terrain
                val baseNoise = terrainNoise.fbm(worldX * 0.0025, worldZ * 0.0025, 4)
                val detailNoiseVal = this.detailNoise.fbm(worldX * 0.01, worldZ * 0.01, 3)

                val biome = biomeMap[x][z]
                val heightModifier = biome.heightModifier

                val baseHeight = 64 + (baseNoise * 32 * heightModifier).toInt()
                val height = baseHeight + (detailNoiseVal * 8).toInt()

                // Place bedrock layer
                for (y in -128..-122) {
                    if (y == -128 || random.nextFloat() < 0.9f) {
                        chunk.setBlock(x, y, z, Blocks.BEDROCK.id)
                    }
                }

                // Generate stone, dirt, and surface layers
                for (y in -122 until height) {
                    when {
                        y < height - 5 -> {
                            // Stone layers with geological variation
                            val stoneType = when {
                                y < -96 -> Blocks.SLATESTONE.id
                                y < -48 -> Blocks.STONE.id
                                random.nextFloat() < 0.05 -> {
                                    when (random.nextInt(3)) {
                                        0 -> Blocks.GRANITE.id
                                        1 -> Blocks.DIORITE.id
                                        else -> Blocks.ANDESITE.id
                                    }
                                }
                                else -> Blocks.STONE.id
                            }
                            chunk.setBlock(x, y, z, stoneType)
                        }
                        y < height - 1 -> {
                            // Dirt layer
                            chunk.setBlock(x, y, z, Blocks.DIRT.id)
                        }
                        else -> {
                            // Surface block based on biome
                            val surfaceBlock = biome.surfaceBlock
                            chunk.setBlock(x, y, z, surfaceBlock)
                        }
                    }
                }

                // Handle water for oceans/lakes
                if (height < 63) {
                    for (y in height until 63) {
                        chunk.setBlock(x, y, z, Blocks.WATER.id)
                    }
                    if (biome.surfaceBlock == Blocks.GRASS.id) {
                        chunk.setBlock(x, height-1, z, Blocks.DIRT.id)
                    }
                }
            }
        }
    }

    private fun generateCaves(chunk: Chunk) {
        for (x in 0 until 16) {
            for (z in 0 until 16) {
                for (y in -120 until 128) {
                    val worldX = chunk.x * 16 + x
                    val worldZ = chunk.z * 16 + z

                    // 3D Perlin worms for caves
                    val caveValue = caveNoise.noise3D(
                        worldX * 0.04,
                        y.toDouble() * 0.04,
                        worldZ * 0.04
                    )

                    // Threshold determines cave density
                    val threshold = 0.6 + (y.toDouble() / 256.0) * 0.1

                    if (caveValue > threshold && chunk.getBlock(x, y, z) != Blocks.BEDROCK.id) {
                        chunk.setBlock(x, y, z, Blocks.AIR.id)
                    }
                }
            }
        }
    }

    private fun placeOres(chunk: Chunk) {
        placeOreVein(chunk, Blocks.COAL_ORE.id, -64, 128, 32, 16, 20)
        placeOreVein(chunk, Blocks.COPPER_ORE.id, -48, 96, 48, 12, 15)
        placeOreVein(chunk, Blocks.TIN_ORE.id, -48, 64, 16, 8, 12)
        placeOreVein(chunk, Blocks.IRON_ORE.id, -64, 64, 0, 12, 18)
        placeOreVein(chunk, Blocks.GOLD_ORE.id, -96, 32, -32, 8, 6)
        placeOreVein(chunk, Blocks.DIAMOND_ORE.id, -128, -48, -80, 4, 3)
        placeOreVein(chunk, Blocks.EMERALD_ORE.id, -32, 64, 16, 3, 2)
        placeOreVein(chunk, Blocks.COBOLT_ORE.id, -80, 0, -40, 6, 8)
    }

    private fun placeOreVein(
        chunk: Chunk,
        oreId: Int,
        minY: Int,
        maxY: Int,
        peakY: Int,
        veinSize: Int,
        attempts: Int
    ) {
        for (i in 0 until attempts) {
            val x = random.nextInt(16)
            val z = random.nextInt(16)

            // Ore distribution peaks at peakY
            val yRange = maxY - minY
            val normalizedPeak = (peakY - minY).toDouble() / yRange
            val r = random.nextDouble()
            val y = minY + (yRange * (normalizedPeak + (r - 0.5) * 0.4)).toInt()

            if (y !in minY..maxY) continue

            // Place vein
            val veinRadius = sqrt(veinSize.toDouble()).toInt()
            for (dx in -veinRadius..veinRadius) {
                for (dy in -veinRadius..veinRadius) {
                    for (dz in -veinRadius..veinRadius) {
                        if (dx * dx + dy * dy + dz * dz > veinSize) continue

                        val bx = x + dx
                        val by = y + dy
                        val bz = z + dz

                        if (bx in 0 until 16 && bz in 0 until 16) {
                            val block = chunk.getBlock(bx, by, bz)
                            if (block == Blocks.STONE.id || block == Blocks.SLATESTONE.id) {
                                chunk.setBlock(bx, by, bz, oreId)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun decorateSurface(chunk: Chunk, biomeMap: Array<Array<Biome>>) {
        for (x in 0 until 16) {
            for (z in 0 until 16) {
                val biome = biomeMap[x][z]
                val height = chunk.getHeightAt(x, z)

                if (height < 63) continue // Skip water

                // Place grass/flowers
                if (random.nextFloat() < biome.grassDensity) {
                    chunk.setBlock(x, height + 1, z, Blocks.TALL_GRASS.id)
                }

                // Place trees
                if (random.nextFloat() < biome.treeDensity && height > 63) {
                    placeTree(chunk, x, height + 1, z, biome)
                }
            }
        }
    }

    private fun placeTree(chunk: Chunk, x: Int, y: Int, z: Int, biome: Biome) {
        // *** FIX: Added a check to prevent trees from generating on chunk edges ***
        // This prevents leaves from spilling into ungenerated or different chunks.
        val leafRadius = 2
        if (x < leafRadius || x >= 16 - leafRadius || z < leafRadius || z >= 16 - leafRadius) {
            return
        }

        val treeHeight = 4 + random.nextInt(3)
        val logId = biome.treeLog
        val leavesId = biome.treeLeaves

        // Place trunk
        for (i in 0 until treeHeight) {
            chunk.setBlock(x, y + i, z, logId)
        }

        // Place leaves (simple sphere)
        val leafStart = y + treeHeight - 2
        for (dy in 0..2) {
            for (dx in -leafRadius..leafRadius) {
                for (dz in -leafRadius..leafRadius) {
                    if (dx * dx + dz * dz <= leafRadius * leafRadius) {
                        // Don't overwrite the top of the trunk
                        if (dx == 0 && dz == 0 && dy > 0) continue

                        val lx = x + dx
                        val ly = leafStart + dy
                        val lz = z + dz

                        // Check if the current block is air to avoid weird tree shapes
                        if(chunk.getBlock(lx, ly, lz) == Blocks.AIR.id) {
                            chunk.setBlock(lx, ly, lz, leavesId)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Simple Perlin noise implementation.
 */
class PerlinNoise(seed: Long) {
    private val random = Random(seed)
    // *** FIX: Correctly initialized the permutation table for standard Perlin noise. ***
    // The original code used random values, which is incorrect. This uses a shuffled
    // sequence of 0-255, duplicated to 512 entries to avoid bounds checking.
    private val permutation = IntArray(512).apply {
        val p = (0..255).shuffled(random)
        for (i in 0..511) {
            this[i] = p[i and 255]
        }
    }

    fun noise(x: Double, y: Double): Double {
        val xi = floor(x).toInt() and 255
        val yi = floor(y).toInt() and 255

        val xf = x - floor(x)
        val yf = y - floor(y)

        val u = fade(xf)
        val v = fade(yf)

        val aa = permutation[permutation[xi] + yi]
        val ab = permutation[permutation[xi] + yi + 1]
        val ba = permutation[permutation[xi + 1] + yi]
        val bb = permutation[permutation[xi + 1] + yi + 1]

        val x1 = lerp(grad(aa, xf, yf), grad(ba, xf - 1, yf), u)
        val x2 = lerp(grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1), u)

        return lerp(x1, x2, v)
    }

    fun noise3D(x: Double, y: Double, z: Double): Double {
        val xi = floor(x).toInt() and 255
        val yi = floor(y).toInt() and 255
        val zi = floor(z).toInt() and 255

        val xf = x - floor(x)
        val yf = y - floor(y)
        val zf = z - floor(z)

        val u = fade(xf)
        val v = fade(yf)
        val w = fade(zf)

        val p = permutation
        val aaa = p[p[p[xi] + yi] + zi]
        val aba = p[p[p[xi] + yi + 1] + zi]
        val aab = p[p[p[xi] + yi] + zi + 1]
        val abb = p[p[p[xi] + yi + 1] + zi + 1]
        val baa = p[p[p[xi + 1] + yi] + zi]
        val bba = p[p[p[xi + 1] + yi + 1] + zi]
        val bab = p[p[p[xi + 1] + yi] + zi + 1]
        val bbb = p[p[p[xi + 1] + yi + 1] + zi + 1]

        val x1 = lerp(grad3D(aaa, xf, yf, zf), grad3D(baa, xf - 1, yf, zf), u)
        val x2 = lerp(grad3D(aba, xf, yf - 1, zf), grad3D(bba, xf - 1, yf - 1, zf), u)
        val y1 = lerp(x1, x2, v)

        val x3 = lerp(grad3D(aab, xf, yf, zf - 1), grad3D(bab, xf - 1, yf, zf - 1), u)
        val x4 = lerp(grad3D(abb, xf, yf - 1, zf - 1), grad3D(bbb, xf - 1, yf - 1, zf - 1), u)
        val y2 = lerp(x3, x4, v)

        return lerp(y1, y2, w)
    }

    fun fbm(x: Double, y: Double, octaves: Int): Double {
        var value = 0.0
        var amplitude = 1.0
        var frequency = 1.0
        var maxValue = 0.0

        for (i in 0 until octaves) {
            value += noise(x * frequency, y * frequency) * amplitude
            maxValue += amplitude
            amplitude *= 0.5
            frequency *= 2.0
        }

        return value / maxValue
    }

    private fun fade(t: Double) = t * t * t * (t * (t * 6 - 15) + 10)

    private fun lerp(a: Double, b: Double, t: Double) = a + t * (b - a)

    private fun grad(hash: Int, x: Double, y: Double): Double {
        return when (hash and 3) {
            0 -> x + y
            1 -> -x + y
            2 -> x - y
            else -> -x - y
        }
    }

    private fun grad3D(hash: Int, x: Double, y: Double, z: Double): Double {
        val h = hash and 15
        val u = if (h < 8) x else y
        val v = if (h < 4) y else if (h == 12 || h == 14) x else z
        return (if (h and 1 == 0) u else -u) + (if (h and 2 == 0) v else -v)
    }
}

enum class Biome(
    val heightModifier: Double,
    val surfaceBlock: Int,
    val grassDensity: Float,
    val treeDensity: Float,
    val treeLog: Int,
    val treeLeaves: Int
) {
    PLAINS(1.0, Blocks.GRASS.id, 0.6f, 0.01f, Blocks.OAK_LOG.id, Blocks.OAK_LEAVES.id),
    FOREST(1.1, Blocks.GRASS.id, 0.7f, 0.08f, Blocks.OAK_LOG.id, Blocks.OAK_LEAVES.id),
    DESERT(0.9, Blocks.SAND.id, 0.05f, 0.001f, Blocks.OAK_LOG.id, Blocks.OAK_LEAVES.id),
    TUNDRA(0.8, Blocks.GRASS.id, 0.2f, 0.01f, Blocks.SPRUCE_LOG.id, Blocks.SPRUCE_LEAVES.id),
    TAIGA(1.2, Blocks.GRASS.id, 0.5f, 0.05f, Blocks.SPRUCE_LOG.id, Blocks.SPRUCE_LEAVES.id),
    SWAMP(0.7, Blocks.GRASS.id, 0.8f, 0.04f, Blocks.WILLOW_LOG.id, Blocks.WILLOW_LEAVES.id),
    JUNGLE(1.3, Blocks.GRASS.id, 0.9f, 0.12f, Blocks.OAK_LOG.id, Blocks.OAK_LEAVES.id)
}