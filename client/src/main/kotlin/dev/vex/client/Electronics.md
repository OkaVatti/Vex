# Electronics & Cobalt — Design Spec (Game Mechanics + Tech Stack)

**Version:** 1.0
**Last Updated:** 2025-10-04
**Audience:** modders, game designers, server admins, and scripters (VexScript / Lua / KotlinScript)
**Summary:** A complete design for an electronics subsystem that blends ComputerCraft-style programmable computers with Minecraft-Redstone-style wiring — replacing Redstone with **Cobalt**, a versatile conductive material that works on/inside blocks and underwater, and plays nicely with Copper, Gold, Silicon and other materials to form PCBs, chassis and devices.

---

## Quick overview

* **Cobalt** is the *new conductor*. Unlike classic redstone:

    * Works **on block faces**, **inside blocks**, **on slabs/stairs/slopes/triangular blocks**, and **underwater**.
    * Supports **analog** and **digital** signalling, **frequency multiplexing**, and **pulse timing**.
    * Can be **insulated**, **shielded**, and **alloyed** (with Copper/Gold) to change conductivity, attenuation, and corrosion characteristics.

* **Electronics** use discrete materials:

    * **Copper** — inexpensive conductor (wires/traces).
    * **Gold** — low-resistance conductor, used for contacts and high-speed traces.
    * **Silicon** — semiconductors: chips, microcontrollers, and etched PCBs.
    * **Other metals, gems, plastics** — used for chassis, shielding, and plating.

* **Network stack**: Communication Cable → Network Cable → Fiber Network Cable → Universal / Advanced Universal (power + multi-protocol).

* **Computers & Devices**: Range from tiny microcontrollers to full Servers and modular Routers / Switches / Wireless APs. All are buildable and programmable with VexScript, Lua, or KotlinScript.

---

## Table of contents

1. Materials & Properties
2. Cobalt — behavior & mechanics
3. Cable & Network types (quick ref)
4. Chassis & Form-factors (mapping)
5. Computers, Routers & Switches (device classes)
6. PCBs, Components & Microcontrollers (tiers & uses)
7. IO, Ports & Connectors
8. Power: supplies, current, thermal & balancing
9. Tools & Diagnostics (multimeter, oscilloscope, firmware flasher)
10. Assembly workflow & in-game crafting/etching pipeline
11. Integration with scripting & API examples (VexScript)
12. Advanced design examples (underwater datacenter, submarine control)
13. Performance & balancing notes (for designers)
14. Modder hooks & extension points

---

## 1 — Materials & Properties

### Cobalt

* **Type:** Conductive metal unique to this system.
* **Key properties:**

    * **Underwater-safe:** does **not** corrode or lose conduction underwater.
    * **Embed-friendly:** can be placed inside a full block’s interior voxel and on sub-block geometries (slabs, stairs, triangular slopes).
    * **Signal modes:** `digital` (0/1), `analog` (0..15 by default, configurable), `rf` (frequency for multiplexing).
    * **Thermal:** conducts heat; prolonged high current can raise temperature and cause thermal throttling if not heat-sunk.
    * **Insulation/Shielding:** can be wrapped with polymer/plastic/chassis to reduce crosstalk.
    * **Aesthetic:** cobalt tint for textures; variants (raw, plated with gold/copper).

### Copper

* Cheap, medium conductivity.
* Easier to craft; used in cheap wires and low-speed PCB traces.

### Gold

* Low-resistance conductor for high-speed contact surfaces and connectors.
* Used on PCB pads and high-end chassis plating.

### Silicon

* Base for chips: microcontrollers, CPUs, GPUs.
* Also used to "etch" PCBs — silicon-etched areas define trace patterns and vias.

### Other materials

* **Plastic** — cheap, insulating chassis, colorable.
* **Metals (iron, steel, titanium, exotic)** — chassis with different thermal, weight, and shielding properties.
* **Gems** — decorative chassis or exotic plate with special properties (e.g., *gem chassis increases signal integrity or doubles shielding*).

---

## 2 — Cobalt: behaviour and mechanics

### Placement / Geometry

* Placeable as:

    * **Surface trace** (on block face).
    * **Embedded trace** (inside block interior).
    * **Sub-block trace** (on slabs, stairs, triangular slopes) — respects the slope geometry and adjacency rules.
* When placed against sloped geometry, cobalt automatically follows the surface’s direction; this enables neat stair/slope signal routing.

### Connectivity rules

* Two cobalt traces connect if they share any touch point (face, edge, or vertex) and are not separated by an insulating layer.
* **Embedded** traces connect through a block if the block has `through-conductivity` (some blocks like glass/air/stone allow embedded cobalt to pass; others block it).
* Cobalt can be **layered** (up to 3 sub-layers per block) so multi-layer PCB-like circuits can exist in a single block volume — realistic PCBs in-game.

### Signal types

