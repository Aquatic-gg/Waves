package gg.aquatic.execute

import gg.aquatic.common.AquaticCommon
import gg.aquatic.common.MiniMessageResolver
import gg.aquatic.common.initializeCommon
import gg.aquatic.execute.action.impl.*
import gg.aquatic.execute.action.impl.logical.ConditionalActionsAction
import gg.aquatic.execute.action.impl.logical.SmartAction
import gg.aquatic.execute.condition.impl.PermissionCondition
import gg.aquatic.kregistry.bootstrap.BootstrapHolder
import gg.aquatic.kregistry.bootstrap.ContributionBuilder
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin

object Execute {

    lateinit var bootstrapHolder: BootstrapHolder

    internal fun injectExecutables() {
        ExecuteRegistryHolder.registryBootstrap(bootstrapHolder, injectExecutablesInternal())
    }

    private fun injectExecutablesInternal(): ContributionBuilder.() -> Unit = {
        injectActions(this)
        injectConditions(this)
        injectSmartActions(this)
    }

    fun injectActions(builder: ContributionBuilder) {
        builder.registry(Action.REGISTRY_KEY) {
            add("actionbar", ActionbarAction)
            add("close-inventory", CloseInventory)
            add("command", CommandAction)
            add("sound", SoundAction)
            add("stop-sound", SoundStopAction)
            add("title", TitleAction)
        }
    }

    fun injectSmartActions(builder: ContributionBuilder) {
        builder.registry(SmartAction.REGISTRY_KEY) {
            add("condition-actions") { clazz -> ConditionalActionsAction(clazz) }
        }
    }

    fun injectConditions(builder: ContributionBuilder) {
        builder.registry(Condition.REGISTRY_KEY) {
            add("permission", PermissionCondition)
        }
    }
}

fun BootstrapHolder.initializeExecute(plugin: JavaPlugin, miniMessage: MiniMessageResolver, injectDefaults: Boolean = true) {
    Execute.bootstrapHolder = this
    try {
        val pl = AquaticCommon.plugin
    } catch (_: Exception) {
        initializeCommon(plugin, miniMessage)
    }
    if (injectDefaults) {
        Execute.injectExecutables()
    }
}
