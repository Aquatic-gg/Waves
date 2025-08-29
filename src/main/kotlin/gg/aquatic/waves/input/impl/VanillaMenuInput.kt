package gg.aquatic.waves.input.impl

import gg.aquatic.waves.input.AwaitingInput
import gg.aquatic.waves.input.Input
import gg.aquatic.waves.input.InputHandle
import gg.aquatic.waves.inventory.InventoryType
import gg.aquatic.waves.menu.MenuComponent
import gg.aquatic.waves.menu.PrivateAquaticMenu
import gg.aquatic.waves.menu.component.Button
import gg.aquatic.waves.util.toMMComponent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture

object VanillaMenuInput : Input {
    override val awaiting = HashMap<Player, AwaitingInput>()

    fun createHandle(
        title: String,
        inventoryType: InventoryType,
        buttons: Collection<MenuComponent>,
        options: Collection<MenuOption>
    ): InputHandle {
        return Handle(title, inventoryType, buttons, options)
    }

    class Handle(
        val title: String,
        val inventoryType: InventoryType,
        val buttons: Collection<MenuComponent>,
        val options: Collection<MenuOption>
    ) : InputHandle {

        override val input: Input = VanillaMenuInput

        override fun await(player: Player): CompletableFuture<String?> {

            val menu = PrivateAquaticMenu(
                title.toMMComponent(),
                inventoryType,
                player,
                true
            )

            for (button in buttons) {
                menu.addComponent(button)
            }

            val future = CompletableFuture<String?>()
            for (option in options) {
                val button = Button(
                    "option_${option.id}",
                    option.item,
                    option.slots,
                    10,
                    10,
                    null,
                    { true },
                    { str, _ -> str },
                    { e ->
                        player.closeInventory()
                        future.complete(option.id)
                    }
                )
                menu.addComponent(button)
            }

            val awaitingInput = AwaitingInput(player, future, this)
            awaiting += player to awaitingInput

            menu.open()
            return future
        }

        override fun forceCancel(player: Player) {
            val handle = awaiting[player] ?: return
            handle.future.complete(null)
            awaiting -= player
        }
    }

    class MenuOption(
        val id: String,
        val item: ItemStack,
        val slots: Collection<Int>
    )
}