* **Digital** — binary ON/OFF; suitable for logic gates.
* **Analog** — integer range (default 0–15, configurable server-side); useful for brightness, motor speed, analog sensors.
* **Frequency (RF)** — pulses at specific frequencies carry multiplexed channels over the same conductor (communication cable type recommended).
* **Power carriage** — Cobalt can convey both signal and DC power if used with Universal/Advanced Universal cables (see below).

### Attenuation & Distance

* Each cobalt segment has a base attenuation. Cobalt's attenuation is low, but some cable types (copper vs cobalt vs fiber) have different attenuation/latency.
* **Repeaters**: Cobalt repeaters or amplifiers exist (simple repeaters, powered amplifiers, directional repeaters) to restore signal and optionally change modes (digital→analog conversions, etc).

### Interaction with fluids & environment

* Underwater: unchanged (full functionality).
* Lava or extreme heat: cobalt heats up — if uncooled, signals can degrade or cause short-circuit events.
* Pressure / depth: no special effect unless an admin chooses to model it.

### Mechanical interactions

* Pistons (or game-equivalents) can move blocks with embedded cobalt if configured; this may disconnect connections depending on movement rules (can optionally support flexible "ribbon" cobalt which bends with block movement).

---

## 3 — Cable & Network types (quick reference)

| Cable Type                   |                               Purpose |                                    Capacity | Special                    |
| ---------------------------- | ------------------------------------: | ------------------------------------------: | -------------------------- |
| **Cable**                    | Basic wire — low-speed digital/analog |                                   1 channel | cheap                      |
| **Thick Cable**              |                    High-current/power |              more current, less attenuation | bulky                      |
| **Communication Cable**      |             Pulse/frequency optimized |                carries multiplexed channels | ideal for many RF channels |
| **Network Cable**            |    Packetized network (Ethernet-like) |                            medium bandwidth | supports addressing        |
| **Fiber Network Cable**      |             High-bandwidth/long-range |            high throughput, low attenuation | immune to EM shielding     |
| **Universal Cable**          |                 Power + data combined |          carries both, limited multiplexing | handy for compact builds   |
| **Advanced Universal Cable** |            Multiplexing + QoS + power | high capacity, requires advanced components | server-grade cabling       |

**Notes:**

* **Fiber** is best for long underground runs or underwater backbones.
* **Communication cable** uses cobalt in a special braid that favors RF multiplexing (less cross-talk).
* Cables may come as **insulated**, **shielded**, or **armored** with extra chassis materials for protection and reduced interference.

---

## 4 — Chassis & Form-factors

Chassis define the housing for PCBs, daughterboards, microcontrollers, and peripherals.

### Chassis families

* **Metal chassis** (all metal types, dyes) — heavy, great thermal conduction, high EMI shielding.
* **Plastic chassis** — light, insulating, color variants.
* **Gem chassis** — cosmetic + optional gameplay benefits (e.g., signal integrity bonus).

### Sizes (slot counts / PCB compatibility)

* `Mini` — 1 PCB slot (Mini PCB)
* `Small` — 2–4 PCB slots
* `Medium` — 5–8 PCB slots
* `Large` — 9–16 PCB slots
* `XL` — 17+ slots (server / rackmount)

Chassis include mounting points for:

* Power supply unit (PSU)
* Cooling (fans/heat sinks)
* IO ports (USB-like, network ports, fiber ports)
* Expansion (daughterboard bay)

**Modifiers**

* **Dyed** plastic: purely cosmetic.
* **Plated** metal (gold/copper plating): reduces contact resistance.
* **Armored**: increases physical/thermal resistance.

---

## 5 — Computers, Routers & Switches (device classes)

### Computers

* **Mini Computer** — microcontroller-class device (8-bit/16-bit). Low power, limited I/O.
* **Small Computer** — general scripting station for simple automation.
* **Personal Computer** — moderate CPU + GPU, suitable for running user interfaces, moderate server tasks.
* **Workstation** — faster CPU/GPU, heavy local tasks.
* **Server** — rackmounted, multi-socket, runs multiple virtual machines / network services.

### Networking devices

* **Router / Advanced Router** — route packets, NAT, QoS policies, multi-interface.
* **Wireless Router / Advanced Wireless Router** — handles wireless channels/bands, client auth.
* **Switch / Advanced Switch** — layer 2/3 switching, VLANs.
* **Wireless Access Point / Advanced WAP** — simple client AP or advanced management features.
* **Wireless Net Interface Card (NIC)** and **Wireless Antenna** — mountable components.

Each device has:

* CPU/microcontroller socket(s)
* RAM slots (DRAM3/4/5 depending on generation)
* Storage (PROM/EEROM/NVRAM)
* Network ports (copper/fiber/wireless)
* IO ports and expansion bays

---

## 6 — PCBs, Components & Microcontrollers

### PCB sizes & types

* **Mini / Small / Medium / Large / XL PCB** — physical board sizes that map to chassis slots.
* **Etched PCB** — custom layout by the player (requires etching tool and blank PCB).
* **Multi-layer PCBs** — created by stacking etched layers; allow sophisticated routing.

