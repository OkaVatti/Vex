# Cobalt — Material & Functional Blocks (Electricity × Magic)

**Version:** 1.0
**Last updated:** 2025-10-04
**Audience:** game designers, modders, content creators, scripters (VexScript/Lua/KotlinScript)

> Summary: **Cobalt** is a hybrid-conductive material that serves as the game's primary signal medium. It conducts **electricity** (digital/analog/frequency), **mana/magic** (flux, resonance), and can be used physically (dust, wire) or as discrete devices (gates, lamps, extenders, enclosures). This document defines cobalt behavior, placement rules, interactions with electronics, magic mechanics and details the functional blocks that use cobalt.

---

## Quick overview (tl;dr)

* **Cobalt** conducts both **electrical signals** and **magical flux**. It works underwater, on/inside blocks, on slabs, stairs and slopes, and supports layered routing inside a single block.
* **Signal types:** digital (boolean), analog (0..N), and frequency/RF (multiplexed channels). Also: **mana flux** (magical charge).
* **Blocks**: Cobalt Dust (trace), Cobalt Extender (booster/logic modifier), Cobalt Block (signal modifier), Logic gates (AND/OR/NOT/...); lamps, tripwires, item displays, doors, explosives, and more interact with cobalt signals.
* **Scripting:** `Event` hooks and `Device` APIs allow scripts (VexScript/Lua/KotlinScript) to read/write cobalt signals, subscribe to changes, and actuate devices.

---

## 1 — Fundamental behavior of Cobalt

### 1.1 Physical placement & geometry

Cobalt placements are flexible and follow these adjacency rules:

* **Placeable**:

    * On block faces (all six directions).
    * **Embedded** inside any block that supports through-conductivity.
    * On **slabs** (horizontal and vertical faces).
    * On **stairs** (all directions) — traces follow stair geometry.
    * On **slopes / triangular/angled blocks** — traces follow the slope from top edge to opposite bottom edge.
    * **Underwater** and touching air — cobalt ignores water immersion when conducting.
* **Layering**:

    * Up to **3 layers** per block volume (configurable). Layers behave like PCB layers: upper/lower/inner. Traces on different layers connect only through vias/components.
* **Connectivity**:

    * Traces connect when they touch at a point (face/edge/vertex), unless separated by insulating material.
    * **Insulation** (plastics, glass, special coatings) prevents undesired connections and crosstalk.

### 1.2 Signal semantics

* **Digital**: ON/OFF truth values. Used for logic gates, binary inputs.
* **Analog**: integer range (default `0..15`), configurable server-side (e.g., `0..255` or continuous). Used for brightness, motor speed, fine control.
* **Frequency / RF**: pulse trains at specified frequencies; multiplex several logical channels on the same physical trace.
* **Mana / Magical Flux**: separate but interoperable resource measured in `flux` units (configurable). Mana travels on cobalt but obeys different attenuation and resonance rules.

### 1.3 Attenuation, speed, and repeaters

* **Propagation speed**: signal travels per game tick; default is immediate within the same tick for adjacent segments, but larger networks can be throttled to reduce CPU cost. (Tunable.)
* **Attenuation**: signal weakens over distance. `Cobalt Extender` and specialized repeaters restore amplitude, convert between signal types, or amplify magical resonance.
* **Repeaters**: directional or bidirectional repeaters can be built; they may also add small delays (useful for timing/pulse-chains).

### 1.4 Thermal & environmental interactions

* Cobalt conducts heat. Heavy currents (or high-mana quotas) can heat traces or chassis causing:

    * Thermal throttling (lowered throughput)
    * Overheat events (shorts, temporary device failure)
* Underwater: cobalt ignores water's insulating effects (operational), but heat exchange with water can be used for cooling.

---

## 2 — Magic & Cobalt: rules and interactions

Cobalt is **mana-conductive** — it accepts, stores, and routes magical flux. Key mechanics:

### 2.1 Mana conduction model

* **Flux unit**: a numeric resource analogous to electrical charge.
* Traces carry **flux** with distinct attenuation and resonance vs electricity:

    * Flux attenuates faster per length than electric signals, but can be amplified via `Cobalt Extender` tuned to a mana resonance.
* **Conductivity variants**:

    * **Raw Cobalt** — balanced electric + mana conduction.
    * **Aureate-Cobalt (Gold-plated)** — better electric conduction, weaker mana resonance.
    * **Runic-Cobalt (enchantable alloy)** — enhanced mana conduction and resonant tunability (created via crafting/ritual).
* **Mixing**: when electrical and mana signals cross on the same trace, they can interact:

    * **Constructive coupling** — a 0→1 edge simultaneous with a mana pulse can produce a combined effect (e.g., color-shifted lamp).
    * **Interference** — conflicting frequencies cause signal degradation or unexpected magic effects (useful for puzzles, hazards).

