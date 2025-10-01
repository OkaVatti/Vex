# **Vex — Complete Design Document**

*A Minecraft-inspired voxel survival game written in Kotlin*  
*Beta 1.7.3 aesthetic with modern quality-of-life and original systems*

---

## **Vision Statement**

Recreate the tactile, lo-fi charm and emergent gameplay of Minecraft Beta 1.7.3 while adding modern quality-of-life features, a deeper ecological and geological system, richer block and mob catalogs, and new dimensions and mechanics that feel natural to the classic experience. Implemented in Kotlin for type safety, concision, and excellent JVM interoperability.

---

## **1 — Core Design Philosophy**

### **1.1 Aesthetic Principles**
- **Visual Style:** Low-poly models with low-resolution textures (default 16×16 pixels)
- **Lighting:** Blocky, stepped lighting with optional smooth ambient occlusion toggle, colored lighting available, ray tracing available.
- **UI Design:** Simple, functional GUI inspired by Beta 1.7.3 with widescreen support
- **Color Palette:** Limited, curated palette evoking the Beta era's distinctive look
- **Audio:** Minimal, atmospheric soundscapes with biome-specific ambient tracks

### **1.2 Gameplay Philosophy**
- **Emergent Sandbox:** Player-driven goals and exploration
- **Simple Progression:** Intuitive crafting chains that gradually unlock complexity
- **Rewarding Exploration:** Rare resources, structures, and secrets encourage travel
- **Player Creativity:** Extensive building vocabulary with predictable block behavior
- **Meaningful Choices:** Different approaches to problems (combat vs stealth, mining vs trading)

### **1.3 Fidelity to Beta Era**
- Include all core mechanics from Minecraft Beta 1.7.3
- Preserve the "feel" of Beta generation (rolling hills, classic biome transitions)
- Maintain simple, predictable block interactions
- Optional "Beta Authentic" mode removes all modern conveniences

### **1.4 Modern Improvements**
- Recipe book with auto-suggestions (toggleable)
- Improved inventory management (shift-click, auto-sort)
- Accessibility features (colorblind modes, UI scaling)
- Controller and touch support
- Robust multiplayer with anti-cheat

---

## **2 — Technical Architecture**

### **2.1 Technology Stack**

**Core Language:** Kotlin (JVM)
- Type-safe, null-safe, concise syntax
- Excellent Java interoperability for libraries
- Coroutines for async operations
- Kotlin Multiplatform for cross-platform clients

**Rendering:** LWJGL + Vulkan
- Modular renderer architecture for future backends
- Greedy meshing for opaque blocks
- Separate translucent rendering pass
- Frustum and occlusion culling

**Networking:** Ktor
- Server/client architecture with Ktor for HTTP and WebSocket
- UDP broadcast for LAN discovery
- mDNS as fallback discovery mechanism

**Persistence:** Custom binary format
- Chunked region files (LZ4 or ZSTD compression)
- Optional Protocol Buffers for structured data
- Periodic autosaves with snapshot support

**Scripting:** Kotlin Scripting (KTS)
- Type-safe mod API
- Sandboxed execution environment
- Hot-reload support for development

**Build System:** Gradle with Kotlin DSL
- Multi-module project structure
- Separate dedicated server artifact
- Automated asset pipeline

### **2.2 Platform Targets**

**Primary Platforms:**
- Windows (x64)
- Linux (x64, ARM64)
- macOS (x64, Apple Silicon)

**Secondary Platforms:**
- Android (experimental)
- Dedicated headless server (Linux)

**Minimum Requirements:**
- CPU: Quad-core 2.5GHz
- RAM: 4GB (8GB recommended)
- GPU: Vulkan 
- Storage: 2GB + world data

---

## **3 — World Structure**

### **3.1 Vertical Range**

**Total Height:** 384 blocks (Y = -128 to Y = 255)

**Vertical Distribution:**
- **Y 192–255:** High peaks, sky features, weather effects
- **Y 128–191:** Mountain tops, high plateaus
- **Y 64–127:** Surface terrain, most biomes
- **Y 0–63:** Underground, shallow caves
- **Y -64–-1:** Deep caves, larger caverns
- **Y -128–-65:** Underdeep, bedrock layers, rare ores

**Benefits:**
- Deeper, more dramatic cave systems
- Taller mountains and builds
- Distinct geological strata
- More vertical exploration content

### **3.2 Chunk Architecture**

**Chunk Dimensions:** 16 × 384 × 16 blocks
- Subdivided into 24 sections of 16×16×16 blocks each
- Sections only allocated when non-empty (memory optimization)
- Each section tracks: blocks, block light, sky light, biome data

**Region Files:**
- Each region contains 32×32 chunk columns
- Compressed with LZ4 for fast read/write
- Incremental saves (only modified chunks)
- Header contains chunk offsets and timestamps

**Chunk States:**
- **Unloaded:** Not in memory
- **Loading:** Being read from disk or generated
- **Generated:** Terrain generated, awaiting decoration
- **Decorated:** Features placed, ready for lighting
- **Lit:** Lighting calculated, ready for meshing
- **Ready:** Fully loaded and renderable
- **Saving:** Being written to disk

### **3.3 Coordinate System**

**World Coordinates:**
- Block position: integer X, Y, Z
- Chunk position: X÷16, Z÷16
- Region position: X÷512, Z÷512

**Chunk-Local Coordinates:**
- 0–15 for X and Z within chunk
- 0–383 for Y within chunk column
- Section index: Y÷16 (0–23)

---

## **4 — World Generation**

### **4.1 Generation Pipeline**

**Phase 1: Biome Map (per-chunk)**
1. Sample temperature and humidity noise (low frequency)
2. Determine base biome from temperature/humidity
3. Apply river carving (erodes biome boundaries)
4. Blend biome edges for smooth transitions

**Phase 2: Terrain Shape (per-chunk)**
1. Sample terrain height noise (multiple octaves)
2. Apply biome-specific height modifiers
3. Generate continental shelf (ocean vs land)
4. Carve rivers and ravines

**Phase 3: Base Terrain (per-section)**
1. Fill blocks based on height map
2. Place bedrock layer (Y -128 to -122, scattered)
3. Apply geological strata rules
4. Generate ore veins and deposits

**Phase 4: Cave Generation (per-chunk)**
1. 3D Perlin worms for classic caves
2. Large cavern placement (rare)
3. Ravine carving (surface to deep)
4. Underwater caves and flooded sections

**Phase 5: Feature Decoration (per-chunk)**
1. Trees, plants, and surface features
2. Dungeon and structure placement
3. Village and ruin generation
4. Lake and pond placement

**Phase 6: Lighting (per-chunk)**
1. Calculate sky light propagation
2. Calculate block light from sources
3. Smooth lighting at chunk borders

### **4.2 Noise Functions**

**Biome Noise:**
- Temperature: OpenSimplex, scale 0.0012, 2 octaves
- Humidity: OpenSimplex, scale 0.0014, 2 octaves
- Produces smooth biome transitions

**Terrain Noise:**
- Base: FBM with 4 octaves, scale 0.0025
- Detail: FBM with 3 octaves, scale 0.01
- Mountain: Ridged multifractal, scale 0.002
- Amplitude: 64 blocks base, up to 180 for mountains

**Cave Noise:**
- Small caves: 3D Perlin, threshold 0.6, scale 0.04
- Large caverns: 3D Simplex, threshold 0.7, scale 0.015
- Density varies by depth (fewer caves near surface)

### **4.3 Geological Strata**

**Overworld (Eithus) Layers:**

**Y -128 to -122:** Bedrock (scattered, impenetrable)
**Y -122 to -96:** Deep Stone (dark, dense stone)
- Contains: Malachite Ore, Magnesite Crystals, Anthracite
- Rare: Diamond, Black Diamond

**Y -96 to -48:** Lower Stone Layer
- Contains: Iron, Gold, Cobalt, Lithium
- Common: Coal, Copper
- Rare: Emerald

**Y -48 to 0:** Middle Stone Layer
- Contains: Iron, Copper, Tin, Silver
- Common: Coal
- Occasional: Gold, Sapphire

**Y 0 to 48:** Upper Stone Layer
- Contains: Copper, Tin, Coal
- Surface exposure: Iron, gems in biome-specific locations
- Occasional: Opal, Ruby

**Y 48+:** Surface and mountains
- Surface ores: Copper (mountain biomes), gems (specific biomes)
- No underground generation

**Special Ore Distribution:**
- **Coal:** Y -64 to 128 (peaks at Y 32–64)
- **Copper:** Y -48 to 96 (peaks at Y 16–48)
- **Iron:** Y -64 to 64 (peaks at Y -16 to 32)
- **Gold:** Y -96 to 32 (peaks at Y -64 to -32)
- **Diamond:** Y -128 to -48 (peaks at Y -96 to -64)
- **Gemstones:** Biome and depth-specific distributions

### **4.4 Biome Catalog**

**Temperature Zones:** Cold, Temperate, Warm, Hot

#### **Cold Biomes (Snow, Ice)**

**Tundra**
- Temperature: -0.5 to 0.0
- Terrain: Flat to rolling, sparse features
- Blocks: Snow, Ice, Packed Ice, Stone
- Flora: Dead Bushes, occasional Spruce
- Fauna: Yaks (planned), Wolves
- Features: Ice spikes, frozen lakes

