# KMenu

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/kmenu/badge)](https://www.codefactor.io/repository/github/mrlarkyy/kmenu)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/KMenu?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/KMenu)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-blue.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

A high-performance, packet-based, asynchronous Minecraft menu framework for Paper.
Designed to be lightweight, packet-efficient, and easy to unit test.

## Key Features

* **Zero Bukkit Inventories:** Uses pure packets for window management via [Pakket](https://github.com/aquatic/Pakket).
* **Async by Design:** Built for Kotlin coroutines with non-blocking updates.
* **Packet-Efficient:** Sends only the slots that actually changed.
* **Reactive Components:** Buttons and lists update dynamically without re-creating objects.
* **Advanced Slot Management:** Priorities, overlaps, rectangles, and ranges.

### Why KMenu?

Unlike standard GUI APIs, KMenu operates entirely on the client side.

**Who should use this?**
* **Server owners** building complex navigation menus, shops, or profile views.
* **Developers** who want full control without Bukkit inventory quirks.

**The "Ghost" Advantage**
Because the inventory logic is handled via packets:
1. **Item Security:** Items are virtual; players cannot steal them via glitches.
2. **Button Logic:** Every slot behaves like a programmable button.
3. **Performance:** No server-side container ticking or sync overhead.

### Technical Overview

KMenu bypasses the Bukkit `InventoryView` system. It listens to packet events, processes click logic
internally, and sends window packets directly to the client. This allows for custom container types
and titles without the limitations of the native API.

---

## Installation

Add the repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
}

dependencies {
    // Core runtime
    implementation("gg.aquatic:KMenu:26.0.1")

    // Optional: configuration serialization (Execute + Stacked)
    implementation("gg.aquatic:KMenu-serialization:26.0.1")

    // Aquatic Libraries (core)
    implementation("gg.aquatic:Pakket:26.1.7")
    implementation("gg.aquatic:KRegistry:25.0.1")
    implementation("gg.aquatic.replace:Replace:26.0.2")
    implementation("gg.aquatic:KEvent:1.0.4")
}
```

---

## Code Showcase

### 1. Creating a Basic Menu

KMenu manages its own ticker internally. Initialize it once during plugin startup.

```kotlin
KMenu.initialize(plugin, scope)

val menu = PrivateMenu(player, Component.text("My Menu"), InventoryType.GENERIC9X3, true)

val button = Button(
    id = "example_btn",
    itemstack = ItemStack(Material.DIAMOND),
    slots = listOf(13),
    priority = 1,
    updateEvery = 20, // ticks
    textUpdater = placeholderContext,
    onClick = { event ->
        player.sendMessage("You clicked a diamond!")
    }
)

menu.addComponent(button)
menu.open(player)
```

### 2. Scrolling Buttons

Perfect for toggling settings or cycling through items.

```kotlin
val scrolls = listOf(
    ScrollingButton.Scroll(MyData.EASY) { ItemStack(Material.GREEN_WOOL) },
    ScrollingButton.Scroll(MyData.HARD) { ItemStack(Material.RED_WOOL) }
)

val scrollingBtn = ScrollingButton.create(
    menu = myMenu,
    slots = listOf(10),
    scroll = scrolls,
    placeholderContext = context,
    onScroll = { data -> println("Switched to $data") }
)
```

### 3. High-Performance Lists

The `ListMenu` handles pagination, searching, and filtering with built-in component re-use to prevent flickering
and packet spam.

```kotlin
class MyList(player: Player, items: List<MyItem>) : ListMenu<MyItem>(
    title = Component.text("Item List"),
    type = InventoryType.GENERIC9X6,
    player = player,
    entries = items.map { item ->
        Entry(
            value = item,
            itemVisual = { ItemStack(Material.PAPER) },
            placeholderContext = myContext,
            onClick = { /* Handle click */ }
        )
    },
    defaultSorting = Sorting.empty(),
    entrySlots = SlotSelection.rect(10, 43).slots
)
```

---

## Kotlin DSL Showcase

```kotlin
val menu = player.createMenu(Component.text("Main Menu")) {
    type = InventoryType.GENERIC9X3

    // Simple Button
    button("teleport_spawn", slot = 13) {
        item = ItemStack(Material.COMPASS)
        onClick {
            player.teleport(player.world.spawnLocation)
            player.sendMessage("Welcome home!")
        }
    }

    // Scrolling Toggle
    scrollingButton(
        slots = listOf(10),
        scrolls = listOf(
            Scroll("Easy") { ItemStack(Material.LIME_DYE) },
            Scroll("Hard") { ItemStack(Material.RED_DYE) }
        )
    ) { mode ->
        player.sendMessage("Difficulty set to $mode")
    }
}

menu.open(player)
```

---

## Performance & Architecture

### SlotManager (Pure Logic)

KMenu decouples menu logic from Bukkit's heavy registry system. The `SlotManager` handles priority and ownership
calculations using pure Kotlin math (`y * 9 + x`), making it lightning-fast and unit-testable without a server.

### Packet Saver

Before sending a slot update, KMenu performs:
1. **Reference check** (same object)
2. **Basic check** (amount + type)
3. **Deep check** (`isSimilar`)
This ensures zero redundant packets, improving network stability for large servers.

### Reactive ListMenu

`ListMenu` re-uses `Button` objects across page changes and searches. Instead of clearing and redrawing
(causing flicker), it updates the internal state of existing buttons.

---

## Unit Testing

```kotlin
@Test
fun `test priority ownership`() {
    val manager = SlotManager()
    manager.addComponent(MockComponent("low", listOf(0), 1))
    manager.addComponent(MockComponent("high", listOf(0), 10))

    assertEquals("high", manager.getTopComponentForSlot(0)?.id)
}
```

---

## Documentation

| Class            | Description                                          |
|:-----------------|:-----------------------------------------------------|
| `Menu`           | Base class for packet-based inventories.             |
| `PrivateMenu`    | A menu bound to a specific player.                   |
| `MenuComponent`  | Abstract base for all UI elements.                   |
| `Button`         | Standard interactive component.                      |
| `SlotSelection`  | Utility for defining slot groups (rect, range, etc). |
| `MenuSerializer` | Load menu configurations from YAML (serialization).  |

---

## Community & Support

Got questions or want to showcase what you've built with KMenu?

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

* **Discord**: [Join the Aquatic Development Discord](https://discord.com/invite/ffKAAQwNdC)
* **Issues**: Open a ticket on GitHub for bugs or feature requests.
