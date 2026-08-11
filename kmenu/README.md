# KMenu

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/kmenu/badge)](https://www.codefactor.io/repository/github/mrlarkyy/kmenu)
[![Reposilite](https://repo.aquatic.gg/api/badge/latest/releases/gg/aquatic/kmenu?color=40c14a&name=Reposilite)](https://repo.aquatic.gg/#/releases/gg/aquatic/kmenu)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-blue.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

A packet-based, asynchronous Minecraft menu framework for Paper. It manages inventory windows entirely through packets,
which makes it unit-testable without a running server.

## Key Features

* **No Bukkit inventories:** Manages windows through packets via [Pakket](https://github.com/aquatic/Pakket).
* **Async:** Built for Kotlin coroutines with non-blocking updates.
* **Packet-efficient:** Sends only the slots that actually changed.
* **Reactive components:** Buttons and lists update without re-creating objects.
* **Slot management:** Priorities, overlaps, rectangles, and ranges.

### Why KMenu?

KMenu operates entirely on the client side, which has a few consequences:

1. **Item security:** Items are virtual, so players cannot remove them through inventory glitches.
2. **Button logic:** Every slot behaves like a programmable button.
3. **No container ticking:** There is no server-side container to tick or keep in sync.

### Technical Overview

KMenu bypasses the Bukkit `InventoryView` system. It listens to packet events, processes click logic internally, and
sends window packets directly to the client. This allows custom container types and titles that the native API does not
expose.

---

## Installation

Add the repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.aquatic.gg/releases")
}

dependencies {
    // Core runtime
    implementation("gg.aquatic:kmenu:26.0.54")

    // Optional: configuration serialization (Execute + Stacked)
    implementation("gg.aquatic:kmenu-serialization:26.0.54")

    // Aquatic Libraries (core)
    implementation("gg.aquatic:pakket:26.0.54")
    implementation("gg.aquatic:kregistry:26.0.54")
    implementation("gg.aquatic:replace:26.0.54")
    implementation("gg.aquatic:kevent:26.0.54")
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

KMenu keeps menu logic separate from Bukkit. The `SlotManager` handles priority and ownership calculations using plain
Kotlin math (`y * 9 + x`), so it is unit-testable without a server.

### Packet Saver

Before sending a slot update, KMenu performs:
1. **Reference check** (same object)
2. **Basic check** (amount + type)
3. **Deep check** (`isSimilar`)
This avoids sending redundant slot-update packets.

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

- Discord: [Aquatic Development](https://discord.com/invite/ffKAAQwNdC)
- Issues: open a ticket on GitHub for bugs or feature requests.
