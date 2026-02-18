package gg.aquatic.common.coroutine

import gg.aquatic.common.AquaticCommon
import org.bukkit.Location
import org.bukkit.entity.Entity

object BukkitCtx {

    val GLOBAL by lazy {
        gg.aquatic.dispatch.paper.BukkitCtx.Global(AquaticCommon.plugin)
    }

    fun ofLocation(location: Location): gg.aquatic.dispatch.paper.BukkitCtx {
        return gg.aquatic.dispatch.paper.BukkitCtx.OfLocation(AquaticCommon.plugin, location)
    }

    fun ofEntity(entity: Entity): gg.aquatic.dispatch.paper.BukkitCtx {
        return gg.aquatic.dispatch.paper.BukkitCtx.OfEntity(AquaticCommon.plugin, entity)
    }
}