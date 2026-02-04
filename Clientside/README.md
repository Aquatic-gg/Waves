# Clientside

A lightweight, packet-based framework for client-side objects in Paper.
Create fake blocks, entities, and ModelEngine interactables that only exist on the client,
with audience filtering, view ranges, and clean interaction hooks.

## Key Features

- **Client-side blocks and entities:** Render objects without touching server world state.
- **Audience + view range:** Show objects only to matching players within range and tracked chunks.
- **Interaction hooks:** Listen for left/right clicks on fake blocks and entities.
- **Batch block updates:** Efficient multi-block updates per chunk.
- **ModelEngine support:** Optional ModelEngine interactables (MEG) with animations, tint, and skins.

## Installation

Add the Aquatic repository and dependency:

```kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

dependencies {
    implementation("gg.aquatic:Clientside:26.0.2")

    // Required Aquatic libs used by Clientside
    implementation("gg.aquatic:Common:26.0.13")
    implementation("gg.aquatic:Pakket:26.1.10")
    implementation("gg.aquatic:Dispatch:26.0.2")
    implementation("gg.aquatic:KEvent:26.0.5")
    implementation("gg.aquatic:Blokk:26.0.2")
    implementation("gg.aquatic:snapshotmap:26.0.2")
    implementation("gg.aquatic.execute:Execute:26.0.1")

    // Optional ModelEngine integration
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.9")
}
```

## Quick Start

Initialize the handler once at plugin startup:

```kotlin
override fun onEnable() {
    FakeObjectHandler.initialize()
}
```

## Usage

### Fake Block

```kotlin
val fakeBlock = FakeBlock(
    block = Blokk.of(Material.DIAMOND_BLOCK),
    location = someLocation,
    viewRange = 48,
    audience = AquaticAudience.online(),
    onInteract = { obj, player, isLeft ->
        player.sendMessage("Clicked ${obj.block.type} (left=$isLeft)")
    }
)

fakeBlock.register()
```

### Fake Entity

```kotlin
val fakeEntity = FakeEntity(
    type = EntityType.ARMOR_STAND,
    location = someLocation,
    viewRange = 64,
    audience = AquaticAudience.online(),
    consumer = {
        equipment[EquipmentSlot.HEAD] = ItemStack(Material.CARVED_PUMPKIN)
    },
    onInteract = { _, player, isLeft ->
        player.sendMessage("Interacted (left=$isLeft)")
    }
)

fakeEntity.register()
```

### Fake Multi-Block

```kotlin
val multi = FakeMultiBlock(
    multiBlokk = myMultiBlokk,
    location = baseLocation,
    viewRange = 32,
    initialAudience = AquaticAudience.online()
)

multi.register()
```

### ModelEngine Interactable (Optional)

```kotlin
val meg = FakeMEG(
    location = someLocation,
    modelId = "my_model",
    viewRange = 48,
    initialAudience = AquaticAudience.online(),
    onInteract = { _, player, _ ->
        player.sendMessage("Model clicked")
    }
)

meg.playAnimation("wave")
```

## How It Works

- Clientside objects are tracked by `FakeObjectHandler` and ticked every 50ms.
- Visibility is computed per player using audience checks, distance, and chunk tracking.
- Blocks use multi-block updates to minimize packets.
- Entities are created and updated via Pakket packet wrappers.

## Cleanup

Always destroy objects when you no longer need them:

```kotlin
fakeBlock.destroy()
fakeEntity.destroy()
multi.destroy()
meg.destroy()
```

## Requirements

- Paper `1.21.11` API
- Kotlin `2.3.0`
- Aquatic libraries listed in the dependencies section
- ModelEngine `R4.0.9` for `FakeMEG` (optional)