### 2.2 Enchanting & runes

* **Cobalt traces** and components can be **enchanted** or inscribed with runes to:

    * Change their **resonant frequency**
    * Increase flux capacity (store more mana temporarily)
    * Filter electrical signals (e.g., only pass pulses above XHz)
* Enchantments may require ritual components (gems, runic dust); enchantments can be temporary and degrade under heavy load.

### 2.3 Mana devices

* Some blocks convert between **flux ↔ electricity**:

    * **Mana Converters**: take mana pulses and emit electrical pulses, and vice versa.
    * **Mana Buffers**: capacitor-like devices storing flux and releasing in bursts.
* **Magic gates**: gates that use logic combined with mana conditions (e.g., AND if both electrical AND mana-flux > threshold).

---

## 3 — Signal model & data (recommended defaults — configurable)

| Name      |      Domain |                       Range / Units | Notes                               |
| --------- | ----------: | ----------------------------------: | ----------------------------------- |
| Digital   | electricity |                               `0/1` | Boolean logic for gates             |
| Analog    | electricity |                     `0..15` default | Fine control for lamps, motors      |
| Frequency | electricity |          pulses per tick (Hz equiv) | RF channels multiplexing            |
| Flux      |       magic | numeric flux units (0..100 default) | Mana resource, separate attenuation |

**Propagation & limits (default)**:

* Max contiguous cobalt trace segments before attenuation: `64` (configurable).
* Layer limit: `3` layers per block.
* Default analog range: `0..15` (server option `analog_max`).
* Default mana buffer capacity for a `Runic-Cobalt Block`: `100 flux`.

---

## 4 — Cobalt Functional Blocks (detailed)

Below each block: *behavior*, *placement*, *interfaces*, *interactions*, and *scripting hooks*.

---

### **Cobalt Dust** (trace / trace segment)

* **What it is:** the smallest unit of cobalt conductor — thin, placeable layer. Functionally equivalent to a trace on a PCB.
* **Placement**:

    * On any valid surface (air-facing, underwater), including slabs, stairs (any orientation), slopes (triangular), and full blocks (face and embedded).
* **Behavior**:

    * Carries electrical signals (digital/analog/frequency) and mana flux.
    * Default attenuation is low; adjacent cobalt dust segments connect automatically.
    * When placed on an angled face it follows the slope direction — allows elegant routing along staircases/slopes.
* **Interactions & special rules**:

    * Can be **coated** with insulation (polymer) to isolate from adjacent traces.
    * Multiple dust layers can be created in the same block (via layering mechanic). Use vias/components to connect layers.
* **Script API**:

```vex
Event.on(:cobalt_signal_change) do |ev| 
  # ev.source: CobaltDust, ev.value: {digital:, analog:, flux:, freq:}
end
Device.find("trace-1").bus.read(location: vec3, layer: 0)
```

---

### **Cobalt Extender**

* **What it is:** an active device that **amplifies** and optionally **filters/retunes** a cobalt signal.
* **Placement**: similar to dust (face, embedded, underwater).
* **Behavior**:

    * Restores attenuated signals to full amplitude.
    * Can be configured to:

        * Amplify only digital or analog signals.
        * Convert frequency channels (RF remapper).
        * Boost / dampen mana flux (tunable resonance).
    * Optionally adds a configurable **delay** (useful for timing).
* **Power**: may require a small power draw from the local rail or a cobalt-powered universal cable.
* **Use cases**:

    * Long-distance cobalt runs.
    * Changing RF channel of a multiplexed communication cable.
    * Increasing mana resonance on a ritual line.
* **Script API**:

```vex
ext = Device.find("extender-1")
ext.config(amplify: true, mode: :analog, delay_ms: 10, mana_boost: 5)
```

---

### **Cobalt Block**

* **What it is:** discrete block that locally **increases or decreases** a transmitted signal by a configurable amount — acts like an analog attenuator/amplifier and mana sink/source.
* **Behavior**:

    * Adjustable parameters:

        * `delta_analog` (e.g., +2 / -3)
        * `delta_flux` (adds/removes flux units)
        * `threshold` (gate threshold for passing signals)
    * Can be set to **stabilize** a signal (smoothing rapid pulses into analog average).
* **Placement**: block-placement only (full block), with face input/output.
* **Interactions**:

    * Useful inside dynamic circuits to shape signals or introduce analog offsets for sensor scaling.
* **Scripting**:

```vex
block = Device.find("cobalt-block-2")
block.set_params(delta_analog: -3, delta_flux: 10, mode: :stabilize)
```

---

### **Cobalt Logic Gates** (AND, OR, NOT, NAND, NOR, XOR, XNOR)

