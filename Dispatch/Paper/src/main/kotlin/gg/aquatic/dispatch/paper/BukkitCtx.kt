package gg.aquatic.dispatch.paper

import gg.aquatic.dispatch.BaseCtx
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import java.util.concurrent.CompletableFuture

sealed class BukkitCtx(
    val plugin: Plugin,
    check: () -> Boolean,
    execution: (Runnable) -> Unit
) : BaseCtx(
    check = {
        plugin.isEnabled && check()
    },
    execution = { runnable ->
        val future = CompletableFuture<Void>()
        execution {
            try {
                runnable.run()
            } finally {
                future.complete(null)
            }
        }
        future.join()
    },
    logger = plugin.logger
) {

    class Global(plugin: Plugin) : BukkitCtx(
        plugin,
        { !Bukkit.isGlobalTickThread() },
        { runnable ->
            Bukkit.getGlobalRegionScheduler().run(plugin) { runnable.run() }
        }
    )

    class OfLocation(plugin: Plugin, location: Location) : BukkitCtx(
        plugin,
        { !Bukkit.getServer().isOwnedByCurrentRegion(location) },
        { runnable ->
            Bukkit.getRegionScheduler().run(plugin, location) { runnable.run() }
        }
    )

    class OfEntity(plugin: Plugin, entity: Entity) : BukkitCtx(
        plugin,
        { !Bukkit.getServer().isOwnedByCurrentRegion(entity) },
        { runnable ->
            entity.scheduler.run(plugin, { runnable.run() }, null)
        }
    )
}
