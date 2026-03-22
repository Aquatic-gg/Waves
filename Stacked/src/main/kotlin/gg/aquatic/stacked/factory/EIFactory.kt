package gg.aquatic.stacked.factory

import com.ssomar.score.api.executableitems.ExecutableItemsAPI
import gg.aquatic.stacked.ItemFactory
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.jvm.optionals.getOrNull

object EIFactory : ItemFactory {
    override fun create(id: String): ItemStack? {
        return ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(id)?.getOrNull()
            ?.buildItem(1, Optional.empty())
    }
}