* **What they are:** passive or active logic components that operate on **digital cobalt signals** (but many accept analog/flux inputs).
* **Placement:** face/block-mounted. Some variants support multi-face IO (e.g., two inputs on sides, output in front).
* **Behavior**:

    * **Digital behavior**: follow standard truth tables (see below).
    * **Analog behavior**: gates can be set to operate in `analog-mode` (e.g., AND returns min(a,b), OR returns max(a,b), XOR returns |a-b|).
    * **Mana-aware gates**: `Runic` or enchanted gates require flux thresholds to change state (e.g., an AND that needs `>= 20 flux` to evaluate).
* **Delay & timing**: gates can have ticks of internal delay (configurable) to manage race conditions.
* **Truth tables** (digital):

**AND**

|  A |  B | Out |
| -: | -: | :-: |
|  0 |  0 |  0  |
|  0 |  1 |  0  |
|  1 |  0 |  0  |
|  1 |  1 |  1  |

**OR**

|  A |  B | Out |
| -: | -: | :-: |
|  0 |  0 |  0  |
|  0 |  1 |  1  |
|  1 |  0 |  1  |
|  1 |  1 |  1  |

**NOT**

|  A | Out |
| -: | :-: |
|  0 |  1  |
|  1 |  0  |

**NAND / NOR / XOR / XNOR** follow expected logic.

* **Scripting hooks**:

```vex
Event.on(:gate_trigger) do |ev|
  # ev.gate_id, ev.inputs, ev.outputs
end
```

---

### **Salt Lamp**

* **Type:** cobalt-powered light.
* **Behavior:** on digital input `1`, emits soft warm light; analog input controls brightness (0..analog_max).
* **Magic interaction:** mana pulses tint the lamp and can cause bioluminescent flicker if combined with flux.
* **Placement:** typical lamp placement — face-mounted or block-mounted.
* **Use:** ambient lighting controlled by cobalt circuits (e.g., daylight sensors).

---

### **Luminum Lamp**

* **Type:** monochromatic, cobalt-powered light.
* **Behavior:** emits bright single-color light; analog controls intensity; frequency inputs can flicker or strobe at RF rates (visual strobe).
* **Use:** utility lighting, signaling.

---

### **Colourium Lamp**

* **Type:** polychromatic RGB-like lamp.
* **Behavior:** supports **multiple analog channels** (R, G, B) transmitted either as separate cobalt channels, or multiplexed via RF; mana resonance can shift hues dynamically.
* **Control:** accepts a single multiplexed signal with sub-channels (e.g., `freq=1` → R, `freq=2` → G).
* **Use:** complex displays, status indicators, decorative lighting.

---

### **Item Plaque**

* **What it is:** display plate that shows any item placed on it.
* **Behavior:** can be locked/unlocked by cobalt signal; signal can rotate display, lock it, or make it vanish on command.
* **Magic interactions:** mana pulses make displayed items glow or animate; runic plaque variants can "attune" to the displayed item and emit minor flux.
* **Script API**:

```vex
plaque = Device.find("plaque-1")
plaque.lock(true)           # lock the item in place
plaque.set_display_mode(:rotate, speed: 2)
```

---

### **Block Enclosure**

* **What it is:** a transparent glass case that showcases and protects a block inside.
* **Behavior:** can be opened or locked by cobalt signal; can buffer the inner block from external cobalt traces (optionally isolating it) or allow controlled via internal connectors (e.g., for powered artifact displays).
* **Magical behavior:** inner block can be bathed in flux; runic enclosures concentrate mana and can be used in rituals.

---

### **Door / Trapdoor** (All wood, metal, gem types)

* **Powered latch:** cobalt signals control locking/unlocking and opening/closing.
* **Analog control:** partial opening/closing via analog input (for slotted trapdoors or animated doors).
* **Magic:** mana pulses can "phase lock" doors (prevent physical opening until flux threshold removed).
* **Placement:** standard placement; supports sub-block wiring for recessed latches.

---

### **Explosive Bundles (Thermite / Dynomite / C4 / C2)**

* **What they are:** in-game explosive bundles which **may** be detonated by cobalt signals.
* **Behavior & safety** (game-design focus):

    * Each bundle has:

        * `arm_time` (seconds from arming to live)
        * `trigger_modes`: `direct` (single pulse), `timed` (requires delayed trigger), `remote` (requires authenticated signal)
        * `blast_radius` and `thermal_effects` specified in game units.
    * **Cobalt-only triggers**: require a defined signal pattern (e.g., digital 1 followed by frequency X) or authenticated packet across `Network Cable` for safety against accidental triggers.
    * **Magic interactions:** mana may amplify or dampen blast; runic explosives respond to flux with greater or lesser effect (tunable).
* **Scripting & safe design:**

    * Explosive triggers require script-level confirmation or privileged capability (`VEX-CAPABILITIES` + admin approval).
    * Example API:

```vex
explosive = Device.find("charge-1")
explosive.arm(delay_s: 3)
explosive.trigger(auth_token: "sig-xyz")
```

> **Important:** treat in-game explosives as game mechanics only. All detonations are virtual; do not provide instructions for real-world explosive construction.

---

### **Tripwire**

* **What it is:** cobalt-enabled detection line (thin wire or trace) that emits a digital pulse when crossed.
* **Behavior**:

    * Emits configurable pulses (pulse width, debounce).
    * Can be set to `sentry` mode (trips on entities of certain types or sizes).
    * Integrates with `Cobalt Dust` and physical `Wire` segments.
* **Magic:** mana pulses can cloak tripwires or make them resonate (stealth detection possible).

---

### **Wire**

* **What it is:** thicker, physical conductor using cobalt core (visual rope-like).
* **Behavior:** similar to `Cobalt Dust` but designed for visible, long spans; supports carries power and multi-channel data (via braiding).
* **Placement & durability:** can span gaps, anchor to blocks, resist physical damage better than dust.

---

## 5 — Interaction with other electronics & devices

* **Cobalt** is the canonical **signal bus** — almost every electronic block accepts cobalt inputs/outputs:

    * **PCBs/motherboards** expose `cobalt` IO pins.
    * **Network devices** accept cobalt as control-plane or as analog triggers.
    * **Sensors** push readings onto cobalt traces as analog values or flux pulses.
* **Bridging**:

    * `Mana Converters` convert flux ↔ electrical analog/digital.
    * `Protocol Converters` in routers/switches translate between cobalt frequency channels and packetized `Network Cable` streams.
* **Shielding & EMI**:

    * Metal chassis and gem plating provide shielding. Unshielded runs can pick up crosstalk when adjacent to high-frequency cobalt traces.
* **IO ports**:

    * Universal ports may carry cobalt signal, power, and packet data simultaneously (advanced universal cable). Devices must declare supported protocols.

---

## 6 — Visual & audio feedback

* **Visuals**:

    * Active cobalt traces glow faint blue by default; mana-charged traces pulsate with color (tunable).
    * Frequency multiplexing shows animated bands along traces (thin moving particles).
    * Enchanted/runic traces have sigils and brighter glows.
* **Audio**:

    * Low-frequency pulses give a faint hum; high-frequency RF has a sharper tone.
* **Diagnostics UI**:

    * In-game multimeter/oscilloscope shows digital edges, analog levels and flux graphs when linked to a trace.

---

## 7 — Scripting & API examples (VexScript-like)

Below are typical interactions; actual API signatures depend on runtime implementation.

### 7.1 Subscribe to cobalt signal change

```vex
Event.on(:cobalt_signal_change) do |ev|
  # ev.loc: vec3, ev.layer: Int, ev.digital: Bool, ev.analog: Int, ev.flux: Int
  if ev.analog > 10
    Server.log("High analog value at #{ev.loc}")
  end
end
```

### 7.2 Read / Write a cobalt trace

```vex
# Write analog value to a trace at location
Device.bus.write_cobalt(vec3(100,64,200), layer: 0, analog: 12)

# Read current signal
sig = Device.bus.read_cobalt(vec3(100,64,200), layer: 0)
puts "digital=#{sig.digital}, analog=#{sig.analog}, flux=#{sig.flux}"
```

### 7.3 Trigger salt lamp when tripwire triggered

```vex
Event.on(:tripwire_trigger) do |ev|
  if ev.entity.is_player?
    lamp = Device.find("salt-lamp-23")
    lamp.io.write(analog: 15)  # full brightness
    await Time.sleep(10)       # keep for 10 seconds
    lamp.io.write(analog: 0)
  end
end
```

### 7.4 Manage mana resonance with extender

```vex
ext = Device.find("extender-east")
ext.configure(mode: :mana_tune, resonance: 42, flux_boost: 10)
```

---

## 8 — Performance & balancing considerations

* **Tick cost**: cobalt signal propagation and multi-layer checking has CPU cost. Keep default limits:

    * Max trace length before mandatory repeater: ~64 units.
    * Frequency multiplexing channels per trace: limit to reduce decode CPU cost.
    * Mana conduction ticks: sample at lower frequency when possible (less often than electrical tick).
* **Power**:

    * Advanced devices consume more energy; runic enchantments add overhead.
* **Game balance**:

    * Early-game: basic cobalt dust and basic gates.
    * Mid-game: extenders, enchanted cobalt, and multi-layer etching.
    * End-game: quantum-like runic cobalt, mana-intensive devices, large networks and servers.

---

## 9 — Implementation & modder notes