### Components

* **Passive:** Resistors, capacitors, inductors (in-game uses: RC timing, filters, debouncing).
* **Active:** Transistors (switching, amplification), op-amps (analog processing).
* **Programmables:** Microcontrollers (8/16/32/64/128-bit tiers), GPUs, NPUs.
* **Memory:** PROM, EEPROM, Static Memory Controllers, DRAMx, NVRAM — sizes vary by microcontroller class (see your provided list).
* **Special Modules:** Encryption modules, secure platform module, biometrics module, audio/video controllers.

### Microcontroller tiers (use-case)

* **8-bit** — minimal logic, simple I/O, low-power automation (e.g., door controllers).
* **16-bit** — more complex control, MIDI/audio handling.
* **32-bit** — device controllers, video controllers, DRAM support.
* **64-bit** — heavy computation, encryption, NPU; suitable for servers.
* **128-bit (emulated)** — high-end features for advanced modules; used for big storage and advanced encryption.

**Memory & peripherals mapping:** (short)

* 8-bit → PROM up to 4MB, static controllers for small DRAM, simple audio controllers.
* 16-bit → PROM up to 256MB, audio/video controllers, graphics memory controllers.
* 32-bit → DRAM3 up to multiple GB, video controllers, advanced GPUs.
* 64/128-bit → DRAM4/DRAM5 & NVRAM up to TB/PB ranges, encryption & biometrics.

---

## 7 — IO, Ports & Connectors

### Port types

* **Universal Cable Port** — supports multiple cable types with adapter.
* **Advanced Universal Port** — handles power + multiplexed data.
* **Video / Advanced Video Port** — analog/digital video output.
* **Audio / Advanced Audio Port** — stereo, multi-channel audio.
* **Programmable Button** — configurable input (GPIO-like).

### Motherboard & expansion

* **Motherboard** — base logic with CPU socket, RAM sockets, chipset.
* **Advanced Motherboard** — additional PCI-like expansion lanes and built-in networking.
* **Daughterboard / Advanced Daughterboard** — plug-in modules for extra IO or specialized features (e.g., extra NICs, GPU modules).
* **Server Motherboard** — multi-socket, redundant PSUs, rack features.

---

## 8 — Power: supplies, current, thermal & balancing

### Power supply families

* **Mini** — 25W–400W
* **Small** — 100W–500W
* **Regular** — 100W–2500W (various)
* Larger units for servers & racks up to multi-kW.

### Power model

* Devices draw power according to component list (CPU tier, DRAM, NICs).
* **Power rail types**: 5V/12V/48V/DC; universal cables can carry multiple rails (internally multiplexed).
* **Thermal throttling**: if a device runs hot (from heavy CPU + inadequate cooling), it throttles speed or disables non-critical peripherals.
* **UPS / Redundancy**: high-tier PSUs support N+1 redundancy and hot-swap.

---

## 9 — Tools & Diagnostics

### Tools provided

* **Multimeter** — measure signal levels, continuity, current, voltage.
* **Oscilloscope** — measure pulse/frequency waveforms (needed for RF/communication cable debugging).
* **Logic Analyzer** — sample digital buses and decode protocols.
* **Spectrum Analyzer** — inspect RF channels (wireless).
* **Flasher** — write firmware to PROM/EEPROM.
* **PCB Etcher / Soldering Station** — produce etched PCBs and solder components.

### Diagnostics API

* `Device.get_status()`: returns device health, temperature, power draw.
* `Network.scan()`: returns active devices and connection stats.
* `IO.read(pin) / IO.write(pin, value)`
* `Bus.sniff(channel)` for analyzing packet traffic (requires permissions)

---

## 10 — Assembly workflow & PCB etching pipeline

1. **Design PCB** — in-game editor or external tool exporting an etch file.
2. **Blank PCB** — craft blank board (mini → XL).
3. **Etch** — use PCB etcher + chemical tank or laser etcher machine.
4. **Populate** — place components (resistors, transistors, microcontrollers) onto the etched board.
5. **Solder** — use soldering station (or automated pick-and-place for advanced facilities).
6. **Flash firmware** — write code to PROM/EEPROM using flasher.
7. **Mount in chassis** — insert PCB into selected chassis size; connect PSUs, IO, and cables.
8. **Verify** — run diagnostics and tools; iterate.

> **Note:** Microcontrollers require bootloaders and drivers; some chips accept "bare-metal" code via PROM; others use standard BIOS-like firmware.

---

## 11 — Integration with scripting & API examples (VexScript)

All `Computer` blocks expose a `Device` API: `Device.io`, `Device.bus`, `Device.net`, `Device.power`, `Device.hw` (hardware metadata). `Event` hooks fire for IO changes, network packets, and power events.

### Example — blinking LED (VexScript 2.x style)