**Taiga**
- Temperature: 0.0 to 0.3
- Terrain: Hilly with coniferous forests
- Blocks: Snow (high elevation), Grass, Podzol
- Flora: Spruce Trees, Ferns, Berry Bushes
- Fauna: Elk, Wolves, Foxes, Blue Jays
- Features: Forest clearings, scattered boulders

**Mountains**
- Temperature: Varies by elevation
- Terrain: Steep peaks, dramatic cliffs
- Blocks: Stone, Snow (peaks), Gravel (slopes)
- Flora: Sparse trees at base, none at peaks
- Fauna: Mountain Goats (planned), Elk
- Features: Ore exposure, rare mountain lakes

#### **Temperate Biomes**

**Plains**
- Temperature: 0.4 to 0.6
- Terrain: Flat to gently rolling
- Blocks: Grass, Dirt, occasional Stone
- Flora: Tall Grass, Flowers (many varieties), Oak Trees (sparse)
- Fauna: Cows, Sheep, Pigs, Chickens, Coyotes, Blue Jays, Robins
- Features: Villages, surface dungeons, flower patches

**Forest**
- Temperature: 0.4 to 0.7
- Terrain: Rolling hills, dense trees
- Blocks: Grass, Dirt, Oak/Birch Logs
- Flora: Oak, Birch, Azalea Trees; Flowers, Tall Grass, Ferns
- Fauna: Elk, Deer, Wolves, Foxes, Blue Jays, Robins
- Features: Forest ponds, woodland cabins (rare)

**Birch Forest**
- Temperature: 0.5 to 0.7
- Terrain: Flat to rolling, very dense
- Blocks: Grass, Birch Logs
- Flora: Birch Trees (dense), Tall Grass
- Fauna: Deer, Foxes, Robins
- Features: Uniform tree distribution, peaceful atmosphere

**Swamp**
- Temperature: 0.6 to 0.8
- Terrain: Flat, waterlogged
- Blocks: Mud, Clay, Water, Oak Logs (swamp variant)
- Flora: Willow Trees, Lily Pads, Marsh Grass, Mushrooms
- Fauna: Frogs (planned), Herons, Witches (rare hostile)
- Features: Witch huts, slime spawning areas

#### **Warm/Hot Biomes**

**Jungle**
- Temperature: 0.8 to 1.0
- Terrain: Hilly, dense vegetation
- Blocks: Grass, Jungle Logs, Vines
- Flora: Jungle Trees (massive), Cocoa, Bamboo, exotic Flowers
- Fauna: Jungle Panthers (planned), Parrots (planned)
- Features: Jungle temples, dense canopy

**Bayou**
- Temperature: 0.7 to 1.0, high humidity
- Terrain: swampy, waterlogged, dense vegetation.
- Blocks: Grass (dull color), Dirt, Willow Logs
- Flora: Willow Trees (vines and drooping leaves), Tall Grass, reeds, sugar cane, lots of water
- Fauna: Planned: Aligators, Frogs, Fireflies, Dragonflies, 
- Features: Scattered trees, warm color palette

**Desert**
- Temperature: 1.0+
- Terrain: Dunes, flat expanses, occasional mesa
- Blocks: Sand, Sandstone, Cactus
- Flora: Cacti, Dead Bushes
- Fauna: Coyotes, Rabbits
- Features: Desert temples, fossil structures, oases (rare)

**Badlands**
- Temperature: 0.8 to 1.0
- Terrain: Mesas, layered terracotta cliffs
- Blocks: Terracotta (many colors), Red Sand
- Flora: Minimal, Dead Bushes, Cacti
- Fauna: Rockworms (hostile, planned)
- Features: Gold exposure, abandoned mineshafts

#### **Special Biomes**

**Ocean**
- Temperature: Varies (cold/warm variants)
- Terrain: Deep water, underwater ravines
- Blocks: Water, Gravel, Sand, Clay, Prismarine (rare)
- Flora: Kelp, Seagrass, Coral (warm oceans)
- Fauna: Fish, Squid, Clams (clamshells → pearls)
- Features: Ocean monuments (rare), shipwrecks, underwater ruins

**Beach**
- Temperature: Matches adjacent biome
- Terrain: Thin strip between ocean and land
- Blocks: Sand (warm) or Gravel (cold)
- Flora: Minimal
- Fauna: Herons, Turtles
- Features: Transition zone

**River**
- Temperature: Matches biome
- Terrain: Carved waterway through land
- Blocks: Water, Gravel, Sand
- Flora: Sugar Cane (banks), Lily Pads
- Fauna: Fish, Herons
- Features: Connects biomes, good for transport

**Mushroom Grotto**
- Temperature: 0.5
- Terrain: isolated islands with caverns and hidden beaches filled with mushrooms.
- Blocks: Mycelium, Giant Mushrooms
- Flora: Giant Red and Brown Mushrooms, Mushroom flowers
- Fauna: Mushroom Villagers (passive traders), Mooshrooms (planned)
- Features: No hostile mob spawning, peaceful sanctuary

### **4.5 Structure Generation**

**Villages**
- Biomes: Plains, Desert, Taiga, Savanna
- Composition: Houses, wells, farms, blacksmith
- NPCs: Villagers (traders, sleep in beds)
- Loot: Blacksmith chests (tools, food, occasionally gems)
- Frequency: Rare (1 per ~800–1200 chunks)

**Abandoned Mineshafts**
- Location: Underground, any biome
- Composition: Oak planks, fences, rails, mine cart chests
- Hazards: Cave spiders, unstable supports
- Loot: Ores, tools, rails, occasionally gemstones
- Frequency: Uncommon (1 per ~400 chunks)

**Dungeons**
- Location: Underground caves
- Composition: Mossy Cobblestone, mob spawner, chests
- Spawners: Zombies, Skeletons, Cave Spiders
- Loot: Weapons, armor, food, rare enchanted items
- Frequency: Common (multiple per world)

**Ruins**
- Location: Surface, various biomes
- Types: Stone circles, collapsed structures, stone pillars
- Purpose: Hints at lore, sometimes portal activation sites
- Loot: Minimal, atmospheric
- Frequency: Uncommon

**Castles (Abandoned)**
- Location: Mountains, Badlands
- Composition: Stone brick, damaged areas
- NPCs: Undead Knights (hostile)
- Loot: Armor, weapons, rare enchanted gear, gemstones
- Frequency: Rare (1 per major mountain region)

**Temples**
- Types: Desert Temple, Jungle Temple
- Composition: Sandstone (desert), Cobblestone (jungle)
- Puzzles: Tripwires, hidden chests, mechanisms
- Loot: Diamonds, gold, emeralds, enchanted books
- Frequency: Rare (1 per desert/jungle)

---

## **5 — Blocks & Materials**

### **5.1 Block Categories & Counts**

**Total Block Types:** ~450+ unique blocks

**Natural Terrain Blocks:** 85+
**Ores & Crystals:** 31 ore types
**Wood Types:** 8 species × 7 variants each = 56 blocks
**Stone Types:** 7 types × 6 variants each = 42 blocks
**Functional Blocks:** 50+
**Decorative Blocks:** 120+
**Plants & Organic:** 85+

### **5.2 Natural Terrain Blocks**

#### **Stone Variants (7 base types)**

1. **Stone** (Generic)
   - Smooth Stone, Cobblestone, Stone Bricks, Mossy variants
   - Slabs, Stairs, Walls (all variants)
   - Used: Structure, tools (cobble)

2. **Granite**
   - Polished variant, Bricks
   - Slabs, Stairs, Walls
   - Reddish color, decorative

3. **Diorite**
   - Polished variant, Bricks
   - Slabs, Stairs, Walls
   - White speckled, decorative

4. **Andesite**
   - Polished variant, Bricks
   - Slabs, Stairs, Walls
   - Gray, decorative

5. **Deep Stone**
   - Found Y < -96
   - Dark, dense appearance
   - Harder to mine than regular stone
   - Variants: Smooth, Polished, Bricks
   - Slabs, Stairs, Walls

6. **Limestone**
   - Found in specific biomes
   - Light colored, sedimentary
   - Can be smelted into Quicklime
   - Variants: Smooth, Polished, Bricks

7. **Sandstone**
   - Naturally generated below sand
   - Variants: Smooth, Chiseled, Cut
   - Slabs, Stairs, Walls
   - Used in desert structures

#### **Soil & Organic Blocks**

