package gg.aquatic.dispatch.paper

import gg.aquatic.dispatch.BaseCtx
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.concurrent.CompletableFuture

class BukkitCtx(
    val plugin: Plugin
) : BaseCtx(
    check = {
        plugin.isEnabled && !Bukkit.getServer().isPrimaryThread
    },
    execution = { runnable ->
        val future = CompletableFuture<Void>()
        plugin.server.scheduler.runTask(plugin, Runnable {
            try {
                runnable.run()
            } finally {
                future.complete(null)
            }
        })
        future.join()
    },
    logger = plugin.logger
)