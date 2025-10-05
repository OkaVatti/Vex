# Vex — Complete Design Document (Rewritten & Expanded)

*Voxel survival crafted in Kotlin — Beta 1.7.3 spirit, modern ergonomics, deep systems (geology, electronics, magic).*

**Version:** 2.0 (integrates Electronics / Cobalt subsystems & VexScript integration)
**Last updated:** 2025-10-05
**Audience:** designers, implementers, modders, scripters

---

## Vision statement

Recreate the tactile lo-fi charm and emergent sandbox of Minecraft Beta 1.7.3 while adding modern QoL, richer geology/ecology, a broad electronics ecosystem (Cobalt), and an approachable, powerful scripting surface (VexScript + Lua/KotlinScript bridges). Implementation target: **Kotlin** for a safe, performant JVM runtime with coroutine-based concurrency and an extensible host API.

---

# Contents

1. Core design principles
2. Technical architecture (engine, networking, persistence)
3. World & generation (verticality, strata, biomes, structures)
4. Blocks, materials, and items (high-level, electronics blocks & items included)
5. The Cobalt system (material, signal model, gameplay mechanics)
6. Electronics and device ecosystem (cables, chassis, computers, microcontrollers)
7. Scripting & runtime integration (VexScript library, Host API, security)
8. Game systems (power, thermal, magic/essence, economy & balance)
9. Content lists & registries (how to use blockList.md / itemList.md)
10. Implementation notes for Kotlin (APIs, serialization, performance)
11. Example scripts & blueprints (VexScript)
12. Tuning constants (default values)
13. Next steps & deliverables

---

# 1 — Core design principles

## 1.1 Aesthetic & UI

* **Visual:** Low-resolution textures (16×16 default), simple low-poly models, stylized dust/particle effects. Optional smoothing/AO and raytracing toggles.
* **UI:** Beta-inspired HUD with modern conveniences: responsive recipe book, tooltips, UI scaling, controller/touch support.
* **Audio:** Biome ambient loops, minimalistic sound design; electronics produce subtle hums/pulses.

## 1.2 Gameplay pillars

* **Emergent sandbox:** systems interact (machines, magic, mobs) producing player-driven goals.
* **Clear progression:** recipes and component tiers gradually extend complexity (cobalt/PCBs → microcontrollers → servers).
* **Reward exploration:** vertical strata and special biomes contain unique ores (Cobalt, Luminum, Black Diamond).
* **Creative building:** predictable block rules (incl. slopes, slabs, stairs) and a rich set of functional blocks.
* **Mod-friendly:** scripting + typed host API; modders can add devices, cables, chips and new Cobalt alloys.

---

# 2 — Technical architecture

## 2.1 Stack

* **Language:** Kotlin (JVM). Coroutines for concurrency.
* **Rendering:** LWJGL + Vulkan (modular render backends). Greedy meshing for opaque blocks; separate translucent pass.
* **Networking:** Ktor (WebSocket/HTTP) + UDP for LAN discovery. Protocol well-defined; supports headless server.
* **Persistence:** chunked region files, compress with LZ4/ZSTD. Optional protobuf metadata for devices and scripts.
* **Scripting & modding:** VexScript (primary DSL), bridges to Lua (LuaJ) and KotlinScript (KTS). Sandboxed, capability-gated Host API.
* **Build & tooling:** Gradle Kotlin DSL, automated asset pipeline, unit + integration tests.

## 2.2 Platforms

* Windows, Linux (x64/ARM64), macOS, experimental Android. Headless server support for Linux.

---

# 3 — World & generation (expanded)

## 3.1 Vertical range & chunk model

* **Y range:** **-128 .. 255** (384 blocks total). Sections of 16 height → 24 sections per chunk column.
* **Chunk:** 16×384×16. Sections lazily allocated; store block data, light, biome per section.
* **Region:** 32×32 chunk columns per region file; incremental saves & snapshot support.

## 3.2 Generation pipeline (phased)

1. **Biome map:** Temperature & humidity sampling, river carving, edge blending.
2. **Terrain shape:** FBM + ridged multifractals for mountains; continental shelf rules for oceans.
3. **Base terrain:** strata fill, bedrock scatter, ore vein seeding.
4. **Caves:** 3D Perlin worms + large cavern placement; underwater cave generation.
5. **Decoration:** trees, structures, ruins, villages, lakes.
6. **Lighting & meshing:** sky & block light then meshing passes.

## 3.3 Geological strata & resource placement (high detail)

* Deeper zones house rarer resources:

    * **Deep (Y -128..-96):** Anthracite, Malachite, Black Diamond (very rare).
    * **Lower (Y -96..-48):** Cobalt, Gold, Lithium with vein-level rarity.
    * **Middle (Y -48..0):** Iron, Copper, Tin, Silver; gems occasional.
    * **Upper (Y 0..48):** Copper, Tin, surface gems (biome dependent).
