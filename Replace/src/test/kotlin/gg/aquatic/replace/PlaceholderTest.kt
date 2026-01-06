package gg.aquatic.replace

import gg.aquatic.replace.placeholder.Placeholder
import gg.aquatic.replace.placeholder.Placeholders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PlaceholderTest {

    @Test
    fun `test literal placeholder replacement`() {
        // Use a simple String as the target instead of a Player object
        val binder = "Larkyy"

        val namePlaceholder = Placeholder.Literal<String>("name") { target, _ ->
            target
        }

        val context = Placeholders.resolverFor(String::class.java, 5)
        context.addPlaceholder(namePlaceholder)

        val item = context.createItem(binder, "Hello %name%!")

        assertEquals("Hello Larkyy!", item.latestState.value)
    }

    @Test
    fun `test context transformation`() {
        data class User(val name: String)
        val binder = User("Aquatic")

        val namePlaceholder = Placeholder.Literal<String>("name") { target, _ -> target }
        Placeholders.register(String::class.java, namePlaceholder)

        val userContext = Placeholders.resolverFor<User>(
            maxUpdateInterval = 5,
            transforms = arrayOf(Placeholders.Transform { it.name })
        )

        val item = userContext.createItem(binder, "Welcome %name%")
        assertEquals("Welcome Aquatic", item.latestState.value)
    }
}