```vex
# scripts/blink_led.vex
puts "Loading blink_led"

# Assume pin 1 on device is mapped to cobalt-driven LED output
Server.register_command("blink") do |player, args|
  dev = Device.find("workstation-1")
  spawn do
    10.times do
       dev.io.write(pin: 1, value: 1)  # write digital high via cobalt trace
       await Time.sleep(0.5)
       dev.io.write(pin: 1, value: 0)
       await Time.sleep(0.5)
    end
  end
end
```

### Example — read analog sensor and send packet over network cable

```vex
# scripts/gel_sensor.vex
# Reads analog on pin 2 and sends reading to server over network cable
async fn read_and_send() do
  dev = Device.find("sensor-node-3")
  value = dev.io.read_analog(pin: 2) # 0..15
  pkt = { "node": dev.id, "reading": value }.to_json()
  Network.send(dest: "192.168.0.100", port: 9000, payload: pkt)
end

Event.on(:tick) do |ev|
  # tick every server tick; filter using a mod counter
  if (ev.tick % 20) == 0
    spawn do await read_and_send() end
  end
end
```

### Example — fiber backbone & router config

```vex
# scripts/fiber_router.vex
router = Device.find("router-1")
# configure route table and QoS via API
router.net.configure do |cfg|
  cfg.add_route("10.0.0.0/24", next_hop: "10.0.0.254")
  cfg.set_qos(priority: :video, bandwidth_percent: 30)
end
```

API surface examples are intentionally concise; full API docs should list all method signatures and capability requirements.

---

## 12 — Advanced design examples

### 12.1 Underwater sensor network (submarine)

* **Goal:** monitor hull damage & temperature across a submerged base.
* **Stack:** Cobalt embedded traces in hull plates → Communication Cable ring → Mini Computer nodes (16-bit) → Fiber backbone to server room → Server for aggregation.
* **Features:** Cobalt works underwater, fiber backbone connects to surface through pressure-sealed fiber port, diagnostics via oscilloscopes.

### 12.2 Floating datacenter

* **Goal:** redundancy & cooling using water contact.
* **Stack:** Servers in metal chassis with active cooling; chassis are water-resistant (special plating), cobalt traces insulated for external run; massive PSUs with redundancy.
* **Thermal:** water-based heat sinks via chassis plating.

---

## 13 — Performance & balancing notes (for designers)

* **Signal update cost:** each time step compute propagation over active cobalt traces. To avoid CPU spikes:

    * Limit multi-layer cobalt per block.
    * Use fiber for long-distance (reduces per-step propagation cost).
    * Rate-limit frequency multiplexing to server-acceptable tick budget.

* **Power & heat:** assign real costs to high CPU/gpu tasks; balance mid-game vs late-game progression by component availability (e.g., DRAM4 requires advanced resources).

* **Automation complexity:** etched PCBs and multi-layer boards enable compact machines — but increase crafting and assembly time/requirements.

* **Network scaling:** provide modern network devices (switches/routers) to offload packet routing tasks to devices rather than having scripts route everything.

---

## 14 — Modder hooks & extension points

* **Device API**: read/write access points for all components; expose extension points for new components (e.g., custom NIC).
* **Custom Cable types**: modders can define cable behavior (attenuation, multiplexing) via scriptable config.
* **Chip definitions**: allow adding new microcontrollers via JSON/yaml module files (specify power draw, memory, supported peripherals).
* **Render & texture hooks**: for cobalt variants (gold-plated cobalt, braided cables) and chassis skins.
* **Security hooks**: admin-controlled capability checks (`VEX-CAPABILITIES`), script signing and audit logs.

---

# 15: Electronics API Reference

## Appendix — Quick reference tables

### Cable quick guide

* **Cable** — cheap, short-range digital
* **Thick Cable** — high current
* **Communication Cable** — multiplexed RF channels
* **Network Cable** — packet switching
* **Fiber** — backbone, long-range
* **Universal** — power + data
* **Advanced Universal** — QoS, multiplexing, high power

### Chassis ↔ PCB mapping

* `Mini` chassis → Mini PCB
* `Small` chassis → Small PCB (or multiple Mini)
* `Medium` chassis → Medium PCB / daughterboard expansion
* `Large/XL` chassis → multiple PCBs + server slots

---

# Electronics & Electricity API — Complete Reference & VexScript Integration

**Version:** 1.0
**Last updated:** 2025-10-04
**Audience:** server implementers (Kotlin), scripters (VexScript), modders

This document extends the Cobalt/electronics design with a **complete API** surface: *Host* (Kotlin) interfaces the server must implement, *Device/Network/Bus* functions, *Events*, *Diagnostics*, *Security (capabilities)* — and a ready-to-drop **VexScript standard library** (`electrics.vex`) that exposes the API ergonomically to script authors.

---

## Contents (quick jump)

1. Design goals & integration notes
2. Core data types (JSON-like / interfaces)
3. Host (Kotlin) API interfaces — complete signatures
4. Device / Bus API (Kotlin) — device-facing functions
5. Network API (packetized / routing)
6. Tools & Diagnostics API
7. Events & payloads (VexScript event names + shapes)
8. Capabilities & security mapping
9. VexScript bindings: `electrics.vex` (full code)
10. Examples (VexScript)
11. Server implementer notes & serialization
12. Next steps & checklist

