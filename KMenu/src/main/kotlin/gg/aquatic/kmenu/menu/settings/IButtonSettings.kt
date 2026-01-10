package gg.aquatic.kmenu.menu.settings

import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.MenuComponent
import gg.aquatic.replace.placeholder.PlaceholderContext

interface IButtonSettings {

    fun create(updater: PlaceholderContext<Menu>, click: suspend (AsyncPacketInventoryInteractEvent) -> Unit = {}): MenuComponent

}