* Ore distribution tuned for progression—players must upgrade tools and create infrastructure (crucible, blast kiln).

## 3.4 Biomes & structures

* Extensive set: Plains, Forest (oak/birch), Taiga, Tundra, Jungle, Bayou, Badlands, Ocean variants, Mushroom Grotto, and dimension biomes (Aethus, Luthus etc).
* Structures: villages, mineshafts, dungeons, temples, abandoned castles, ruins. Portal frames for dimensions (Luthus/Aethus/Hathus/Tythus) found in special ruins or built via rituals.

---

# 4 — Blocks, materials, and items (summary + electronics focus)

A comprehensive **block** and **item** catalog exists in the files created earlier:

* `/mnt/data/blockList.md` — canonical block registry overview
* `/mnt/data/itemList.md` — canonical item registry overview

> Use these as the master catalogs. Below are expanded specifics needed by the gameplay systems introduced earlier.

## 4.1 Materials & block families

* **Natural materials:** stone families, ores, sand/silt, soils, wood families (multiple species), vegetation.
* **Processed materials:** ingots, dusts, alloys.
* **Electronics family:** Cobalt Dust, Cobalt Extender, Cobalt Block, logic gates, cables (Fiber/Network/Universal), chassis (metal/gem/plastic), PCBs (Mini→XL), microcontroller chips (8→128-bit emulated).
* **Lighting:** Salt, Luminum, Colourium lamps with cobalt control.

## 4.2 Functional blocks (expanded)

* **Crafting/processing:** Workbench (3×3), Advanced Workbench (5×5), Kiln, Blast Kiln, Crucible, Anvil, Toolbench.
* **Electronics blocks:** See §6 for full electronics ecosystem. All electronics blocks are block-entities that expose ports and metadata to the Host API.
* **Magic blocks:** Mystic Altar, Essence Extractor, Runic Enclosures—interact with Cobalt via mana/flux channels.

---

# 5 — The Cobalt system (core new system)

Cobalt is the new canonical conductor and a hybrid material that conducts **electricity** *and* **mana (flux)**. It replaces redstone in design and adds expanded physical geometry and magical interaction.

## 5.1 Placement & geometry

* Placeable as **Cobalt Dust** on block faces, inside blocks (embedded), on slabs (horizontal/vertical), stairs (all orientations), slopes (triangular, sloped blocks), and underwater.
* **Layering:** up to **3 layers per block** (upper/inner/lower) to support multi-layer PCB-like routing.
* **Vias & connectors:** Vias connect layers and allow vertical routing inside a block volume.

## 5.2 Signal model

* **Domains**:

    * **Digital:** boolean ON/OFF (interpreted from analog via `digital_threshold`).
    * **Analog:** integer range `0..ANALOG_MAX` (default 15).
    * **Frequency (RF):** multiplexed pulse channels (max RF channels per trace).
    * **Flux:** mana units (0..flux buffer) with separate attenuation.
* **Propagation:**

    * Signals propagate across segments with configurable speed and attenuation; repeaters/extenders restore/amplify.
    * Default tuned values are in §12 (e.g., `ANALOG_MAX = 15`, `MAX_SEGMENTS_BEFORE_REPEATER = 64`).

## 5.3 Interaction with magic (flux)

* **Flux** travels on cobalt but attenuates faster than electricity (encourages local buffers).
* **Runic variants** of cobalt (crafted/enchanted) improve flux conduction or provide resonant behaviors.
* **Mana converters** convert flux ↔ electrical domain; **mana buffers** store flux for rituals or mana-powered devices.

## 5.4 Thermal & durability interactions

* Cobalt conducts heat — current draw and flux generation raise trace temperature.
* **Water cooling** reduces temperature (rewarding underwater/immersed designs).
* Overheat triggers event `:cobalt_overheat` and may melt/damage traces at `MELT_TEMP`.

---

# 6 — Electronics & device ecosystem (complete overview)

This section describes the playable electronics components, how they assemble, and the gameplay progression.

## 6.1 Cable & conduit types

* **Cable**: basic wire (1 channel). Cheap.
* **Thick Cable**: high-current rail (power).
* **Communication Cable**: RF multiplexing optimized.
* **Network Cable**: packetized, Ethernet-like for devices and servers.
* **Fiber Network Cable**: high throughput backbone, low attenuation.
* **Universal Cable**: power + data in one line (limited multiplexing).
* **Advanced Universal Cable**: QoS, multi-rail, high power throughput.

## 6.2 Chassis & device form-factors