---

# 1 — Design goals & integration notes

* Provide concrete, typed Kotlin interfaces for runtime implementers. These are the canonical Host API.
* Provide VexScript wrappers (pure `.vex`) that call into Host APIs exposed to the script sandbox (via `Host.*` module). The VexScript library simplifies common tasks and enforces capability checks.
* Keep network, device, and bus APIs separate so implementations can optimize per-module (e.g., Cobalt bus vs packet network).
* Events are fired into VexScript `Event.on(:...)` handlers with strict payloads.

---

# 2 — Core data types

Below are the main data types used in APIs. They are shown in typed pseudo-code for clarity.

```kotlin
// Vec3
data class Vec3(val x:Int, val y:Int, val z:Int)

// Face enum
enum class Face { NORTH, SOUTH, EAST, WEST, UP, DOWN, SLOPE_NW, SLOPE_NE, SLOPE_SW, SLOPE_SE, SLOPE_N, SLOPE_W, SLOPE_S, SLOPE_E }

// SignalSnapshot
data class SignalSnapshot(
  val digital: Boolean,
  val analog: Int,      // 0..ANALOG_MAX
  val flux: Int,        // 0..flux buffer
  val freqChannel: Int?,// RF channel or null
  val timestamp: Long
)

// TraceInfo
data class TraceInfo(
  val loc: Vec3,
  val face: Face,
  val layer: Int,
  val material: String, // "cobalt", "runic_cobalt"
  val insulated: Boolean,
  val lastSignal: SignalSnapshot,
  val heat: Double,
  val safeCurrentCu: Int
)

// DeviceInfo
data class DeviceInfo(
  val id: String,
  val type: String,
  val loc: Vec3,
  val ports: Map<String, PortSpec>,
  val meta: Map<String, Any>
)

// PortSpec
data class PortSpec(
  val name: String,
  val types: Set<String> // e.g., {"digital","analog","flux","packet"}
)

// NetworkGraph (simple)
data class NetworkGraph(
  val nodes: List<String>, // device ids or trace ids
  val edges: List<Pair<String,String>>
)

// Packet
data class Packet(val src: String?, val dest: String?, val port: Int, val payload: ByteArray, val reliable: Boolean)
```

---

# 3 — Host (Kotlin) API interfaces — full signatures

The `Host` object is exposed to Kotlin runtime and VexScript as `Host.*`. Server implementers must provide these.

> NOTE: methods return `Result<T, HostError>` in production code. For readability we show direct returns and throw `HostException` on critical failures.

### `Host.CobaltService` — world & trace control

```kotlin
interface CobaltService {
  // Placement & query
  fun placeTrace(loc: Vec3, face: Face, layer: Int = 0, material: String = "cobalt", insulated: Boolean = false): Boolean
  fun removeTrace(loc: Vec3, face: Face, layer: Int = 0): Boolean
  fun getTrace(loc: Vec3, face: Face, layer: Int = 0): TraceInfo?
  fun scanNetwork(center: Vec3, radius: Int = 16): NetworkGraph
  fun getGraphConnectedTo(loc: Vec3, face: Face, layer: Int = 0): NetworkGraph

  // Low-level IO
  fun readSignal(loc: Vec3, face: Face, layer: Int = 0): SignalSnapshot
  fun writeSignal(loc: Vec3, face: Face, layer: Int = 0,
                  digital: Boolean? = null, analog: Int? = null, flux: Int? = null, freqChannel: Int? = null): Boolean
  fun emitPulse(loc: Vec3, face: Face, layer: Int = 0, durationTicks: Int, analogValue: Int = 15, fluxValue: Int = 0, freqChannel: Int? = null)
  fun createVia(loc1: Vec3, face1: Face, layer1: Int, loc2: Vec3, face2: Face, layer2: Int): Boolean

  // Admin / configuration
  fun setConfig(key: String, value: Any)
  fun getConfig(key: String): Any?
}
```

### `Host.DeviceRegistry` — device discovery & control

```kotlin
interface DeviceRegistry {
  fun findDeviceById(id: String): DeviceHandle?
  fun findDevicesByType(type: String, center: Vec3?, radius: Int? = null): List<DeviceHandle>

  fun registerDevice(deviceInfo: DeviceInfo): String // returns id
  fun unregisterDevice(id: String): Boolean
}
```

`DeviceHandle` includes device-level control API (see section 4).

### `Host.NetworkService` — packetized networking & routing

```kotlin
interface NetworkService {
  fun sendPacket(packet: Packet): String // returns packet handle/id
  fun broadcastPacket(port: Int, payload: ByteArray, reliable: Boolean = true): String
  fun routeGraph(graph: NetworkGraph, policy: RoutePolicy): RouteResult
  fun scanPacketNetwork(center: Vec3, radius: Int): NetworkGraph
}
```

