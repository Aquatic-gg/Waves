package gg.aquatic.waves.input.impl

import gg.aquatic.waves.input.AwaitingInput
import gg.aquatic.waves.input.Input
import gg.aquatic.waves.input.InputHandle
import gg.aquatic.waves.util.event.event
import gg.aquatic.waves.util.unregister
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.concurrent.CompletableFuture

object ChatInput : Input {

    private var listener: Listener? = null

    override val awaiting = HashMap<Player, AwaitingInput>()

    private fun initialize() {
        listener = event<AsyncPlayerChatEvent> {
            val handle = awaiting.remove(it.player) ?: return@event
            (handle.handle as Handle).handle(it, handle)
        }
    }

    private fun terminate() {
        listener?.unregister()
        listener = null
    }

    fun createHandle(cancelVariants: List<String> = listOf("cancel")): InputHandle {
        return Handle(cancelVariants)
    }

    class Handle(
        private val cancelVariants: List<String> = listOf("cancel")
    ) : InputHandle {
        override val input: Input = ChatInput

        override fun await(player: Player): CompletableFuture<String?> {
            val handle = AwaitingInput(player, CompletableFuture(), this)
            awaiting += player to handle

            if (listener == null) {
                initialize()
            }

            return handle.future
        }

        fun handle(event: AsyncPlayerChatEvent, awaitingInput: AwaitingInput) {
            val content = event.message

            if (content.lowercase() in cancelVariants) {
                awaitingInput.future.complete(null)
            } else {
                awaitingInput.future.complete(content)
            }
        }

        override fun forceCancel(player: Player) {
            val handle = awaiting[player] ?: return
            handle.future.complete(null)
            awaiting -= player

            if (awaiting.isEmpty()) {
                terminate()
            }
        }
    }
}