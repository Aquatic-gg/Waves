package gg.aquatic.execute

import gg.aquatic.execute.condition.ConditionHandle
import org.bukkit.Bukkit
import org.bukkit.entity.Player

suspend fun <T> Collection<ConditionHandle<T>>.checkConditions(
    binder: T,
    textUpdater: (T, String) -> String = { _, str -> str }
): Boolean {
    for (configuredRequirement in this) {
        if (!configuredRequirement.execute(binder, textUpdater)) return false
    }
    return true
}

suspend fun <T : Any> Collection<ActionHandle<T>>.executeActions(
    binder: T,
    textUpdater: (T, String) -> String = { _, str -> str }
) {
    for (configuredAction in this) {
        configuredAction.execute(binder, textUpdater)
    }
}

suspend fun Collection<ActionHandle<Player>>.broadcastActions(textUpdater: (Player, String) -> String = { _, str -> str }) {
    for (player in Bukkit.getOnlinePlayers()) {
        for (configuredAction in this) {
            configuredAction.execute(player, textUpdater)
        }
    }
}