### `Host.Tools` — diagnostics

```kotlin
interface ToolsService {
  fun attachMultimeter(loc: Vec3, face: Face, layer: Int = 0): MultimeterHandle
  fun attachOscilloscope(loc: Vec3, face: Face, layer: Int = 0): OscilloscopeHandle
  fun attachLogicAnalyzer(loc: Vec3, pins: List<Int>): LogicAnalyzerHandle
}
```

### `Host.Security` — capability checks & tokens

```kotlin
interface SecurityService {
  fun checkCapability(scriptId: String, capability: String): Boolean
  fun requireCapability(scriptId: String, capability: String) // throws if missing
  fun signTokenForDevice(deviceId: String, expiryTicks: Long): String
}
```

---

# 4 — Device / Bus API (Kotlin) — device-facing functions

Devices are represented by `DeviceHandle` objects provided to scripts (e.g., when they find a device). A device exposes `io`, `net`, `power`, `hw`. Below is the minimal device interface.

```kotlin
interface DeviceHandle {
  val id: String
  val info: DeviceInfo

  // IO — read/write ports, pins
  val io: DeviceIO
  // Network
  val net: DeviceNetwork
  // Power & thermal
  val power: DevicePower
  // Hardware control / metadata
  val hw: DeviceHw

  fun configure(params: Map<String,Any>): Boolean
  fun getStatus(): DeviceStatus
}

interface DeviceIO {
  fun readDigital(port: String): Boolean
  fun writeDigital(port: String, value: Boolean): Boolean
  fun readAnalog(port: String): Int
  fun writeAnalog(port: String, value: Int): Boolean
  fun readFlux(port: String): Int
  fun writeFlux(port: String, value: Int): Boolean
  fun readPin(pinIndex: Int): Int
  fun writePin(pinIndex: Int, value: Int): Boolean
  fun readCobalt(loc: Vec3, face: Face, layer: Int = 0): SignalSnapshot
  fun writeCobalt(loc: Vec3, face: Face, layer: Int = 0, digital: Boolean? = null, analog: Int? = null, flux:Int? = null, freqChannel:Int? = null): Boolean
}

interface DeviceNetwork {
  fun sendPacket(port: Int, dest: String, payload: ByteArray, reliable: Boolean = true): String
  fun registerPacketHandler(port: Int, handlerId: String)
  fun unregisterPacketHandler(port: Int, handlerId: String)
}

interface DevicePower {
  fun getPowerDraw(): Double  // watts
  fun getVoltage(): Double
  fun getTemperature(): Double
  fun setPowerLimit(watts: Double): Boolean
}

interface DeviceHw {
  fun getMeta(): Map<String,Any>
  fun flashFirmware(firmwareBytes: ByteArray): Boolean
}
```

---

# 5 — Network API (packetized / routing)

High-level packet network for `Network Cable` and `Fiber`. The system is separate from per-trace cobalt IO but can be tied by bridging devices.

```kotlin
data class RoutePolicy(val preferFiber:Boolean=false, val qos: Map<String,Any> = emptyMap())

data class RouteResult(val success:Boolean, val installedRoutes: Int, val errors: List<String>)

interface PacketHandler {
  fun onPacket(packet: Packet)
}
```

`NetworkService.sendPacket` must validate reachability via routing layer and may cause `cobalt_signal_change` events if a packet triggers physical IO.

---

# 6 — Tools & Diagnostics API

`MultimeterHandle`, `OscilloscopeHandle`, and `LogicAnalyzerHandle` expose capture and data APIs:

```kotlin
interface MultimeterHandle {
  fun read(): MultimeterRead // voltage, current, analog, digital, flux
  fun detach()
}

data class MultimeterRead(val voltage: Double, val currentCu: Int, val analog:Int, val digital:Boolean, val flux:Int)

interface OscilloscopeHandle {
  fun startCapture(sampleRateHz: Int, durationTicks: Int)
  fun getWaveform(): ByteArray // server-native binary
  fun detach()
}

interface LogicAnalyzerHandle {
  fun startCapture(pins: List<Int>, sampleRateHz: Int, durationTicks: Int)
  fun getDecoded(protocol: String): Any // decoded bus data (I2C, SPI...)
  fun detach()
}
```

These are expensive operations and must be rate-limited by runtime.

---

# 7 — Events & payloads (VexScript integration)

All events appear in VexScript via `Event.on(:event_name)` with typed payloads. Key cobalt/electronics events:

* `:cobalt_signal_change` — payload:

  ```js
  {
    source_loc: Vec3, face: Symbol, layer: Int,
    old: SignalSnapshot, new: SignalSnapshot,
    device_id: String|null
  }
  ```
* `:device_registered` — payload `{ device: DeviceInfo }`
* `:device_unregistered` — `{ deviceId: String }`
* `:cobalt_overheat` — `{ loc, face, layer, heat, device_id? }`
* `:flux_threshold_crossed` — `{ loc, layer, old_flux, new_flux, threshold }`
* `:packet_received` — when a device receives a packet: `{ deviceId, port, packet }`
* `:tool_capture_complete` — `{ toolId, handleId, dataReference }`

