package gg.aquatic.execute

import gg.aquatic.common.AquaticCommon
import gg.aquatic.common.MiniMessageResolver
import gg.aquatic.common.initializeCommon
import gg.aquatic.execute.action.impl.*
import gg.aquatic.execute.action.impl.logical.ConditionalActionsAction
import gg.aquatic.execute.action.impl.logical.SmartAction
import gg.aquatic.execute.condition.impl.DateRangeCondition
import gg.aquatic.execute.condition.impl.DayOfMonthCondition
import gg.aquatic.execute.condition.impl.DayOfWeekCondition
import gg.aquatic.execute.condition.impl.BiomeCondition
import gg.aquatic.execute.condition.impl.HasEmptyInventorySlotCondition
import gg.aquatic.execute.condition.impl.MonthCondition
import gg.aquatic.execute.condition.impl.OnlinePlayerCountCondition
import gg.aquatic.execute.condition.impl.PermissionCondition
import gg.aquatic.execute.condition.impl.TimeRangeCondition
import gg.aquatic.execute.condition.impl.WeekOfYearModuloCondition
import gg.aquatic.execute.condition.impl.WeekParityCondition
import gg.aquatic.execute.condition.impl.WorldCondition
import gg.aquatic.kregistry.bootstrap.BootstrapHolder
import gg.aquatic.kregistry.bootstrap.ContributionBuilder
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
            add("biome", BiomeCondition)
            add("date-range", DateRangeCondition)
            add("day-of-month", DayOfMonthCondition)
            add("day-of-week", DayOfWeekCondition)
            add("has-empty-inventory-slot", HasEmptyInventorySlotCondition)
            add("month", MonthCondition)
            add("online-player-count", OnlinePlayerCountCondition)
            add("permission", PermissionCondition)
            add("time-range", TimeRangeCondition)
            add("week-parity", WeekParityCondition)
            add("week-of-year-mod", WeekOfYearModuloCondition)
            add("world", WorldCondition)
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
