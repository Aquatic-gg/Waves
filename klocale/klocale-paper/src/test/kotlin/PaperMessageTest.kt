package gg.aquatic.klocale.impl.paper

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaperMessageTest {

    private val plain = PlainTextComponentSerializer.plainText()

    @Test
    fun `test placeholder replacement`() {
        val message = PaperMessage.of(Component.text("Hello %player%!"))
        
        val replaced = message.replace("player", "Aquatic")
        
        // Use plain serializer to verify the content
        val resultText = plain.serialize(replaced.lines.first().component)
        assertEquals("Hello Aquatic!", resultText)
    }

    @Test
    fun `test static message detection`() {
        val staticMsg = PaperMessage.of(Component.text("No placeholders here"))
        val dynamicMsg = PaperMessage.of(Component.text("Has %placeholder%"))

        // We can't access private fields easily, but we can verify behavior
        // A static message should return 'this' when replace is called
        val replaced = staticMsg.replace("any", "thing")
        
        assertEquals(staticMsg, replaced, "Static messages should return themselves upon replacement")
    }

    @Test
    fun `test multi-line joining`() {
        val message = PaperMessage.of(
            Component.text("Line 1"),
            Component.text("Line 2")
        )
        
        assertEquals(2, message.lines.count())
    }
}