VexScript handlers receive the event payload as a Ruby-like object. Example:

```vex
Event.on(:cobalt_signal_change) do |ev|
  puts "Cobalt at #{ev.source_loc} changed: analog #{ev.new.analog}"
end
```

---

# 8 — Capabilities & security mapping

Scripts must declare and be granted capabilities. Host checks every sensitive API call.

**Capabilities**

* `Cobalt.IO` — read/write arbitrary trace state (`readSignal`, `writeSignal`, `emitPulse`).
* `Cobalt.Place` — place/remove traces (`placeTrace`, `removeTrace`).
* `Cobalt.Admin` — change cobalt config, `createVia`, forced unlocks.
* `Device.Flash` — `flashFirmware` on devices.
* `Network.Send` — send arbitrary packets on network (`NetworkService`).
* `Tools.Use` — attach expensive tools (oscilloscope / logic analyzer).
* `Explosives.Control` — arm/disarm/trigger explosives — admin only.

VexScript `Host` wrappers must call `Host.Security.requireCapability(scriptId, capability)` before invoking the host.

---

# 9 — VexScript bindings: `electrics.vex` (complete library)

Drop this file into `scripts/electrics.vex`. It wraps Host APIs and provides helper utilities. It assumes the runtime exposes `Host.Cobalt`, `Host.Device`, `Host.Network`, `Host.Tools`, `Host.Security` — names can be adapted.

> This is complete VexScript code using the syntax described earlier.

```vex
# scripts/electrics.vex
# VEX-CAPABILITIES: ["Cobalt.IO","Cobalt.Place","Device.Flash","Network.Send","Tools.Use"]

# electrics.vex - VexScript helper library for electronics & cobalt
module Electrics
  ANALOG_MAX = 15

  # Wrapper around Host\Security to check capability
  fn require_cap(scriptId: String, cap: String) do
    if not Host.Security.checkCapability(scriptId, cap)
      raise("Missing capability #{cap} for script #{scriptId}")
    end
  end

  # Placement helpers
  fn place_cobalt(loc: Hash, face: Symbol, layer: Int = 0, material: String = "cobalt", insulated: Bool = false) -> Bool do
    # script id from runtime environment
    sid = Script.meta.id()
    Host.Security.requireCapability(sid, "Cobalt.Place")
    return Host.Cobalt.placeTrace(loc, face, layer, material, insulated)
  end

  fn remove_cobalt(loc: Hash, face: Symbol, layer: Int = 0) -> Bool do
    sid = Script.meta.id()
    Host.Security.requireCapability(sid, "Cobalt.Place")
    return Host.Cobalt.removeTrace(loc, face, layer)
  end

  # Read/Write API
  fn read_trace(loc: Hash, face: Symbol, layer: Int = 0) do
    sid = Script.meta.id()
    Host.Security.requireCapability(sid, "Cobalt.IO")
    return Host.Cobalt.readSignal(loc, face, layer)
  end

  fn write_trace(loc: Hash, face: Symbol, layer: Int = 0, digital: Bool? = nil, analog: Int? = nil, flux: Int? = nil, freq: Int? = nil) -> Bool do
    sid = Script.meta.id()
    Host.Security.requireCapability(sid, "Cobalt.IO")
    # clamp analog
    if analog != nil
      analog = [0, [ANALOG_MAX, analog].min].max
    end
    return Host.Cobalt.writeSignal(loc, face, layer, digital, analog, flux, freq)
  end

  fn emit_pulse(loc: Hash, face: Symbol, layer: Int = 0, ticks: Int = 1, analog: Int = ANALOG_MAX, flux: Int = 0, freq: Int? = nil) do
    sid = Script.meta.id()
    Host.Security.requireCapability(sid, "Cobalt.IO")
    Host.Cobalt.emitPulse(loc, face, layer, ticks, analog, flux, freq)
  end

  # Device helpers
  fn find_device(id: String) do
    return Host.Device.findDeviceById(id)
  end

  fn flash_firmware(deviceId: String, firmwareBytes) do
    sid = Script.meta.id()
    Host.Security.requireCapability(sid, "Device.Flash")
    dev = Host.Device.findDeviceById(deviceId)
    if dev == nil
      raise("Device #{deviceId} not found")
    end
    return dev.hw.flashFirmware(firmwareBytes)
  end

  # Network helpers
  fn send_packet(deviceId: String, port: Int, dest: String, payload: String, reliable: Bool = true) do
    sid = Script.meta.id()
    Host.Security.requireCapability(sid, "Network.Send")
    dev = Host.Device.findDeviceById(deviceId)
    if dev == nil
      raise("Device #{deviceId} not found")
    end
    return dev.net.sendPacket(port, dest, payload.to_bytes(), reliable)
  end

  # Diagnostics wrappers
  fn read_multimeter(loc: Hash, face: Symbol, layer: Int = 0) do
    sid = Script.meta.id()
    Host.Security.requireCapability(sid, "Tools.Use")
    mm = Host.Tools.attachMultimeter(loc, face, layer)
    val = mm.read()
    mm.detach()
    return val
  end

  fn capture_scope(loc: Hash, face: Symbol, layer: Int = 0, sampleRate: Int = 1000, duration: Int = 20) do
    sid = Script.meta.id()
    Host.Security.requireCapability(sid, "Tools.Use")
    sc = Host.Tools.attachOscilloscope(loc, face, layer)
    sc.startCapture(sampleRate, duration)
    data = sc.getWaveform()
    sc.detach()
    return data
  end

  # Network scan helper
  fn scan_network(center: Hash, radius: Int = 16) do
    sid = Script.meta.id()
    Host.Security.requireCapability(sid, "Cobalt.IO")
    return Host.Cobalt.scanNetwork(center, radius)
  end

  # Utility: automatic extender placement along path (simple)
  fn auto_extend_path(path: Array<Hash>) do
    sid = Script.meta.id()
    Host.Security.requireCapability(sid, "Cobalt.Place")
    # place extenders every MAX_SEGMENTS_BEFORE_REPEATER (read from Host.Cobalt.getConfig)
    cfgMax = Host.Cobalt.getConfig("max_segments_before_repeater") ?: 64
    step = cfgMax as Int
    i = 0
    while i < path.length do
      p = path[i]
      # place an extender device (this requires an external device placement workflow)
      Host.Cobalt.placeTrace(p, :UP, 0, "cobalt", false)
      i += step
    end
    return true
  end

end # module
```

