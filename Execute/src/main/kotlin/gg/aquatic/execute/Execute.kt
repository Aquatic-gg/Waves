package gg.aquatic.execute

import gg.aquatic.common.AquaticCommon
import gg.aquatic.common.initializeCommon
import gg.aquatic.execute.action.impl.*
import gg.aquatic.execute.action.impl.logical.ConditionalActionsAction
import gg.aquatic.execute.action.impl.logical.SmartAction
import gg.aquatic.execute.action.impl.logical.SmartActionFactory
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

    fun injectExecutables() {
        Registry.update {
            injectExecutables(this)
        }
    }

    fun injectExecutables(graph: MutableRegistryGraph) {
        val actions = MutableRegistry<Class<*>, FrozenRegistry<String, Action<*>>>()
        val conditions = MutableRegistry<Class<*>, FrozenRegistry<String, Condition<*>>>()
        val smartActions = MutableRegistry<String, SmartActionFactory>()

        injectActions(actions)
        injectConditions(conditions)
        injectSmartActions(smartActions)

        graph.registerRegistry(Action.REGISTRY_KEY, actions.freeze())
        graph.registerRegistry(Condition.REGISTRY_KEY, conditions.freeze())
        graph.registerRegistry(SmartAction.REGISTRY_KEY, smartActions.freeze())
    }

    fun injectActions() {
        Registry.update {
            this.replaceRegistry(Action.REGISTRY_KEY) { injectActions(this) }
        }
    }

    fun injectActions(registry: MutableRegistry<Class<*>, FrozenRegistry<String, Action<*>>>) {
        registry.registerAction("actionbar", ActionbarAction)
        registry.registerAction("close-inventory", CloseInventory)
        registry.registerAction("command", CommandAction)
        registry.registerAction("sound", SoundAction)
        registry.registerAction("stop-sound", SoundStopAction)
        registry.registerAction("title", TitleAction)
    }

    fun injectSmartActions() {
        Registry.update {
            this.replaceRegistry(SmartAction.REGISTRY_KEY) { injectSmartActions(this) }
        }
    }

    fun injectSmartActions(registry: MutableRegistry<String, SmartActionFactory>) {
        registry.register(
            "smart-action-example"
        ) { clazz, classTransforms -> ConditionalActionsAction(clazz, classTransforms) }
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
    try {
        val pl = AquaticCommon.plugin
    } catch (_: Exception) {
        initializeCommon(plugin)
    }
}

