# KMenu

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/kmenu/badge)](https://www.codefactor.io/repository/github/mrlarkyy/kmenu)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/KMenu?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/KMenu)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

A high-performance, **completely packet-based**, asynchronous, and reactive Minecraft Menu framework for PaperMC.
Designed to be lightweight, packet-efficient, and easy to unit test.

## 🚀 Key Features

* **Zero Bukkit Inventories:** Uses pure packets for window management via [Pakket](https://github.com/aquatic/Pakket).
* **Asynchronous & Coroutine-based**: Built from the ground up to support Kotlin Coroutines.
* **Packet-Efficient**: Uses a 'Packet Saver' logic to compare item states and only send updates when visually
  necessary.
* **Reactive Components**: Buttons and lists update dynamically without re-creating objects.
* **Advanced Slot Management**: Built-in support for priorities, overlaps, and complex geometry (rectangles, ranges).

### ❓ Why KMenu?

Unlike standard GUI APIs, KMenu operates **entirely on the client-side**.

**Who should use this?**
*   **Server Owners** building complex navigation menus, shops, or profile views.
*   **Developers** who want absolute control over the inventory behavior without fighting Bukkit's internal `InventoryView` state.

**The "Ghost" Advantage:**
Because the inventory logic is handled via packets:
1.  **Item Security:** The items displayed are "virtual." Players cannot "steal" items through glitches because the server doesn't believe they are holding anything.
2.  **Button Logic:** Every slot behaves as a programmable button rather than a storage container.
3.  **Performance:** No overhead from the server-side `Container` logic, ticking, or player-inventory synchronization.


### 🛠️ Technical Overview

KMenu bypasses the standard Bukkit/Spigot `InventoryView` system. It listens to incoming packet events, processes click
logic internally, and sends `WindowItems` packets directly to the client. This allows for custom container types and
titles without the limitations of the native API.

---

## 🛠 Installation

Add the repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
}

dependencies {
    implementation("gg.aquatic:KMenu:26.0.1")

    // Aquatic Libraries
    implementation("gg.aquatic:Pakket:26.1.1")
    implementation("gg.aquatic:KRegistry:25.0.1")
    implementation("gg.aquatic.execute:Execute:26.0.1")
    implementation("gg.aquatic.replace:Replace:26.0.2")
    implementation("gg.aquatic:Stacked:26.0.1")
    implementation("gg.aquatic:KEvent:1.0.4")

    // Left-Right Caching
    implementation("io.github.charlietap:cachemap:0.2.4")
    implementation("io.github.charlietap:cachemap-suspend:0.2.4")
}
```

---

## 💻 Code Showcase

### 1. Creating a Basic Menu

Menus are built using components that automatically handle their own rendering and interaction logic.

```kotlin
// INITIALIZE THE LIBRARY & TICKER FIRST!
KMenu.initialize()

/*
Add to your coroutine ticker (every tick)
I did not want to create my own ticker, 
as you could have had your own one already made...
 */
MenuHandler.tick()

val menu = PrivateMenu(player, Component.text("My Menu"), InventoryType.GENERIC9X3, true)

val button = Button(
    id = "example_btn",
    itemstack = ItemStack(Material.DIAMOND),
    slots = listOf(13),
    priority = 1,
    updateEvery = 20, // Ticks
    textUpdater = placeholderContext,
    onClick = { event ->
        player.sendMessage("You clicked a diamond!")
    }
)

menu.addComponent(button)
menu.open()
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

The `ListMenu` handles pagination, searching, and filtering with built-in component re-use to prevent flickering and
packet spam.

```kotlin
class MyList(player: Player, items: List<MyItem>) : ListMenu<MyItem>(
    title = Component.text("Item List"),
    type = InventoryType.GENERIC9X6,
    player = player,
    entries = items.map { item ->
        Entry(
            value = item,
            itemVisual = { ItemStack(Material.PAPER) },
            searchString = item.name,
            placeholderContext = myContext,
            onClick = { /* Handle click */ }
        )
    },
    defaultSorting = Sorting.empty(),
    entrySlots = SlotSelection.rect(10, 43).slots
)
```

---

## 🚀 Kotlin DSL Showcase

The most intuitive way to build menus in Kotlin.

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

menu.open()
```

---

## 🏗 Performance & Architecture

### SlotManager (Pure Logic)

KMenu decouples menu logic from Bukkit's heavy registry system. The `SlotManager` handles all priority and ownership
calculations using pure Kotlin math ($y \times 9 + x$), making it lightning-fast and **100% unit-testable** without a
Minecraft server.

### Packet Saver

The framework caches the visual state of the inventory. Before sending an update to the player, it performs a
triple-check:

1. **Reference check** (same object?)
2. **Basic check** (Amount + Type match?)
3. **Deep check** (NBT/isSimilar match?)
   This ensures zero redundant packets are sent, significantly improving network stability for high-player-count
   servers.

### Reactive ListMenu

The `ListMenu` implementation re-uses `Button` objects across page changes and searches. Instead of clearing the
inventory and redrawing (which causes items to flicker), it simply updates the internal state of the existing buttons.

---

## 🧪 Unit Testing

Verify your UI logic in milliseconds:

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

## 📜 Documentation

| Class            | Description                                          |
|:-----------------|:-----------------------------------------------------|
| `Menu`           | Base class for packet-based inventories.             |
| `PrivateMenu`    | A menu bound to a specific player.                   |
| `MenuComponent`  | Abstract base for all UI elements.                   |
| `Button`         | Standard interactive component.                      |
| `SlotSelection`  | Utility for defining slot groups (Rect, Range, etc). |
| `SlotManager`    | Internal logic handler for slot ownership.           |
| `MenuSerializer` | Utility to load menu configurations from YAML.       |

---

## 💬 Community & Support

Got questions, need help, or want to showcase what you've built with **Pakket**? Join our community!

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

* **Discord**: [Join the Aquatic Development Discord](https://discord.com/invite/ffKAAQwNdC)
* **Issues**: Open a ticket on GitHub for bugs or feature requests.