> The above VexScript library assumes host-exposed objects: `Host.Cobalt`, `Host.Device`, `Host.Tools`, `Host.Security`, and `Script.meta.id()` to get script identity. Implement these names when exposing host functions.

---

# 10 — Examples (VexScript)

### Example A — Monitor a trace and auto-cool overheated traces

```vex
# monitor_and_cool.vex
Event.on(:cobalt_overheat) do |ev|
  loc = ev.loc
  # If underwater, water cooling; else, attempt to place a cooling fan device (requires admin caps)
  if World.is_water_block(loc)
    Server.log("Auto-cooled trace at #{loc} by water.")
  else
    # Place a fan device or schedule a reroute
    Server.log("Overheat at #{loc}. Scheduling reroute.")
    # Maybe flip a breaker:
    Device.find("psu-1").power.setPowerLimit(0)
  end
end
```

### Example B — Simple RF beacon (frequency multiplex)

```vex
# rf_beacon.vex
fn beacon_tick() do
  loc = { x:100, y:64, z:200 }
  Electrics.write_trace(loc, :UP, 0, digital: true, freq: 3)
  await Time.sleep(2)
  Electrics.write_trace(loc, :UP, 0, digital: false)
end

spawn do
  while true
    await beacon_tick()
    await Time.sleep(20)
  end
end
```

---

# 11 — Server implementer notes & serialization

* **Exposing Host to Scripts**

    * Expose `Host.Cobalt`, `Host.Device`, `Host.Network`, `Host.Tools`, `Host.Security` as objects/methods accessible from the VexScript sandbox. Implement capability checks in `Host.Security` before executing operations.
    * `DeviceHandle` objects passed to scripts should be thin proxies calling underlying server code.

* **Event firing**

    * When `writeSignal` changes a snapshot, compute connected nodes whose state changes and fire `:cobalt_signal_change` with old/new snapshots.
    * Fire `:flux_threshold_crossed` when flux crosses configured thresholds.
    * Fire `:cobalt_overheat` when heat crosses `OVERHEAT_TEMP` constant.

* **Persistence**

    * Save `TraceInfo.lastSignal`, `material`, `insulated`, `heat`, and `layer` to chunk data. Also save devices with `DeviceInfo` and per-device `meta`.

* **Rate-limits**

    * Enforce server-side rate limits for `ToolsService` captures; fail or queue heavy requests.
    * Limit high-frequency `writeSignal` calls from untrusted scripts to avoid DoS.

* **Security**

    * `flashFirmware` must require `Device.Flash`; validate firmware signatures or require admin approval for firmware that touches network/persistent storage.
    * `Explosives.Control` gating as described earlier.

---

# 12 — Next steps & checklist for delivery

If you want, I can now produce:

* A. **Kotlin interface file** (`HostApi.kt`) containing the exact interfaces above (copy/paste ready).
* B. **VexScript standard library packaged** (zipped) with the `electrics.vex` and helper docs.
* C. **Test harness**: a sample Kotlin micro-runtime stub implementing the Host interfaces so you can run VexScript test scripts locally (hello-world style).
* D. **Full JSON schemas** for serialization of `TraceInfo`, `SignalSnapshot`, `DeviceInfo`, `NetworkGraph`.

Which do you want next? I can produce A (Kotlin file) right away in this reply if you'd like.
