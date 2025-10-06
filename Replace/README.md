## What's this?
Replace is a library that allows you to easily create placeholders for your plugins.

- Smart Placeholder updating (elimination of unneeded context updates - useful for limiting packet usage)
- Global placeholders
- Context-based updating
- Generic Placeholders & Contexts
- Limiting the updating interval
- Support for Literal, Kyori Components, ItemStacks

## Example Usage

### Creating Placeholders

````kt 
// True means that the placeholder is a constant. While set to true, the placeholder will retrieve
// the value just once and never get updated again (never executed the lambda again)
val placeholder = Placeholder<Player>("player", true) { player, foundString ->
    return@Placeholder player.name
}

val argumentPlaceholder = Placeholder<Player>("argument", false) { player, foundString ->
    val arguments = foundString.split("_")

    if (arguments.size < 2) {
        return@Placeholder ""
    }

    val value = arguments[1] // Getting custom parameter from your placeholder
    when (value.lowercase()) {
        "name" -> return@Placeholder player.name
        "health" -> return@Placeholder player.health.toString()
    }

    return@Placeholder foundString
}
````

### Registering custom global placeholders

This registers the global placeholders.
Global means that while you are creating a PlaceholderContext, the placeholders will get automatically added to it
depending on the binder T (generic type).

````kt
// Reified
Placeholders.register(placeholder, argumentPlaceholder)

Placeholders.register(Player::class.java, placeholder, argumentPlaceholder)
````

### Creation of PlaceholderContext

PlaceholderContext is a context handler for your placeholder. You can create as many contexts as you want or
create a global context too. However, have in mind that global contexts do not work well while using Player-based 
placeholders. 

I would highly recommend using void-based placeholders & contexts for global usage!

````kt 
// 5 stands for the updating interval in ticks. You can easily limit how often the placeholders get updated.
val globalContext = Placeholders.resolverFor<Void>(5)

val playerContext = Placeholders.resolverFor<Player>(10)

val entityContext = Placeholders.resolverFor<Entity>(100)
````

### Retrieving PlaceholderContext items

````kt 
fun something() {
    // 5 stands for the updating interval in ticks. You can easily limit how often the placeholders get updated.
    val playerContext = Placeholders.resolverFor<Player>(5)
    Placeholders.Transform<Something, Player> {
        it.player
    }
    val tranformed = Placeholders.resolverFor<Something>(
        5, Placeholders.Transform(
            Player::class.java
        ) {
            it.player
        })
    val transformedReified = Placeholders.resolverFor<Something>(
        5,
        Placeholders.Transform{
            it.player
        }
    )
}

class Something(
    val player: Player
)
````

### Updating PlaceholderContext items

````kt
fun something() {

    val player = Bukkit.getPlayer("MrLarkyy_")!!

    val component = Component.text("Hello %player%!")
    val context = PlaceholderContext.player

    val item = context.createItem(player, component)

    val firstUpdate = item.latestState
    if (firstUpdate.wasUpdated) {
        val newUpdatedValue by firstUpdate
    }

    val result = item.tryUpdate(player)
    if (result.wasUpdated) {
        val newUpdatedValue by result
    }
}
````