* **Data model**:

    * Represent cobalt traces as graph edges with attributes: layer, attenuation, insulation, flux_capacity, last_signal.
    * Devices register IO ports with `port_type` (`digital`, `analog`, `flux`, `rf_channel`, `packet`).
* **Events**:

    * `cobalt_signal_change(location, layer, delta)` — fired when a trace value changes.
    * `cobalt_overheat(location, device_id)` — for thermal warnings.
    * `mana_resonance_event(location, resonance_id, intensity)`
* **Persistence**:

    * Save trace states, enchantments, and flux buffers to world/chunk data. Consider pruning inactive networks periodically.
* **Security**:

    * Dangerous APIs (explosives, remote file transfer) require capability tokens and admin consent in server config.
* **Editor tooling**:

    * Provide an in-game PCB editor, trace-laying tool with snapping to layers, slope-aware routing, and annotation.
* **Debugging**:

    * In-world diagnostics (hover-cursor multimeter), network visualizers, and scope HUDs.

---

## 10 — Example use-cases & scenarios

* **Underwater research station** — cobalt traces embedded in hull plates provide robust data and power routing; mana runes in a protected lab amplify underwater enchantments.
* **Magical security vault** — runic-cobalt circuits detect flux signatures and trigger lockouts and alarms; explosive bundles are gated by multi-factor cobalt+mana authentication.
* **Theatrical RGB fountain** — Colourium lamps controlled via multiplexed frequency channels produce choreographed shows using cobalt dust laid on slope-stairs for aesthetic runs.

---

## 11 — Glossary

* **Flux** — unit of magical energy conducted by cobalt.
* **Layer** — sub-plane within a block for multi-layer routing.
* **Runic** — enchanted variant of cobalt/components with magical properties.
* **Attenuation** — loss of signal over distance.
* **Extender** — amplifier/booster that can be tuned for electric/magical signals.

---

## 12 — API Reference

### Cobalt — Full API Reference & Tuned Defaults

**Version:** 1.0
**Last updated:** 2025-10-04
**Audience:** modders, server admins, scripters (VexScript/Lua/KotlinScript)
**Purpose:** definitive in-game API for Cobalt material, cobalt functional blocks and the cobalt signal/network model — with tuned default numeric values appropriate for gameplay balance and server performance.

---

#### Table of contents

1. Design overview & units
2. Global configuration (admin tunables)
3. Core concepts & data types
4. Cobalt runtime constants (tuned defaults)
5. API: `Cobalt` module (placement & world-level)
6. API: `Device.bus` / trace IO (read/write)
7. API: `Network` helpers (scan, graph, routing)
8. API: `Extender`, `Repeater`, `Block` devices (config & control)
9. Events (payloads & semantics)
10. Diagnostics & tools API (multimeter, oscilloscope)
11. Security & capabilities required
12. Examples (VexScript)
13. Tuning rationale & recommended limits

---

#### 1 — Design overview & units

* **Tick**: 1 game tick = 1/20 second (use ticks for timing).
* **Segment**: one cobalt trace unit placed on a block face/edge/vertex or wire span unit.
* **Layer**: sub-plane inside a block; multi-layer routing supported (default 3).
* **Analog units**: integer range `0..ANALOG_MAX` (default `0..15`).
* **Flux**: mana units conducted by cobalt, integer (`0..FLUX_BUFFER`).
* **Current Units (CU)**: simplified electrical current unit used for thermal/power modelling.
* **Distance units**: measured in number of contiguous cobalt segments.

---

#### 2 — Global configuration (admin tunables)

All of these can be set in server config; names shown for reference:

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
  digital_threshold: 1         # digital considered ON if analog >= this
  trace_safe_current_cu: 10
  thick_cable_safe_current_cu: 100
  thermal_overheat_temp: 100.0
  thermal_melt_temp: 250.0
  heat_per_cu_per_tick: 0.01
  water_cooling_per_tick: 0.5
  max_frequency_channels_per_trace: 8
  max_rf_rate_hz: 40           # pulses per tick equivalent cap guard
