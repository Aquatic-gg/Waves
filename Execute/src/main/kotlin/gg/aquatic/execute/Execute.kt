package gg.aquatic.execute

import gg.aquatic.execute.action.impl.ActionbarAction
import gg.aquatic.execute.action.impl.CloseInventory
import gg.aquatic.execute.action.impl.CommandAction
import gg.aquatic.execute.action.impl.SoundAction
import gg.aquatic.execute.action.impl.SoundStopAction
import gg.aquatic.execute.action.impl.TitleAction
import gg.aquatic.execute.action.registerAction
import gg.aquatic.execute.requirement.impl.PermissionCondition
import gg.aquatic.execute.requirement.registerCondition
import gg.aquatic.kregistry.FrozenRegistry
import gg.aquatic.kregistry.MutableRegistry
import gg.aquatic.kregistry.MutableRegistryGraph
import gg.aquatic.kregistry.Registry
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin

object Execute {

    lateinit var miniMessage: MiniMessage
    lateinit var plugin: JavaPlugin

    fun injectExecutables() {
        Registry.update {
            injectExecutables(this)
        }
    }

    fun injectExecutables(graph: MutableRegistryGraph) {
        val actions = MutableRegistry<Class<*>, FrozenRegistry<String, Action<*>>>()
        val conditions = MutableRegistry<Class<*>, FrozenRegistry<String, Condition<*>>>()

        injectActions(actions)
        injectConditions(conditions)

        graph.registerRegistry(Action.REGISTRY_KEY, actions.freeze())
        graph.registerRegistry(Condition.REGISTRY_KEY, conditions.freeze())
    }

    fun injectActions() {
        Registry.update {
            this.replaceRegistry(Action.REGISTRY_KEY) { injectActions(this) }
        }
    }

    fun injectActions(registry: MutableRegistry<Class<*>, FrozenRegistry<String, Action<*>>>) {
        registry.registerAction("action-bar", ActionbarAction)
        registry.registerAction("close-inventory", CloseInventory)
        registry.registerAction("command", CommandAction)
        registry.registerAction("sound", SoundAction)
        registry.registerAction("stop-sound", SoundStopAction)
        registry.registerAction("title", TitleAction)
    }

    fun injectConditions() {
        Registry.update {
            this.replaceRegistry(Condition.REGISTRY_KEY) { injectConditions(this) }
        }
    }

    fun injectConditions(registry: MutableRegistry<Class<*>, FrozenRegistry<String, Condition<*>>>) {
        registry.registerCondition("permission", PermissionCondition)
    }
}

fun initExecute(plugin: JavaPlugin, miniMessage: MiniMessage = MiniMessage.miniMessage()) {
    Execute.miniMessage = miniMessage
    Execute.plugin = plugin
}