**Dirt**
- Standard block, spreads to grass in light
- Path variant (compressed by walking)
- Coarse Dirt (doesn't spread grass)

**Grass Block**
- Top texture changes by biome
- Side has dirt/grass transition
- Can be silk-touched

**Podzol**
- Taiga biome soil
- Doesn't spread
- Allows mushrooms in any light

**Mycelium**
- Mushroom biome exclusive
- Spreads slowly like grass
- Purple particle effects

**Silt**
- Falls when unsupported
- Grey Sand
- Found in near wet and dry areas, including underground

**Sand**
- Falls when unsupported
- Red Sand variant (badlands)
- Can be smelted into glass

**Gravel**
- Falls when unsupported
- Drops flint occasionally
- Found near water and underground

**Clay**
- Found underwater
- Can be crafted into bricks or terracotta
- Dyes into colored terracotta

**Mud**
- Swamp biome
- Slows movement slightly
- Can be packed into Mud Bricks

**Snow**
- Accumulates in layers (0–8)
- Full snow block variant
- Melts near heat sources

**Ice**
- Transparent, slippery
- Packed Ice (doesn't melt)
- Found in cold biomes


#### **Wood Types (8 species)**

Each wood type includes:
- **Log** (bark on all sides)
- **Stripped Log** (bark removed)
- **Wood** (bark on all 6 sides)
- **Planks**
- **Stairs**
- **Slabs** (horizontal and vertical)
- **Fence**
- **Fence Gate**
- **Door**
- **Trapdoor**
- **Sign**
- **Pressure Plate**
- **Button**

**Species:**
1.  **Oak** - Classic, warm brown
2.  **Birch** - Pale, off-white
3.  **Spruce** - Dark brown
4   **Pine** - Pale Orangish-Brown
5.  **Jungle** - Reddish-brown
6.  **Palm** - Pale Yellow 
7.  **Cyprus** - Pale Brown with greenish undertones
8.  **Dark Oak** - Very dark brown
9.  **Willow** - Gray-green (bayou)
10. **Sakura** - Pale pink (rare)
12. **Maple** - Reddish-Brown
11. **Rubber** - Pale-Yellow with Silver Highlights

**Special Wood Properties:**
- Willow: High Essence radiation
- Sakura: Moderate Magic radiation
- Dark Oak: Low Essence radiation
- Maple: Sap used to make different types of syrups
- Rubber: New material and farming equipment
- All logs radiate Essence (increases with tree density)

### **5.3 Ores, Metals, Alloys & Minerals**

#### **Complete Ore System (31 types)**

**Natural Ores (Base Materials)**

| Ore | Category | Y-Level | Biome Specificity | Vein Size | Rarity |
|-----|----------|---------|-------------------|-----------|--------|
| **Coal** | Fossil Fuel | -64 to 128 | All | 8-16 | Very Common |
| **Copper** | Weak Metal | -48 to 96 | All, more in mountains | 6-12 | Common |
| **Tin** | Weak Metal | -48 to 64 | All | 4-8 | Common |
| **Silver** | Magical Weak Metal | -64 to 32 | Mountains, Mushroom | 3-6 | Uncommon |
| **Gold** | Weak Metal | -96 to 32 | All, more in badlands | 4-8 | Uncommon |
| **Cobalt** | Weak Metal | -80 to 0 | All | 3-6 | Uncommon |
| **Lithium** | Weak Metal | -64 to 16 | Desert, Badlands | 2-4 | Rare |
| **Iron** | Heavy Metal | -64 to 64 | All | 6-12 | Common |
| **Platinum** | Heavy Metal | N/A | Aethus only | 2-4 | Very Rare |
| **Titanium** | Heavy Metal | N/A | Hathus only | 3-6 | Rare |
| **Malachite** | Heavy Metal | -128 to -64 | Deep underground | 2-4 | Very Rare |

**Gemstones (Natural)**

| Gem | Y-Level | Biome Specificity | Rarity | Properties |
|-----|---------|-------------------|--------|------------|
| **Opal** | 0 to 64 | Desert, Mesa | Uncommon | Magical, iridescent |
| **Fire Opal** | -32 to 32 | Desert, Volcanic | Rare | Magical, fiery glow |
| **Moonstone** | -16 to 48 | All, night finds | Uncommon | Magical, subtle glow |
| **Sapphire** | -64 to 16 | Mountains, Ocean | Rare | Magical, blue |
| **Star Sapphire** | -80 to 0 | Mountains | Very Rare | Magical, asterism effect |
| **Ruby** | -64 to 16 | Jungle, Mountains | Rare | Magical, red |
| **Heart Ruby** | -80 to -32 | Deep caves | Very Rare | Magical, heart-shaped |
| **Amethyst** | -48 to 32 | Geodes (rare) | Uncommon | Magical, purple |
| **Emerald** | -32 to 64 | Mountains only | Very Rare | Magical, trading |
| **Diamond** | -128 to -48 | Deep underground | Very Rare | Magical, hardest |
| **Black Diamond** | -128 to -96 | Deepest layers | Extremely Rare | Magical, void aspect |

**Oceanic Gems**

| Gem | Source | Rarity | Properties |
|-----|--------|--------|------------|
| **Pearl** | Clamshells (ocean floor) | Uncommon | Lustrous, trading |
| **Black Pearl** | Ancient Shells (deep ocean) | Very Rare | Magical, valuable |

**Crystals (Natural)**

| Crystal | Y-Level | Dimension | Properties |
|---------|---------|-----------|------------|
| **Quartz** | -32 to 64 | Eithus, Hathus | Fireproof, decorative |
| **Rose Quartz** | N/A | Aethus | Magical, pink, fireproof |
| **Black Quartz** | N/A | Hathus | Magical, black, fireproof |
| **Anthracite** | -128 to -80 | Hathus | Compact coal, high fuel value |
| **Magnesite** | -96 to -48 | Hathus | Fireproof, magnetic properties |
| **Luminum** | N/A | Tythus | Luminous crystal, glows naturally |

**Manmade Alloys (Crucible Required)**

| Alloy | Components | Properties | Use |
|-------|------------|------------|-----|
| **Bronze** | Copper + Tin | Sturdy, corrosion-resistant | Tools, armor (Tier 3) |
| **Orichalcum** | Gold + Silver | Sturdy, magical conductivity | Tools, magical items |
| **Steel** | Iron + Coal | Heavy, strong, magnetic | Tools, armor (Tier 5) |
| **Blue Steel** | Steel + Cobalt | Heavy, conductive, magnetic | Advanced contraptions, tools |

#### **Ore Processing Chain**

1. **Mining:** Ore block → Raw Ore (or gem directly)
2. **Smelting (Kiln):** Raw Ore → Ingot (or refined gem)
3. **Alloying (Crucible):** Metal A + Metal B → Unrefined Alloy
4. **Refining (Kiln):** Unrefined Alloy → Alloy Ingot
5. **Crafting:** Ingots → Tools, Armor, Blocks

**Special Processing:**
- **Cobalt Dust:** Cobalt Ingot → 4 Dust (crafting)
- **Lithium Salt:** Lithium Ingot → 2 Salt (kiln + chemical)
- **Metal Blocks:** 9 Ingots → 1 Decorative/Storage Block

#### **Ore Block Variants**

Each ore (except gemstones) has variants:
- **Stone Ore** (default host rock)
- **Slatestone Ore** (Y < -96, harder to mine)
- **Magnesite Ore** (Hathus, higher yield but dangerous)
- 

**Ore Textures:**
- Coal: Black specks in stone
- Copper: Green-blue veins
- Tin: Silver with Brown Rust specks
- Cobolt: Deep Blue Veins
- Lithium: Yellowish-Pink Veins
- Silver: Silver veins
- Iron: Beige/tan, with rust spots
- Gold: Yellow flecks
- Platinum: Pale Silvery-Purple Veins 
- Titanium: Blue-ish silver Spots
- Malachite: Turquoise-Cyan With deep layering
- Opal: Iridescent Chunk of Stone
- Fire Opal: Irridescent Chunk of Opal and Obsidian
- Moonstone: Pale blue crystals
- Sapphire: Deep Blue crystals
- Ruby: Deep Red Crystals
- Amethyst: Deep Purple Crystals
- Diamond: Light blue crystals
- Black Diamond: Black Crystals
- Emerald: Bright green crystals

### **5.4 Functional Blocks**

#### **Crafting & Processing Stations**

**Workbench**
- **Recipe:** 4 Wood Planks (any type)
- **Function:** Standard 3×3 crafting grid
- **UI:** Recipe book integration, search bar
- **Use:** Basic item crafting

**Advanced Workbench**
- **Recipe:** Workbench + 4 Iron Ingots + 2 Diamonds
- **Function:** Extended 5×5 crafting grid
- **UI:** Supports complex recipes, shows material costs
- **Use:** Advanced items, specialized tools, magical items

**Kiln (Furnace)**
- **Recipe:** 8 Cobblestone
- **Function:** Smelts ores and cooks food
- **Fuel:** Coal, Charcoal, Wood, Blaze Rods, Lava Bucket, Anthracite
- **Speed:** 10 seconds per item (standard)
- **UI:** Input slot, fuel slot, output slot, burn timer

**Blast Kiln**
- **Recipe:** Kiln + 5 Iron Ingots + 3 Smooth Stone
- **Function:** Fast metal smelting only (no food)
- **Speed:** 5 seconds per item (2× speed)
- **Fuel:** Same as Kiln but consumes faster
- **Use:** Efficient ore processing

**Crucible**
- **Recipe:** 7 Steel Ingots + Kiln + Obsidian
- **Function:** Alloy creation from two metals
- **Process:** 
  1. Insert Metal A (ingot)
  2. Insert Metal B (ingot)
  3. Add fuel
  4. Output: Unrefined Alloy
- **Supported Alloys:** Bronze, Orichalcum, Steel, Blue Steel
- **Speed:** 30 seconds per alloy

**Anvil**
- **Recipe:** 3 Iron Blocks + 4 Iron Ingots
- **Function:** Item repair, combining, and upgrading
- **Uses:** Repairs cost iron; degrades over time
- **Features:**
  - Combine two damaged tools → repaired tool + bonus durability
  - Combine tool + upgrade material → enhanced tool
  - Rename items (cosmetic)

**Toolbench**
- **Recipe:** Workbench + 2 Iron Ingots + Anvil (in Advanced Workbench)
- **Function:** Weapon and armor modification
- **Features:**
  - Socket gems into items (stat bonuses)
  - Replace tool handles (durability, speed trade-offs)
  - Add armor plating (protection, weight trade-offs)
  - Sharpen weapons (damage boost, durability cost)

#### **Agriculture & Food Processing**

**Composter**
- **Recipe:** 7 Wooden Slabs (any type)
- **Function:** Converts plant matter into bone meal
- **Input:** Seeds, saplings, leaves, crops, flowers
- **Process:** Fills in layers, 100% = 1 bone meal
- **Output:** Bone meal (crop growth accelerator)

**Mill**
- **Recipe:** 4 Stone + 2 Iron Ingots + Gear mechanism
- **Function:** Grinds seeds and grains into flour
- **Process:** Wheat → Flour, Seeds → Seed Meal
- **Use:** Flour for baking bread, cakes, cookies

**Barrel**
- **Recipe:** 6 Planks + 2 Slabs (any wood)
- **Function:** Storage (27 slots, same as chest)
- **Feature:** Can be opened with block on top
- **Use:** Compact storage, brewing ingredient aging (future)

#### **Magical & Essence Systems**

**Mystic Altar**
- **Recipe:** 4 Obsidian + 2 Diamonds + 2 Emeralds + Book
- **Function:** Item enchantment using magical energy
- **Mechanics:**
  - Requires surrounding bookshelves (up to 15)
  - Consumes Lapis Lazuli and Essence
  - Produces Cobalt Signal based on area's Magic Level
- **Process:**
  1. Place item and Lapis Lazuli in slots
  2. Select enchantment from available options
  3. Consumes levels and materials
  4. Item receives magical enhancement
- **Signal:** Strength 0-15 based on ambient Magic Level
- **Enchantments:** Sharpness, Durability, Efficiency, Fortune, Silk Touch, Protection, etc.

**Essence Extractor**
- **Recipe:** 4 Copper Ingots + 2 Silver Ingots + Glass Bottle + Cobalt Dust
- **Function:** Extracts Essence from environment
- **Mechanics:**
  - Collects ambient Essence over time
  - Produces Cobalt Signal based on Essence Level (0-15)
  - Output: Essence Bottles (liquid form)
- **Use:** 
  - Essence used in advanced crafting
  - Potion brewing component
  - Magical item creation
- **Rate:** 1 bottle per 10 minutes in high-Essence areas

**Tree Tap**
- **Recipe:** 3 Copper Ingots + 2 Wood Planks + Bucket
- **Function:** Extracts sap from adjacent tree logs
- **Placement:** Must be adjacent to natural log block
- **Process:** 
  - Slowly fills with sap over time
  - Right-click with empty bottle to collect
  - Doesn't damage tree
- **Output:** Sap (used in crafting, can be refined to resin)
- **Rate:** 1 bottle per hour per tap

#### **Utility & Storage Blocks**

**Chest**
- **Recipe:** 8 Planks (any wood type)
- **Capacity:** 27 item slots
- **Features:**
  - Can combine two chests → Double Chest (54 slots)
  - Visual wood type matches recipe
  - Locked with key item (optional)
- **Opening:** Produces sound, cat on chest prevents opening

**Metal Chests**
- **Variants:** Copper, Bronze, Iron, Steel, Silver, Gold
- **Recipe:** 8 Metal Ingots (respective type)
- **Capacity:** 
  - Copper/Bronze: 36 slots
  - Iron: 45 slots
  - Steel: 54 slots
  - Silver/Gold: 63 slots
- **Features:** 
  - More durable than wood
  - Blast resistant (Iron+)
  - Doesn't burn

**Cobalt Chest**
- **Recipe:** Chest + 4 Cobalt Ingots + Cobalt Dust
- **Capacity:** 27 slots (like standard chest)
- **Signal:** Emits Cobalt Signal (strength 15) when opened
- **Use:** Detection system, contraptions triggered by accessing chest

**Crate**
- **Recipe:** 6 Planks + 2 Slabs
- **Capacity:** 18 slots
- **Feature:** Stackable (up to 3 high), can be pushed
- **Use:** Compact storage, organizing

#### **Lighting Blocks**

**Torch**
- **Recipe:** Stick + Coal/Charcoal
- **Light Level:** 14
- **Placement:** Wall or floor, requires solid block
- **Properties:** Standard light source, melts snow/ice nearby

**Cobalt Torch**
- **Recipe:** Stick + Cobalt Dust
- **Light Level:** 5 (dim)
- **Signal:** Emits Cobalt Signal (strength 10)
- **Use:** Dim lighting with circuit functionality
- **Properties:** Works underwater, doesn't melt ice

**Luminum Torch**
- **Recipe:** Stick + Luminum Crystal
- **Light Level:** 7
- **Effect:** Radiates Magic (radius 3) and Essence (radius 2)
- **Properties:** Soft glow, enchanting ambiance
- **Use:** Enhancing magical/essence levels for crafting

**Amethyst Torch**
- **Recipe:** Stick + Amethyst
- **Light Level:** 12
- **Properties:** Purple light, purely decorative variant

**Ultraviolet Torch**
- **Recipe:** Stick + Quartz + Luminum Dust
- **Light Level:** 8
- **Effect:** Accelerates plant growth in radius 2
- **Use:** Indoor/underground farming
- **Growth Rate:** 1.5× normal speed

**Salt Lamp**
- **Recipe:** 4 Salt Blocks + 4 Iron Ingots + Cobalt Dust
- **Light Level:** Configurable 0-15 (via Cobalt Signal input)
- **Signal:** Receives Cobalt Signal
- **Features:**
  - Toggle on/off with signal
  - Dimmer functionality
  - Warm orange/pink glow
- **Use:** Variable lighting systems, mood lighting

**Ultraviolet Lamp**
- **Recipe:** 4 Glass + 2 Quartz + Luminum + Iron
- **Light Level:** 10
- **Effect:** Plant growth acceleration (radius 5)
- **Use:** Large-scale indoor farms
- **Growth Rate:** 2× normal speed
- **Power:** Requires Cobalt Signal to activate

**Luminum Lamp**
- **Recipe:** 4 Luminum + 4 Glass + Cobalt Dust
- **Light Level:** Configurable 0-15 (via Cobalt Signal)
- **Signal:** Receives Cobalt Signal
- **Features:**
  - Variable brightness
  - Toggle on/off
  - Radiates Essence/Magic while lit
- **Use:** Adjustable lighting, magical ambiance

**Colourium Lamp**
- **Recipe:** 4 Glass + Luminum + 3 Dyes + Cobalt Dust
- **Light Level:** Configurable 0-15
- **Color:** Changes based on Cobalt Signal strength (0-15)
- **Signal:** Receives Cobalt Signal
- **Colors:** Cycles through spectrum (red→orange→yellow→green→cyan→blue→purple)
- **Use:** Decorative, status indicators, colorful lighting

**Luminum Block**
- **Recipe:** 9 Luminum Crystals
- **Light Level:** 10 (always on)
- **Effect:** Radiates Magic (radius 6) and Essence (radius 4)
- **Properties:** Solid, glowing block
- **Use:** Permanent light source, magical area enhancement

**Cobalt Block**
- **Recipe:** 9 Cobalt Ingots
- **Light Level:** 3 (when active)
- **Signal:** 
  - Receives Cobalt Signal
  - Weakens signal by 1 as it passes through
  - Toggleable light when powered
- **Use:** Signal dampening, toggleable dim light, circuit components

#### **Cobalt Contraptions (Redstone-like System)**

**Cobalt Dust**
- **Recipe:** Cobalt Ingot → 4 Cobalt Dust (crafting)
- **Placement:** Ground, walls (like redstone)
- **Signal:** Transmits Cobalt Signal (0-15 strength)
- **Properties:**
  - Works underwater (unlike redstone)
  - Weakens by 1 per block (15 block range)
  - Can climb walls with proper placement
  - Visually glows when powered (blue)
- **Uses:** Wiring for contraptions, logic circuits

**Cobalt Signal Extender (Repeater)**
- **Recipe:** 3 Stone + 2 Cobalt Dust + Cobalt Torch
- **Function:** Extends signal back to full strength (15)
- **Delay:** Configurable 1-4 ticks (right-click to adjust)
- **Directional:** Only forwards signal in one direction
- **Use:** Long-distance signal transmission

**Cobalt Gates**

**AND Gate**
- **Recipe:** 3 Stone + 2 Cobalt Dust + Iron
- **Function:** Outputs signal only if BOTH inputs active
- **Size:** Single block
- **Truth Table:** A=1, B=1 → Output=1

**OR Gate**
- **Recipe:** 3 Stone + 2 Cobalt Dust + Copper
- **Function:** Outputs signal if ANY input active
- **Size:** Single block
- **Truth Table:** A=1 OR B=1 → Output=1

**NOT Gate (Inverter)**
- **Recipe:** 3 Stone + 2 Cobalt Dust + Quartz
- **Function:** Inverts signal (on→off, off→on)
- **Size:** Single block
- **Truth Table:** A=0 → Output=1; A=1 → Output=0

**XOR Gate**
- **Recipe:** 3 Stone + 3 Cobalt Dust + Gold
- **Function:** Outputs signal if inputs differ
- **Size:** Single block
- **Truth Table:** A≠B → Output=1

**NAND Gate**
- **Recipe:** 3 Stone + 2 Cobalt Dust + Steel
- **Function:** Inverted AND (outputs unless both inputs on)
- **Size:** Single block
- **Use:** Universal gate (can build any logic from NAND)

**NOR Gate**
- **Recipe:** 3 Stone + 2 Cobalt Dust + Silver
- **Function:** Inverted OR (outputs only if both inputs off)
- **Size:** Single block
- **Use:** Universal gate, memory cells

#### **Input Devices**

**Button**
- **Recipe:** 1 Stone/Wood/Metal (various types)
- **Function:** Momentary signal (1.5 seconds)
- **Placement:** Wall or floor
- **Signal Strength:** 15
- **Use:** Temporary activation, doors, traps

**Switch (Lever)**
- **Recipe:** Stick + Cobblestone
- **Function:** Toggle signal on/off
- **Placement:** Wall, floor, ceiling
- **Signal Strength:** 15
- **State:** Persists (saved with world)
- **Use:** Permanent switches, lighting systems

**Pressure Plate**
- **Materials:** Wood, Stone, Iron, Gold
- **Function:** Emits signal when entity on top
- **Weight Sensitivity:**
  - Wood: Any entity (mobs, items, player)
  - Stone: Mobs and players only
  - Iron: Players only
  - Gold: Items only (weighted by stack size)
- **Signal Strength:** 
  - Wood/Stone/Iron: 15 (on/off)
  - Gold: 0-15 based on item count
- **Use:** Automatic doors, item detection, mob traps

**Weighted Pressure Plate**
- **Recipe:** 2 Gold Ingots
- **Function:** Analog output based on entities
- **Signal:** 0-15 based on number of entities
- **Use:** Counting systems, analog circuits

#### **Output Devices**

**Door**
- **Materials:** 8 wood types, Iron
- **Function:** Opens when powered (iron) or clicked (wood)
- **Signal:** Iron doors require Cobalt Signal
- **Size:** 2 blocks tall
- **Types:** Single or double doors

**Trapdoor**
- **Materials:** 6 wood types, Iron
- **Function:** Opens when powered (iron) or clicked (wood)
- **Signal:** Iron trapdoors require Cobalt Signal
- **Placement:** Horizontal or vertical
- **Use:** Hidden entrances, mob control

**Piston**
- **Recipe:** 3 Planks + 4 Cobblestone + Iron + Cobalt Dust
- **Function:** Pushes blocks when powered
- **Range:** 1 block push distance
- **Limit:** Pushes up to 12 blocks
- **Cannot Push:** Obsidian, Bedrock, Tile Entities

**Sticky Piston**
- **Recipe:** Piston + Slimeball
- **Function:** Pushes AND pulls blocks
- **Use:** Moving platforms, hidden doors, contraptions

**Dispenser**
- **Recipe:** 7 Cobblestone + Bow + Cobalt Dust
- **Function:** Dispenses or uses items when powered
- **Capacity:** 9 slots
- **Behavior:**
  - Arrows → shoots
  - Armor → equips on player
  - Bucket → places liquid
  - Other items → drops as entity

**Dropper**
- **Recipe:** 7 Cobblestone + Cobalt Dust
- **Function:** Drops items (doesn't use them)
- **Capacity:** 9 slots
- **Use:** Item transportation, sorting systems

**Funnel**
- **Recipe:** 5 Iron Ingots + Chest
- **Function:** 
  - Collects items above (1 block radius)
  - Transfers to container below/beside
  - Can be locked with Cobalt Signal
- **Speed:** 1 item per 0.4 seconds
- **Use:** Automatic item collection, sorting

**Note Player**
- **Recipe:** 8 Planks + Cobalt Dust
- **Function:** Plays note when powered
- **Pitch:** Adjustable by clicking (25 pitches)
- **Instrument:** Determined by block below
  - Wood → Bass
  - Stone → Kick drum
  - Glass → Click
  - Sand → Snare
- **Use:** Music, doorbells, alarms

**Mini-Diskette Player**
- **Recipe:** 4 Iron + 2 Cobalt Dust + 2 Diamonds + Jukebox
- **Function:** Plays Mini-Diskettes (rare music items)
- **Discs:** Custom tracks, found from Wanderers/Strangers
- **Features:** Volume control, playlist support
- **Signal:** Emits Cobalt Signal when playing

#### **Magnetic Devices**

**Magnet**
- **Recipe:** 4 Iron Ingots + Malachite + Cobalt Dust
- **Function:** Attracts magnetic entities/blocks when powered
- **Signal:** Requires Cobalt Signal to activate
- **Range:** 8 block radius
- **Pull Strength:** 0.5 blocks/tick
- **Affects:**
  - Iron/Steel items (dropped)
  - Iron/Steel armor stands
  - Minecarts
  - Magnetic blocks (Iron, Malachite, Steel)

**Polar Magnet**
- **Recipe:** Magnet + 2 Malachite + 2 Cobalt Ingots
- **Function:** Directional magnetic poles (N/S)
- **Signal:** Requires Cobalt Signal
- **Poles:** 
  - North (blue end): Pulls toward
  - South (red end): Pushes away
- **Range:** 12 block radius
- **Use:** 
  - Item sorting by magnetism
  - Transportation systems
  - Advanced contraptions
- **Interaction:** Two polar magnets can attract/repel each other

#### **Miscellaneous Functional Blocks**

**Mapping Table**
- **Recipe:** 4 Planks + 2 Paper + Compass
- **Function:** Create and modify maps
- **Process:**
  - Insert paper + ink → empty map
  - Insert empty map → fills with explored area
  - Add dye → color-code map
  - Insert compass → add position marker
- **Features:**
  - Zoom levels (4 zoom-outs possible)
  - Clone maps
  - Lock maps (prevent changes)

**Loom**
- **Recipe:** 2 Planks + 2 String
- **Function:** Apply patterns to banners
- **Patterns:** 30+ available patterns
- **Process:** Banner + Dye + Pattern (optional)
- **Use:** Decorative banners, faction markers

**Cartography Table**
- **Recipe:** 4 Planks + 2 Paper
- **Function:** Map editing and combining
- **Features:**
  - Combine maps (create larger maps)
  - Copy maps
  - Add markers to maps
  - Lock maps

**Grindstone**
- **Recipe:** 2 Sticks + Stone Slab + 2 Planks
- **Function:** 
  - Remove enchantments from items (get XP back)
  - Repair items by combining two damaged items
- **Use:** Recycling enchanted items, repairing without anvil cost

**Wetstone**
- **Recipe:** Water Bucket + Limestone
- **Function:** Used to sharpen tools, slightly increasing their durability, and stats temporarily
- **Requirement:** At least a 2x2 Crafting interface
- **Effects:** Haste effect applied when using sharpened tool 

**Scaffolding**
- **Recipe:** 6 slabs + Rope
- **Function:** Climbable temporary structure
- **Placement:** Stacks vertically automatically
- **Removal:** Break bottom → all collapse
- **Use:** Building aid, vertical movement
- **Properties:** 
  - Walkable
  - Doesn't require support (floating)
  - Very fast to place/remove

**Ladder**
- **Recipe:** 7 Sticks
- **Function:** Climbable, attaches to walls
- **Speed:** Moderate climbing speed
- **Use:** Vertical access

**Rope**
- **Recipe:** 9 String
- **Function:** Long climbable line
- **Placement:** Hangs down from attachment point
- **Length:** Up to 64 blocks
- **Speed:** Fast climbing speed
- **Use:** Deep mine shafts, cliff descent

**Compressor** (planned)
- **Function:** Compress items into dense variants
- **Use:** 9 items → 1 block (e.g., 9 iron → 1 iron block)

---

### **5.5 Decorative Blocks**

#### **Glass & Panes**

**Glass**
- **Recipe:** Smelt sand in kiln
- **Variants:** Clear, Tinted (light-blocking)
- **Properties:** Transparent, fragile (no drop without Silk Touch)

**Stained Glass**
- **Recipe:** 8 Glass + Dye (center)
- **Colors:** 16 dye colors
- **Properties:** Colored transparency, no drop without Silk Touch

**Tempered Glass**
- **Recipe:** Smelt Glass in a Blast Kiln
- **Variants:** Clear, Tinted,
- **Properties:** Transparency, Durable (Will always drop)

**Glass Pane**
- **Recipe:** 6 Glass → 16 Panes
- **Form:** Thin window pane
- **Connection:** Connects to adjacent panes/blocks
- **Variants:** All stained glass colors

#### **Decorative Stone**

**Bricks**
- **Recipe:** 4 Clay Balls → Brick (kiln)
- **Block:** 4 Bricks → Brick Block
- **Variants:** Slabs, Stairs, Walls

**Terracotta**
- **Recipe:** Clay Block → Terracotta (kiln)
- **Stained:** Terracotta + Dye → Stained Terracotta (16 colors)
- **Glazed:** Stained Terracotta → Glazed (kiln, patterns)

**Concrete**
- **Recipe:** Concrete Powder (4 Sand + 4 Gravel + Dye) + Water
- **Colors:** 16 colors
- **Properties:** Solid, vibrant color, construction-friendly

**Prismarine**
- **Source:** Ocean monuments
- **Variants:** Regular, Bricks, Dark
- **Properties:** Animated texture (shimmering)

#### **Organic Decorative**

**Wool**
- **Source:** Sheep (shearing)
- **Colors:** 16 colors (via dyeing)
- **Use:** Decorative, beds, banners, soft blocks

**Carpet**
- **Recipe:** 2 Wool → 3 Carpet
- **Colors:** 16 colors
- **Properties:** 1/16 block thick, decorative flooring

**Banners**
- **Recipe:** 6 Wool + Stick
- **Colors:** 16 base colors
- **Patterns:** 30+ patterns (via loom)
- **Use:** Decoration, faction markers, way-finding

**Paintings**
- **Recipe:** 8 Sticks + Wool (center)
- **Variants:** 26 different paintings (random placement)
- **Sizes:** 1×1, 1×2, 2×1, 2×2, 4×2, 4×3, 4×4
- **Use:** Wall decoration

**Item Plaques**
- **Recipe:** 4 Sticks (sides) + 4 metal (corners)+ leather (center)
- **Function:** Display items on walls
- **Rotation:** 8 positions (45° increments)
- **Use:** Item display, maps, decoration

**Armor Stand**
- **Recipe:** 6 Sticks + Stone Slab
- **Function:** Display armor and items
- **Poses:** Adjustable (via tools)
- **Use:** Decoration, armor storage display

---

### **5.6 Plants & Agriculture**

#### **Crops**

**Wheat**
- **Seeds:** From breaking tall grass
- **Growth:** 8 stages, requires farmland + water
- **Harvest:** Wheat (bread ingredient)
- **Growth Time:** 60 minutes (base)

**Carrots**
- **Seeds:** Occasionally from zombies, villages
- **Growth:** 8 stages
- **Harvest:** 1-4 carrots
- **Use:** Food, breeding pigs

**Potatoes**
- **Seeds:** Occasionally from zombies, villages
- **Growth:** 8 stages
- **Harvest:** 1-4 potatoes, rarely poisonous potato
- **Use:** Baked for food

- **Seeds:** Village farms, dungeon chests
- **Growth:** 4 stages
- **Harvest:** Beetroot + seeds
- **Use:** Food, crafting

**Sugar Cane**
- **Source:** River/beach banks
- **Growth:** 3 blocks tall, requires water adjacent
- **Harvest:** Bottom block regrows
- **Use:** Sugar, paper

**Cactus**
- **Source:** Desert biomes
- **Growth:** 3 blocks tall, damages touching entities
- **Harvest:** Break any block, upper blocks drop
- **Use:** Dye (green), traps

**Bamboo**
- **Source:** Jungle biomes
- **Growth:** 12-16 blocks tall, fast-growing
- **Harvest:** Break any block
- **Use:** Scaffolding, sticks, decoration

**Cocoa**
- **Source:** Jungle biomes
- **Growth:** Grows on jungle log sides, 3 stages
- **Harvest:** Cocoa beans
- **Use:** Cookies, brown dye

**Berries (Sweet Berry Bush)**
- **Source:** Taiga biomes
- **Growth:** 4 stages, damages when walked through (mature)
- **Harvest:** Sweet berries (food)
- **Use:** Food, breeding foxes

**Blueberries (Blueberry Bush)**
- **Source:** Forest biome
- **Growth:** 4 stages, damages when walked through (mature)
- **Harvest:** Blueberries (food)
- **Use:** Food, breeding

#### **Flowers & Decorative Plants**

**Common Flowers** (found in various biomes)
- Dandelion (yellow dye, Essence+)
- Poppy (red dye)
- Blue Orchid (light blue dye, Magic+)
- Allium (magenta dye, Magic+)
- Azure Bluet (light gray dye)
- Tulips: Red, Orange, White, Pink (respective dyes)
- Oxeye Daisy (light gray dye)
- Cornflower (blue dye)
- Lily of the Valley (white dye, Magic++)

**Special Flowers**
- Sunflower (2 blocks tall, yellow dye, faces east, Essence+)
- Lilac (2 blocks tall, magenta dye, Magic+)
- Rose Bush (2 blocks tall, red dye)
- Peony (2 blocks tall, pink dye, Essence+)
- Hibiscus

**Magical Flowers** (rare, specific biomes)
- **Luminum Flower:** Tythus only, glows (light level 7), Magic+++ and Essence++
- **Moonflower:** Night-blooming, soft glow (light level 5), Magic++
- **Firebloom:** Desert/Badlands, warm glow (light level 6), Fire resistance aura
- **Frost Lily:** Mountains/Tundra, cold aura, Magic+

**Essence/Magic Radiation:**
- Low (+): Single flower, small radius (2 blocks)
- Medium (++): Strong flower or cluster, medium radius (4 blocks)
- High (+++): Rare magical flowers, large radius (6 blocks)
- Stacking: Multiple flowers in area accumulate effects

#### **Mushrooms**

**Red Mushroom**
- **Growth:** Low light (< 12), spreads to nearby blocks
- **Giant Form:** Can be grown into giant mushroom with bone meal
- **Use:** Mushroom stew, suspicious stew

**Brown Mushroom**
- **Growth:** Low light (< 12), spreads to nearby blocks
- **Giant Form:** Can be grown into giant mushroom
- **Use:** Mushroom stew, suspicious stew

**Giant Mushrooms**
- **Growth:** Bone meal on mushroom in open space
- **Blocks:** Mushroom block (all faces), Mushroom stem
- **Harvest:** Drops mushrooms when broken
- **Use:** Building, mushroom farming

#### **Grasses & Ground Cover**

**Tall Grass**
- **Types:** Short grass (1 block), Tall grass (2 blocks)
- **Variants:** Normal, Fern
- **Drop:** Seeds (wheat), occasionally flowers
- **Spread:** Bone meal on grass blocks
- **Essence:** Low Essence radiation (cumulative in dense areas)

**Sea Grass**
- **Growth:** Underwater, requires light
- **Variants:** Short, Tall
- **Use:** Decoration, turtle breeding

**Kelp**
- **Growth:** Underwater column, grows upward
- **Harvest:** Break any block, upper blocks drop
- **Use:** Dried kelp (food, fuel), decoration

**Lily Pad**
- **Source:** Swamp water surfaces
- **Function:** Walkable, floats on water
- **Use:** Water crossing without boats

#### **Trees & Saplings**

Each tree species has:
- **Sapling:** Grows into tree with time/bone meal
- **Log:** Main trunk block (bark texture)
- **Leaves:** Foliage block, decays without log nearby
- **Special Drops:** Some trees drop fruit/special items

**Oak Tree**
- **Biomes:** Plains, Forest
- **Sapling Drop:** 5% from leaves
- **Special:** Occasionally drops apples
- **Essence Radiation:** Medium (logs and leaves)

**Birch Tree**
- **Biomes:** Forest, Birch Forest
- **Appearance:** White bark
- **Essence Radiation:** Medium

**Spruce Tree**
- **Biomes:** Taiga, Mountains
- **Appearance:** Dark, conical shape
- **Essence Radiation:** Low

**Jungle Tree**
- **Biomes:** Jungle
- **Size:** Massive (2×2 trunk possible)
- **Special:** Cocoa grows on sides
- **Essence Radiation:** High

**Cypress Tree**
- **Biomes:** Bayou
- **Appearance:** Dark, red, layered canonpy
- **Essence Radiation:** Low

**Dark Oak Tree**
- **Biomes:** Forest (dark forest)
- **Size:** Always 2×2 trunk
- **Special:** Apples, mushrooms nearby
- **Essence Radiation:** Very low

**Willow Tree**
- **Biomes:** Swamp
- **Appearance:** Drooping branches
- **Essence Radiation:** Very high (swamp atmosphere)

**Sakura Tree** (rare)
- **Biomes:** Special cherry grove (rare)
- **Appearance:** Pink blossoms
- **Special:** Blossom particles
- **Magic Radiation:** Medium
- **Essence Radiation:** High

**Rubber Tree** (rare)

---

## **6 — Tools, Weapons & Armor**

### **6.1 Tool Tiers & Materials**

**Tier 1: Wood**
- **Durability:** 59 uses
- **Speed:** 2× hand speed
- **Harvest Level:** Basic (wood, stone, soft materials)
- **Crafting:** Planks + Sticks

**Tier 2: Stone**
- **Durability:** 131 uses
- **Speed:** 4× hand speed
- **Harvest Level:** Stone, iron ore
- **Crafting:** Cobblestone + Sticks

**Tier 3: Copper/Tin/Bronze**
- **Durability:** 210 uses (Bronze: 250)
- **Speed:** 5× hand speed
- **Harvest Level:** Iron, gold, gems
- **Crafting:** Ingots + Sticks
- **Special:** Bronze slightly better stats

**Tier 4: Iron**
- **Durability:** 250 uses
- **Speed:** 6× hand speed
- **Harvest Level:** Diamond, redstone
- **Crafting:** Iron Ingots + Sticks

**Tier 5: Steel**
- **Durability:** 500 uses
- **Speed:** 7× hand speed
- **Harvest Level:** Obsidian, all ores
- **Crafting:** Steel Ingots + Sticks
- **Special:** Magnetic property

**Tier 6: Diamond**
- **Durability:** 1561 uses
- **Speed:** 8× hand speed
- **Harvest Level:** All blocks
- **Crafting:** Diamonds + Sticks
- **Enchantability:** High

**Tier 7: Gold**
- **Durability:** 32 uses (very low!)
- **Speed:** 12× hand speed (fastest)
- **Harvest Level:** Same as wood
- **Enchantability:** Very high
- **Use:** Speed + enchantments, not longevity

**Tier 8: Exotic/Endgame**
- **Materials:** Platinum, Titanium, Malachite, Orichalcum
- **Durability:** 2000+ uses
- **Speed:** 9-10× hand speed
- **Special Properties:** Dimension-specific bonuses
- **Crafting:** Rare materials from other dimensions

### **6.2 Tool Types**

**Pickaxe**
- **Function:** Mine stone, ores, metal blocks
- **Efficiency:** Required for proper ore drops
- **Recipe:** 3 Material + 2 Sticks (T-shape)

**Axe**
- **Function:** Chop wood, strip logs
- **Efficiency:** Faster wood breaking
- **Weapon:** Can be used in combat (moderate damage)
- **Recipe:** 3 Material + 2 Sticks (L-shape)

**Shovel**
- **Function:** Dig dirt, sand, gravel, snow
- **Special:** Create grass paths
- **Recipe:** 1 Material + 2 Sticks (vertical)

**Sythe**
- **Function:** Till dirt into farmland
- **Special:** Faster leaf breaking
- **Recipe:** 2 Material + 2 Sticks (L-shape)

**Shears**
- **Function:** Harvest wool, leaves, vines without breaking
- **Durability:** 238 uses
- **Recipe:** 2 Iron Ingots
- **Use:** Sheep shearing, leaf collection, tripwire cutting

**Fishing Rod**
- **Function:** Catch fish, pull entities
- **Durability:** 64 uses
- **Recipe:** 3 Sticks + 2 String
- **Catch:** Fish (food), treasure (rare), junk
- **Enchantable:** Luck of the Sea, Lure

**Mining Drill** (late-game tool)
- **Function:** Fast mining in 3×3 area
- **Power:** Requires battery/fuel
- **Recipe:** 4 Steel + 2 Diamonds + 2 Cobalt + Motor
- **Speed:** 15× hand speed
- **Durability:** Infinite (requires fuel/charge)
- **Fuel:** Anthracite, coal, battery

**Sledgehammer** (area mining)
- **Function:** Mines 3×3 area (slower per block)
- **Tier:** Steel, Diamond
- **Recipe:** 5 Material + 2 Long Sticks
- **Speed:** Per-block slower than pickaxe
- **Use:** Bulk mining, quarrying
- **Durability:** 2× standard pickaxe

**Wrench** (utility tool)
- **Function:** Rotate blocks, configure contraptions
- **Recipe:** 3 Iron Ingots
- **Use:** Adjust pistons,
- **Durability:** Infinite (doesn't break)

6.3 Weapons
WeaponTiers AvailableDamageAttack SpeedRangeSpecial PropertiesRecipe PatternSwordAllTier-based 4-11Fast (1.6/sec)MeleeSweep attack1 Material + Stick (vertical)AxeAllTier-based 6-13Slow (0.8/sec)MeleeCan be used as tool3 Material + 2 Sticks (L-shape)SpearWood, Stone, Bronze, Iron, Steel, Diamond3-9Medium (1.2/sec)3 blocksThrowable1 Material + 2 Sticks (vertical)BowWood + String4-9 (arrow-based)Charge-up15-50 blocksRequires arrows3 Sticks + 3 StringCrossbowIron/Steel mechanism6-11Slow reload20-60 blocksPre-loadable, higher damageComplex (Advanced Workbench)Throwing KnifeBronze, Iron, Steel2-5Instant8-15 blocksStackable, recoverable1 Material (shapeless)TridentSpecial (found/crafted)9Medium (1.1/sec)Melee/20 blocksThrowable, returns if enchantedPrismarine + Diamond + Orichalcum
Weapon Enchantments:

Edge (I-X): +1.25 damage per level
Smite (I-X): +2.5 damage vs undead
Bane of Arachnae (I-X): +2.5 damage vs spiders
Kick (I-V): Increased knockback
Flames (I-): Sets target on fire
Looting (I-III): Increased mob drops
Wide Slash (I-III): Increased sweep attack damage (swords)
Compound (I-V): Increased arrow damage (bows)
Piercer (I-IV): Arrows pass through entities (crossbows)

6.4 Armor System
Armor Tiers
MaterialHelmetChestplateLeggingsBootsTotal ProtectionDurability MultiplierEnchantabilitySpecial PropertiesLeather132171× (base: 55)15Dyeable, cheapCopper2541121.5×12Oxidizes over timeBronze2652152×14Corrosion resistantIron2652152×9Standard protectionSteel3762183×10Magnetic, high durabilityGold2531110.5×25High enchantability, fragileDiamond3863204×10High protectionSilver2542131.8×22Magical defense, undead protectionOrichalcum3763193.5×18Magical conductivityPlatinum3873214.5×12Aethus only, lightweightTitanium4973235×8Hathus only, heat resistant
Protection Values: Each point of armor reduces damage by 4%, with diminishing returns above 20 points.
Armor Crafting Patterns:

- **Platinum**
**Titanium**
-**Ml:ac it**
Helmet: 5 materials (upside-down U shape)
Chestplate: 8 materials (body shape)
Leggings: 7 materials (legs shape)
Boots: 4 materials (boot shape)

Armor Enchantments
EnchantmentMax LevelEffectCompatible PiecesProtectionIVGeneral damage reduction (+4% per level)AllFire ProtectionIVFire/lava damage reductionAllBlast ProtectionIVExplosion damage reductionAllProjectile ProtectionIVArrow/projectile damage reductionAllFeather FallingIVFall damage reductionBoots onlyDepth StriderIIIFaster underwater movementBoots onlyFrost WalkerIIFreezes water beneath feetBoots onlyRespirationIIIExtended underwater breathingHelmet onlyAqua AffinityIFaster mining underwaterHelmet onlyThornsIIIReflects damage to attackersAllSoul SpeedIIIFaster movement on soul sandBoots onlyUnbreakingIIIIncreased durabilityAllMendingIRepairs with XPAll
Special Armor Pieces
Cloaks/Capes

Recipe: 4 Wool/Leather + 2 String
Slot: Back (cosmetic slot)
Function:

Purely cosmetic by default
Can be enchanted for minor effects (Elytra-like glide with special variant)
Dyeable
Banner patterns transferable



Climbing Boots

Recipe: Leather Boots + 4 Iron Ingots + 2 Hooks
Function: Climb walls slowly (like ladders)
Speed: Half normal movement speed on walls
Durability: Drains faster when climbing

Diving Helmet

Recipe: Iron Helmet + 2 Glass Panes + Copper Tube
Function: Extended underwater breathing (5 minutes)
Visual: Bubble effect while worn
Crafting: Requires Advanced Workbench


7 — Food & Cooking
7.1 Food Items
Food ItemHunger RestoredSaturationCooking RequiredSource/RecipeSpecial EffectsApple42.4NoOak/Dark Oak trees-Bread56.0Yes (crafting)3 Wheat-Cookie20.4Yes (crafting)Wheat + Cocoa Beans-Cake14 (7 slices)2.8Yes (crafting)Wheat + Sugar + Egg + MilkPlaceableRaw Beef31.8NoCow-Steak812.8Yes (kiln)Raw Beef-Raw Pork31.8NoPig-Cooked Pork812.8Yes (kiln)Raw Pork-Raw Chicken21.2NoChicken30% poison chanceCooked Chicken67.2Yes (kiln)Raw Chicken-Raw Mutton21.2NoSheep-Cooked Mutton69.6Yes (kiln)Raw Mutton-Raw Fish20.4NoFishing-Cooked Fish56.0Yes (kiln)Raw Fish-Mushroom Stew67.2Yes (crafting)Red + Brown Mushroom + BowlReturns bowlRabbit Stew1012.0Yes (crafting)Rabbit + Carrot + Potato + Mushroom + BowlBest foodSweet Berries20.4NoBerry bushes-Golden Apple49.6Yes (crafting)Apple + 8 Gold IngotsRegeneration II (5s), Absorption (2min)Enchanted Golden Apple49.6NoFound in lootRegeneration V (30s), Resistance (5min), Fire Resistance (5min), Absorption IV (2min)
7.2 Brewing & Potions
Brewing Stand

Recipe: 3 Cobblestone + 1 Blaze Rod
Function: Create potions from ingredients
Fuel: Blaze Powder (20 potions per powder)
Process: Water Bottle + Nether Wart → Awkward Potion + Ingredient → Effect Potion

Base Potions
PotionIngredientEffect DurationEffect StrengthModifiersHealingGlistering MelonInstantRestores 4 HPGlowstone: +4 HPRegenerationGhast Tear0:45Regen IRedstone: 1:30, Glowstone: Regen II (0:22)StrengthBlaze Powder3:00+3 damageRedstone: 8:00, Glowstone: +6 damage (1:30)SwiftnessSugar3:00+20% speedRedstone: 8:00, Glowstone: +40% speed (1:30)Fire ResistanceMagma Cream3:00Immunity to fireRedstone: 8:00Night VisionGolden Carrot3:00See in darkRedstone: 8:00InvisibilityFermented Spider Eye (on Night Vision)3:00InvisibleRedstone: 8:00Water BreathingPufferfish3:00Breathe underwaterRedstone: 8:00LeapingRabbit's Foot3:00+0.5 jump heightRedstone: 8:00, Glowstone: +1 jump (1:30)Slow FallingPhantom Membrane1:30Slow descentRedstone: 4:00
Negative Potions (Fermented Spider Eye modifier)

Poison, Weakness, Slowness, Harming


8 — Mobs & AI
8.1 Complete Mob Catalog
Passive Mobs
MobDimensionBiomesHealthDropsBehaviorSpecial PropertiesPigEithusAll10Raw Pork (1-3)Wanders, oinksRideable with saddle, carrot on stickCowEithusAll10Raw Beef (1-3), Leather (0-2)Wanders, moosMilkable with bucketSheepEithusAll8Mutton (1-2), Wool (1)Wanders, eats grassShearable, 16 colorsChickenEithusAll4Raw Chicken (1), Feathers (0-2)Wanders, lays eggsFalls slowlyRabbitEithusPlains, Forest3Rabbit Hide (0-1), Raw Rabbit (0-1), Rabbit's Foot (rare)Hops, flees playersFast movementElkEithusForest, Taiga12Venison (2-4), Fur Pelt (1-2), Antlers (1)Wanders, grazesDrops berries it eatsBlue JayEithusForest, Taiga, Plains3Feathers (1-2), Bird Meat (0-1)Flies, singsAmbient soundsRobinEithusForest, Taiga, Plains3Feathers (1-2), Bird Meat (0-1)Flies, singsAmbient soundsHeronEithusRivers, Lakes, Ponds8Feathers (2-3), Bird Meat (1-2), Heron's Foot (rare)Wades, hunts fishCatches fish from waterGlowbatEithusCaves4Luminum Dust (1-2)Flies, hangs from ceilingEmits light (level 5)VillagerEithusVillages (any biome)20Nothing (ethically)Trades, sleeps, worksTrading system, professionsWandererEithusAll (rare spawn)20Tools, Items, Gems (rare), Mini-Diskette (very rare)Wanders, trades, despawnsRare mobile traderStrangerAllAll (very rare)20Tools, Torches, Mini-Diskette (rare)Stalks player from distance, disappears when observedCreepy, harmlessAethilligerAethusVillages24Feathers (3-5), Paper (1-2), Gems (rare)Trades, fliesSky dimension tradersPegasusAethusFloating Plains30Feathers (4-6)Flies, wandersTameable with Silver Apples, rideable
Neutral Mobs
MobDimensionBiomesHealthDamageDropsBehaviorAggression TriggerWolfEithusForest, Taiga, Mountains8 (40 tamed)4 (wild), 8 (tamed)Fur Pelt (0-2)Hunts sheep/cows, pack behaviorWhen attackedCoyoteEithusPlains, Desert, Forest103Fur Pelt (1-2)Hunts chickens, rabbits, heronsWhen attackedFoxEithusForest, Plains, Mountains102Fur Pelt (1-2)Hunts chickens, rabbits, small birdsFlees when attackedFarlanderAllAll407Opals (1-2), Crystal Eye (rare), Waystones (rare)Teleports randomlyWhen looked at in eyesForsaken SoulHathusSoul Plains306Soul Crystal (1-3), Mini-Diskette (rare)Wanders, neutralAttacks if not wearing Silver/Gold armorLost SoulLuthusUnderdark255Soul Crystal (1-2), Mini-Diskette (rare)Wanders, whispersAttacks if not wearing gemstone armorValcereonAethusSilver Castle5012Armor pieces, Tools, Weapons, Gems, Feathers (5-8), Wings (rare)Guards castle, fliesWhen attacked or castle entered
Hostile Mobs
MobDimensionBiomesHealthDamageDropsBehaviorSpecial AttacksZombieEithusAll (night/dark)203-5Rotten Flesh (0-2), Ash (0-1), Weak Tools (rare)Shambles toward player, burns in daylight-SkeletonEithusAll (night/dark)202-4 (arrow)Bones (0-2), Arrows (0-2), Bow (rare)Shoots arrows, burns in daylightRanged combatSpiderEithusAll (night/dark)162-3String (0-2), Spider Eye (0-1)Climbs walls, jumpsWall climbingCave SpiderEithusCaves, Mineshafts122 + poisonString (0-2), Spider Eye (0-1)Climbs walls, poisonsPoison effectCreeperEithusAll (night/dark)2049 (explosion)Gunpowder (0-2), Music Disc (if killed by skeleton)Silent approach, explodes near playerExplosion (destroys blocks)WitchEithusSwamp huts26Varies (potions)Glass Bottles, Glowstone, Redstone, Sugar, Sticks, Gunpowder, Spider EyesThrows potions, drinks healing potionsRanged potion attacksKoboldEithusUnderground Dungeons, Mineshafts164Ores (various), Mining Tools (damaged)Mines ores, attacks intrudersPickaxe attacksUndead KnightEithusAbandoned Castles408-10Armor pieces, Weapons, Rare Items, GemsGuards castle, patrolsHeavy armor, strong attacksDemonoidHathusWastelands306Rotten Flesh (1-3), Bones (0-2), Heart (rare)Aggressive, fast-TallonithHathusWastelands254 (fireball)Bones (1-2), Tears of Tallonith (1)Shoots fireballsRanged fire attacksStalkerLuthusUnderdark358Decayed Flesh (1-2), Crystal Eye (rare), Mini-Diskette (very rare)Stalks in darkness, lures with soundsMimics familiar sounds
8.2 AI Behavior Systems
Pathfinding:

A* algorithm with cost maps
Dynamic obstacle avoidance
Vertical pathfinding (ladders, vines, stairs)
Jump height consideration (1-2 blocks based on mob)
Swimming/flying pathing for aquatic/aerial mobs

Mob States:

Idle: Standing, looking around
Wander: Random walking within home range
Flee: Running from threats
Attack: Pursuing and attacking target
Patrol: Following predefined path (guards)
Sleep: Inactive during certain times (villagers)
Work: Performing job tasks (villagers)
Trade: Interacting with player (traders)

Group Behavior:

Pack Hunting: Wolves hunt in coordinated groups
Herding: Cows, sheep cluster together
Flocking: Birds fly in groups
Swarming: Spiders can call reinforcements

Environmental Responses:

Day/Night: Hostile mobs spawn/burn
Weather: Some mobs seek shelter in rain
Temperature: Cold slows some mobs, heat affects others
Light Levels: Mob spawning tied to darkness
Player Proximity: Mobs activate within range


9 — Dimensions & Portals
9.1 Dimension Catalog
DimensionAccess MethodY RangeBiome CountKey FeaturesDifficultyEithus (Overworld)Starting dimension-128 to 25515+Full biome diversity, villages, all resourcesNormalLuthus (Deep Dark)Ancient Portal (rare ruins)-64 to 1923Perpetual darkness, horror mobs, rare lootVery HardAethus (Skylands)Sky Portal (high altitude)128 to 3846Floating islands, sky castles, Platinum oreHardHathus (Nether)Obsidian Portal (found inactive)-128 to 1285Lava oceans, fire-resistant mobs, unique oresHardTythus (Twilight)Mystic Ritual (rare ruins)0 to 2568Dream-like, perpetual twilight, magical floraMedium-Hard
9.2 Portal Construction & Activation
Luthus Portal
Construction:

Frame: 4×5 Ancient Stone blocks (found in rare ruins)
Size: 2×3 inner portal space
Location: Must be built at ruins with portal frame

Activation:

Place 4 Soul Crystals in frame corners
Use Black Diamond on center bottom block
Portal ignites with dark purple effect

Properties:

One-way initially (return portal auto-generates)
Unstable: Can close if not maintained with Essence
Coordinates match 1:1 with Overworld

Aethus Portal
Construction:

Frame: 4×5 Quartz Blocks
Size: 2×3 inner portal space
Location: Must be at Y > 180 (high altitude)
Base: Must have 3×3 platform of Gold Blocks beneath

Activation:

Place Rose Quartz in each corner
Use Diamond on center block
Right-click with Feather
Portal ignites with white-blue effect

Properties:

Bidirectional
Coordinates: 1:1 with Overworld (but offset vertically)
Stable once activated

Hathus Portal
Construction:

Frame: Found pre-built but inactive
Made of: Obsidian (4×5)
Location: Y < -64 in Eithus (deep caves)

Activation:

Collect Fire Opal
Use Fire Opal on bottom center block
Pour Lava Bucket in center
Portal ignites with orange-red effect

Properties:

Bidirectional
Coordinates: 8:1 ratio (1 block Hathus = 8 blocks Eithus)
Very stable
Ambient heat near portal

Tythus Portal
Construction:

Not built—accessed via Altar
Location: Ancient ruins with crumbling altar

Activation:

Repair altar with: 4 Moonstone + 4 Amethyst + 1 Emerald
Place altar at ruins
Activate at specific times: Dawn or Dusk
Use Moonstone on altar
Portal opens above altar (particle effect)

Properties:

Temporary (lasts 10 minutes)
One-way entry (exit portals spawn naturally)
Coordinates: Random offset from entry point
Time dilation: 2:1 (time moves slower in Tythus)

9.3 Dimension-Specific Content
Luthus (Deep Dark)
Biomes:

Underdark Caverns (main)
Void Wastes (empty, echoey)
Whispering Depths (ambient sounds)

Blocks:

Shadowstone (black stone, absorbs light)
Void Grass (dark purple grass-like block)
Crystal formations (decorative)
Essence Pools (liquid Essence)

Resources:

Soul Crystals (common, used in magic)
Black Diamonds (very rare)
Enhanced Essence extraction
Rare enchanted gear in chests

Challenges:

Permanent darkness (torches dimmed to 50%)
Stalker mobs