```

Default tuned values are expanded in section **4**.

---

#### 3 — Core concepts & data types

**Trace** — single contiguous cobalt connection segment; attributes:

* `loc` (Vec3 + face/edge/vertex + layer)
* `material` (`cobalt`, `aureate_cobalt`, `runic_cobalt`, etc.)
* `insulated` (bool)
* `last_signal` (SignalSnapshot)

**SignalSnapshot**

```ts
{
  digital: Bool,        // true if digital ON
  analog: Int,          // 0..ANALOG_MAX
  flux: Int,            // 0..dynamic buffer
  freq_channel: Int?,   // if part of RF multiplex
  timestamp: Long       // tick/time last set
}
```

**Port** — device input/output anchor (named port, supports `digital`/`analog`/`flux`/`rf`/`packet`).

**NetworkGraph** — graph of connected traces/devices used by scan/route.

---

#### 4 — Cobalt runtime constants (tuned defaults)

These defaults are chosen for balanced gameplay performance and to allow interesting mid/late-game complexity.

* `ANALOG_MAX = 15` (default analog precision)
* `MAX_SEGMENTS_BEFORE_REPEATER = 64` (require repeaters/extenders beyond this)
* `PROPAGATION_SPEED = 8` segments / tick (signals can traverse up to 8 segments per tick)
* `LAYERS_PER_BLOCK = 3` (upper/inner/lower)
* `ANALOG_ATTENUATION = 1 analog unit per 16 segments`
* `FLUX_ATTENUATION = 1 flux unit per 4 segments`
* `DEFAULT_FLUX_BUFFER (Runic-Cobalt block) = 100 flux units`
* `EXTENDER_DEFAULT_BOOST_ANALOG = restore to full / +15 units`
* `EXTENDER_DEFAULT_BOOST_FLUX = +40 flux units`
* `REPEATER_DELAY = 1 tick (configurable)`
* `TRACE_SAFE_CURRENT_CU = 10` (current units for standard cobalt dust per segment)
* `THICK_CABLE_SAFE_CURRENT_CU = 100`
* `HEAT_PER_CU_PER_TICK = 0.01 heat units`
* `OVERHEAT_TEMP = 100.0 heat units` (performance throttling)
* `MELT_TEMP = 250.0 heat units` (device failure / visual damage)
* `WATER_COOLING_PER_TICK = 0.5 heat reduction`
* `MAX_RF_CHANNELS_PER_TRACE = 8`
* `MAX_RF_RATE_HZ = 40` (practical cap on pulse per tick equiv; higher rates are disallowed or downsampled)

> These are tunable in server config. Use them as conservative defaults that keep small builds cheap and large networks requiring infrastructure (repeaters, extenders, fiber).

---

## 5 — API: `Cobalt` module — world-level placement & query

**Permission:** reading basic info is allowed; placement/destruction requires `Cobalt.Place` capability (normal players with in-game tools can place if server allows). Admin-level functions require `Cobalt.Admin`.

### `Cobalt.place_trace(loc: Vec3, face: Face, layer: Int = 0, material: Symbol = :cobalt, insulated: Bool = false) -> Bool`

Place a cobalt dust segment at location `loc` on the given face and layer.

* `loc` – block coordinate `{x,y,z}` (block position)
* `face` – one of `:north,:south,:east,:west,:up,:down` or a slope identifier
* `layer` – `0..(LAYERS_PER_BLOCK-1)`
* `material` – `:cobalt`, `:aureate_cobalt`, `:runic_cobalt`, etc.
* `insulated` – true to place with insulation around it

**Returns**: `true` if placed successfully.

---

### `Cobalt.remove_trace(loc: Vec3, face: Face, layer: Int = 0) -> Bool`

Remove trace; returns `true` on success.

---

##### `Cobalt.get_trace(loc: Vec3, face: Face, layer: Int = 0) -> TraceInfo | nil`

Get trace metadata and last signal snapshot.

**TraceInfo**

```ts
{
  material: Symbol,
  insulated: Bool,
  last_signal: SignalSnapshot,
  heat: Float,
  safe_current_cu: Int
}
```

---

##### `Cobalt.scan_network(center: Vec3, radius: Int=16) -> NetworkGraph`

Scans and returns connected traces/devices within radius as a NetworkGraph object. Useful for diagnostics and routing.

---

##### `Cobalt.get_graph_connected_to(loc: Vec3, face: Face, layer: Int = 0) -> NetworkGraph`

Returns graph for contiguous network anchored at given trace.

---

##### `Cobalt.admin.set_config(key: String, value: Any)`

**Permission:** `Cobalt.Admin` only. Set server-level cobalt config.

---

#### 6 — API: `Device.bus` — trace IO (read / write)

**Permission:** scripts running as device owners can read/write to their device ports. Cross-device trace writes read the public state (subject to permissions). For Host-level scripts, require `Cobalt.IO` capability.

##### `Device.bus.read_cobalt(loc: Vec3, face: Face, layer: Int = 0) -> SignalSnapshot`

Return latest snapshot `{digital, analog, flux, freq_channel, timestamp}`.

---

##### `Device.bus.write_cobalt(loc: Vec3, face: Face, layer: Int = 0, digital: Bool? = nil, analog: Int? = nil, flux: Int? = nil, freq_channel: Int? = nil) -> Bool`

Write to a trace. Partial writes allowed (e.g., only set `analog`), unspecified fields are unchanged.

* Writes are queued and propagated according to `PROPAGATION_SPEED`.
* If write violates current or thermal limits, it returns `false` and optionally raises `cobalt_overheat` event.

**Returns**: `true` if accepted.

---

##### `Device.bus.emit_pulse(loc, face, layer, duration_ticks: Int, analog_value: Int = ANALOG_MAX, flux_value: Int = 0, freq_channel: Int? = nil)`

Emit a timed pulse (handled by trace scheduler).

---

##### `Device.bus.create_via(loc1, face1, layer1, loc2, face2, layer2) -> Bool`

Create a via (vertical/hole) connecting layers or faces (consumes components/crafting). Vias are required to cross layers.

---

##### `Device.bus.lock_trace(loc, face, layer, lock_token: String)`

Lock a trace to prevent unauthorized write until token matched (used for security e.g., vault triggers). Requires capability `Cobalt.Admin` to force-unlock.

---

#### 7 — API: `Network` helpers (packetized network)

**Purpose:** higher-level packetized networking for Network Cable / Fiber devices.

### `Network.send(dest: String, port: Int, payload: Bytes, reliable: Bool = true) -> PacketHandle`

Sends a packet on packet network. Traces on cobalt communication cables may carry control-plane signals for routing but packetized network uses dedicated `Network Cable` devices.

---

##### `Network.route_graph(graph: NetworkGraph, policy: RoutePolicy) -> RouteResult`

Compute or install routes across a cobalt-enabled graph for hybrid cable + cobalt control surfaces.

---

#### 8 — API: Extender / Repeater / Cobalt Block devices

> These are device-level APIs. Obtain with `Device.find("extender-1")` or via event payloads.

##### `Extender.configure(options: ExtenderConfig) -> Bool`

`ExtenderConfig`:

```ts
{
  amplify_mode: :digital | :analog | :flux | :rf | :all,
  analog_boost: Int,        // how many analog units to restore (default EXTENDER_DEFAULT_BOOST_ANALOG)
  flux_boost: Int,          // flux units to add/restore
  delay_ticks: Int,         // artificial delay
  rf_channel_map: Map<Int,Int> // remap input RF channels to output channels
}
```

##### `Extender.get_status() -> {temp:Float,current_load_cu:Int,uptime_ticks:Int}`

---

##### `Repeater.configure(delay_ticks: Int = REPEATER_DELAY, directional: Bool = true)`

Repeaters restore signal; can be directional and add delay.

---

##### `CobaltBlock.set_params(delta_analog: Int, delta_flux: Int, mode: :stabilize | :pass_through | :scale, enabled: Bool)`

`mode` options:

* `stabilize`: averages pulses into analog mean
* `scale`: multiply incoming analog by scale factor
* `pass_through`: pure pass-through with delta applied

---

#### 9 — Events (payloads & semantics)

**Event names & payload structure — all events available to `Event.on` handlers in `.vex` files.**

##### `:cobalt_signal_change`

Fired when a trace changes value.

Payload:

```ts
{
  source_loc: Vec3,
  face: Face,
  layer: Int,
  old: SignalSnapshot,
  new: SignalSnapshot,
  device_id: String?  // device connected to this trace, if any
}
```

##### `:cobalt_overheat`

Fired when a trace/device exceeds `OVERHEAT_TEMP`.

Payload:

```ts
{
  loc: Vec3,
  face: Face,
  layer: Int,
  current_heat: Float,
  threshold: Float,
  device_id: String?
}
```

##### `:cobalt_melt`

Fired on permanent failure / melt (device destroyed or visually damaged)

Payload:

```ts
{
  loc, face, layer, cause: :thermal | :explosive | :corrosion, device_id?
}
```

##### `:flux_threshold_crossed`

Fired when flux at a trace crosses a configured threshold.

Payload:

```ts
{
  loc, layer, old_flux: Int, new_flux: Int, threshold: Int, resonant_id: String?
}
```

##### `:network_scan_complete`

Fired when an async scan completes (Cobalt.scan_network)

Payload:

```ts
{ origin, graph: NetworkGraph, scan_id }
```

---

#### 10 — Diagnostics & tools API

##### `Tools.multimeter.attach(loc, face, layer) -> MultimeterHandle`

Multimeter methods:

* `read() -> {voltage: Float, current_cu: Int, analog: Int, digital: Bool, flux: Int}`

##### `Tools.oscilloscope.attach(loc, face, layer) -> OscHandle`

Oscilloscope:

* `start_capture(sample_rate_hz: Int, duration_ticks: Int)`
* `get_waveform() -> WaveformData` (for UI plotting; heavy on server — rate-limit)

##### `Tools.logic_analyzer.attach(bus_loc, pins:Array<Int>, sample_rate_hz, duration_ticks) -> Capture`

Decode common bus protocols (I2C, SPI, UART) if library exists.

---

#### 11 — Security & capabilities required

APIs and controls require capabilities which server operators grant on script load (`VEX-CAPABILITIES`):

* `Cobalt.IO` — reading/writing arbitrary traces
* `Cobalt.Place` — placing and removing traces
* `Cobalt.Admin` — changing cobalt server config, forcible unlocks, admin-level scans
* `Cobalt.ExplosiveControl` — arming/disarming explosive bundles via API (high-risk; admin-only)
* `Host.Http`, `Host.File` — for scripts that log, export or interact with external services

**Note:** Explosive arming/triggering MUST be gated by `Cobalt.ExplosiveControl` and additional server checks (e.g., major confirmation prompts) to prevent accidental or malicious misuse.

---

#### 12 — Examples (VexScript)

##### Example 1 — read a trace and log

```vex
Event.on(:cobalt_signal_change) do |ev|
  sig = ev.new
  Server.log("Cobalt changed at #{ev.source_loc} (layer #{ev.layer}): digital=#{sig.digital}, analog=#{sig.analog}, flux=#{sig.flux}")