* **Chassis sizes:** Mini, Small, Medium, Large, XL — in metal, plastic, gem variants.
* Chassis define PCB counts, cooling options, and mounting points for PSUs, NICs, daughterboards.
* **Racks:** 1U/2U/4U for server systems.

## 6.3 Computers & networking devices

* **Device classes:** Mini Computer → Personal Computer → Workstation → Server
* **Network devices:** Router, Advanced Router, Switch, Wireless Router, Wireless AP — each has port specs, throughput, and QoS features.

## 6.4 PCBs & components

* **PCBs:** Mini/Small/Medium/Large/XL and etched PCB variants (user designs).
* **Components:** resistors, capacitors, transistors, oscillators, LEDs, PROM/EEPROM, DRAMx, NVRAM.
* **Microcontroller tiers:** 8-bit → 128-bit (emulated), with memory tiers (PROM/EEPROM/DRAM/NVRAM) mapped to in-game items.

## 6.5 Power & thermal

* **PSUs:** Mini → Regular → Rack units with wattage tiers (25W → multi-kW).
* **Power rails:** 5V/12V/48V internal rails. Universal cables multiplex rails.
* **Thermal model:** heat per current tick, water cooling, fans and heat sinks. Overheat causes throttling and potential damage.

## 6.6 Tools & assembly pipeline

* **Tools:** Multimeter, Oscilloscope, Logic Analyzer, Flasher, PCB Etcher, Soldering Station.
* **Assembly:** design etched PCB → populate → solder → flash firmware → mount in chassis → connect cables → power and test.

---

# 7 — Scripting & runtime integration

Vex emphasizes a single, well-documented scripting language with bridges to Lua and KotlinScript.

## 7.1 VexScript (language highlights)

* **Syntax:** Ruby-like with Rust/Crystal inspirations. Immutable by default; `mut` for mutation. `fn` for functions; `def Type#method` for methods.
* **Types:** optional type annotations (`String`, `Int`, `Array<T>`, `Device`).
* **Async model:** `async fn` / `await` / `spawn`.
* **Events:** `Event.on(:cobalt_signal_change) do |ev| ... end`.
* **Capability checks:** scripts must declare `# VEX-CAPABILITIES: [...]` header and runtime enforces capabilities via `Host.Security`.

## 7.2 Host API (exposed to scripts)

* Host modules are exposed as: `Host.Cobalt`, `Host.Device`, `Host.Network`, `Host.Tools`, `Host.Security`, `Host.File`, `Host.Http`.
* Example Host functions (representative):

    * `Host.Cobalt.placeTrace(loc, face, layer, material, insulated)`
    * `Host.Cobalt.readSignal(loc, face, layer)` → `SignalSnapshot`
    * `DeviceHandle.io.writeAnalog(port, value)`; `DeviceHandle.net.sendPacket(...)`
    * `Host.Tools.attachOscilloscope(loc, face, layer)` → handle to capture waveform

(Full Kotlin interface file can be generated; refer to earlier API spec for exact signatures.)

## 7.3 VexScript standard library `electrics.vex`

* Bundled helpers: `Electrics.place_cobalt(...)`, `Electrics.read_trace(...)`, `Electrics.emit_pulse(...)`.
* Enforces capability checks before Host calls.

## 7.4 Security model

* **Capabilities**: `Cobalt.IO`, `Cobalt.Place`, `Cobalt.Admin`, `Device.Flash`, `Network.Send`, `Tools.Use`, `Explosives.Control`.
* Scripts without capability cannot perform sensitive actions (placing traces, controlling explosives, flashing firmware).
* Admins sign scripts or grant capabilities per world policy.

---

# 8 — Game systems — power, thermal, magic & economy

## 8.1 Power model

* Devices draw watts based on component list (CPU tier, DRAM count, NICs).
* Power routing via universal/thick cables; batteries & UPS for redundancy.
* PSUs have efficiency and may require redundancy for servers (N+1).

## 8.2 Thermal model

* Heat accrues with current usage and flux processing.
* Heat per CU per tick and water cooling constants tuned (see §12).
* Thermal throttling when heat > `OVERHEAT_TEMP` and destruction at `MELT_TEMP`.

## 8.3 Magic (Essence / Flux)

* Ambient Essence in biomes and in specific structures; extracted via Essence Extractor.
* **Flux** (mana units) is conducted via Cobalt with distinct attenuation.
* Magical runes and runic Cobalt variants allow ritual systems, magical gates, and flux-augmented devices.

## 8.4 Economy & progression

* Game progression tied to resource availability, manufacturing chain (crucible, blast kiln), and electronics complexity.
* Endgame: distributed servers, large fiber backbones, runic-cobalt networks and high-tier storage (EENVRAM).

---

# 9 — Content lists & registries

Use the canonical files produced earlier:

* `blockList.md` — all blocks, grouped and normalized (includes Cobalt family, devices, chassis, lamps).
* `itemList.md` — all items (components, chips, cables, tools).

**Registry guidance:**

* Implement block/item IDs with consistent namespacing (e.g., `vex:cobalt_dust`, `vex:mini_computer`, `vex:cobalt_extender`).
* Provide metadata for block entities (port lists, default signals, firmware tags).
* For complex items (PROM, DRAM modules) allow metadata NBT to record size and firmware.

---

# 10 — Implementation notes (Kotlin)

## 10.1 Runtime structure

* **Modules:** `world`, `render`, `net`, `device`, `cobalt`, `script`, `tools`, `persistence`.
* **Cobalt service** manages trace graph, signal scheduling, attenuation, thermal calculation, and event emission.
* **Device registry** holds `DeviceHandle` proxies exposed to VexScript; devices implement `io`, `net`, `power`, `hw`.
* **Script sandboxing:** run scripts in coroutine scopes with resource/time quotas; enforce capability checks via `Host.Security`.

## 10.2 Serialization & persistence

* **Trace state** (per-block layers, last signals, heat, enchantments) stored in chunk data. Save diffs for performance.
* **Device states** (inventory, firmware, ports) persisted with region files or separate device DB.

## 10.3 Performance & scaling

* **Propagation batching:** group signal updates per tick and short-circuit unchanged subgraphs.
* **Rate limits:** oscilloscope/logic analyzer captures expensive — throttle per-script & per-server.
* **Network offload:** complex routing and packet queueing handled in background threads (coroutines + bounded dispatchers).

---

# 11 — Examples & recipes (VexScript snippets)

### 11.1 Blink LED (VexScript)

```vex
# scripts/blink_led.vex
puts "Blink script loaded"
dev = Electrics.find_device("device-mini-1")
spawn do
  10.times do
    dev.io.writeAnalog("led_pin", 15)
    await Time.sleep(0.5)
    dev.io.writeAnalog("led_pin", 0)
    await Time.sleep(0.5)
  end
end
```

### 11.2 Flux guard (prevent vault unlocking on flux spike)

```vex
Event.on(:flux_threshold_crossed) do |ev|
  if ev.loc == vec3(100,64,200) && ev.new_flux >= 50
    Device.find("vault-lock").io.writeDigital("lock", true)
    Server.broadcast("Vault lock forced due to flux spike")
  end
end
```

### 11.3 Auto-extend long cobalt run

```vex
path = [...] # array of vec3
Electrics.auto_extend_path(path)
```

---

# 12 — Tuning constants (server defaults & rationale)

These are the tuned defaults recommended for balanced gameplay & server performance. They are *configurable* by server admins.

```yaml
cobalt:
  analog_max: 15
  max_segments_before_repeater: 64
  propagation_speed_segments_per_tick: 8
  layers_per_block: 3
  analog_attenuation_segments_per_drop: 16
  flux_attenuation_segments_per_drop: 4
  default_flux_buffer: 100
  default_extender_boost_analog: 15
  default_extender_boost_flux: 40
  digital_threshold: 1
  trace_safe_current_cu: 10
  thick_cable_safe_current_cu: 100
  thermal_overheat_temp: 100.0
  thermal_melt_temp: 250.0
  heat_per_cu_per_tick: 0.01
  water_cooling_per_tick: 0.5
  max_frequency_channels_per_trace: 8
  max_rf_rate_hz: 40
```

**Rationale**: `ANALOG_MAX=15` provides 4-bit resolution (good for lighting and simple analog controls) while keeping compute light. `MAX_SEGMENTS_BEFORE_REPEATER=64` encourages infrastructure without being onerous. Flux decay faster to keep magic local and valuable.

---

# 13 — Next steps & deliverables I can produce now

Pick any of the following (I will produce instantly):

* **A. `HostApi.kt`** — full Kotlin interface file with all Host/Cobalt/Device/Network/Tools APIs.
* **B. `VexScript` standard library package** (electrics.vex + examples).
* **C. `blocks.json` & `items.json`** — machine-readable registries derived from `blockList.md` & `itemList.md` (IDs, categories, basic properties).
* **D. Full ANTLR grammar `VexScript.g4`** or Kotlin `AstBuilderVisitor` (parser → AST).
* **E. Test harness (Kotlin)** — minimal in-memory Host implementation so you can run VexScript test scripts locally.

---

## Final notes

This document preserves the original Beta-style vision while integrating the expanded electronics, cobalt, and scripting subsystems you requested. It is intentionally prescriptive where systems intersect (e.g., Cobalt signal semantics, scripting capabilities, thermal/power model), yet configurable for server operators.

Tell me which deliverable above to produce next and I’ll create it immediately (Kotlin interface, registry JSON, VexScript lib, grammar, or test harness).
