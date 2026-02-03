package gg.aquatic.stacked.factory

import com.ssomar.score.api.executableitems.ExecutableItemsAPI
import com.ssomar.score.sobject.InternalData
import gg.aquatic.stacked.ItemFactory
import gg.aquatic.stacked.ItemHandler
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

object EIFactory : ItemFactory {
    override fun create(id: String): ItemStack? {
        return ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(id)?.getOrNull()
            ?.buildItem(1, null as? InternalData)
    }
}