end
```

##### Example 2 — extend a long run with automatic extenders

```vex
fn auto_extend_run(path: Array<Vec3>) do
  # path is a series of positions to inspect (path planning assumed)
  for i in 0 .. path.length-1 step 64 do    # every 64 segments
    loc = path[i]
    if Cobalt.get_trace(loc, :north, 0) != nil
      # place extender at loc+offset (server permission required)
      Cobalt.place_trace(loc, :up, 0, :cobalt, insulated: false) # place dust for anchor
      dev = Device.find_nearest(loc, "extender", radius: 2)
      if dev
        dev.configure(amplify_mode: :all, analog_boost: 15, flux_boost: 40)
      end
    end
  end
end
```

##### Example 3 — guard with flux threshold (magic vault)

```vex
# require runic cobalt and token on the trace
vault_trace = vec3(100, 64, 200)
Event.on(:flux_threshold_crossed) do |ev|
  if ev.loc == vault_trace && ev.threshold == 50 && ev.new_flux >= 50
    # deny unlock if flux spike from unknown source
    Server.broadcast("Flux spike detected on vault line — aborting unlock.")
    Device.find("vault-lock").io.write(digital: 0) # force lock
  end
end
```

---

#### 13 — Tuning rationale & recommended limits

**Why these defaults?**

* `ANALOG_MAX = 15` gives a familiar, compact resolution for lighting and motor control without expensive per-segment arithmetic. It maps well to 4-bit control nodes and is easy for players to think about while being low-cost server-side.
* `MAX_SEGMENTS_BEFORE_REPEATER = 64` encourages infrastructure (repeaters/extenders) for long runs and prevents trivial cross-world long-distance links. 64 is large enough for many builds but small enough to force planning.
* `PROPAGATION_SPEED = 8` segments/tick allows signals to travel moderate distances quickly (8 * 20 = 160 segments/second) while containing per-tick CPU work. It also interacts well with scheduled repeaters and extenders.
* `ANALOG_ATTENUATION = 1 per 16 segments` — gentle decay makes long analog runs meaningful but non-trivial.
* `FLUX_ATTENUATION = 1 per 4 segments` — flux (magic) decays faster to encourage local mana sources and buffers; magic remains special and valuable.
* `TRACE_SAFE_CURRENT_CU = 10` & `THICK_CABLE_SAFE_CURRENT_CU = 100` provide a two-tier power system: dust for logic, thick cables for power rails.
* Thermal constants (`OVERHEAT_TEMP`, `MELT_TEMP`, `HEAT_PER_CU_PER_TICK`) keep thermal management meaningful but not punishing. Water cooling being strong (`0.5` heat/tick) rewards underwater or water-cooled designs.

**Performance considerations**

* Oscilloscope & logic analyzer captures are server-heavy; gating and rate-limits are recommended.
* RF multiplexing (max channels/rate) is capped to avoid high-frequency decode costs.

---

#### Appendix — Storage & persistence

* Cobalt trace state (last signal, heat, layer placement, enchantments) must be saved in chunk/world data.
* Large inactive networks should be trimmed or set to low-sampling frequency to save CPU/memory.
* Admins may set auto-prune thresholds (e.g., prune traces with no change > X days).

---

#### Closing notes & next steps

I can now produce any of the following in the same format (pick one or more):

1. **Complete JSON schema** for `TraceInfo`, `SignalSnapshot`, `NetworkGraph`, `EventPayloads` suitable for runtime and serialization.
2. **Server config template** (`cobalt.yml`) with comments and recommended ranges for each value.
3. **VexScript helper library** implementing common tasks (`scan_and_extend`, `flux_guard`, `auto_cool`) using the above API.
4. **Balance test scenarios** (example builds + expected CPU/power/thermal loads) to further refine defaults.

Which would you like next?
