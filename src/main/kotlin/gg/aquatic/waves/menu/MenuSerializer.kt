package gg.aquatic.waves.menu

import gg.aquatic.waves.inventory.ButtonType
import gg.aquatic.waves.inventory.InventoryType
import gg.aquatic.waves.menu.settings.AnimatedButtonSettings
import gg.aquatic.waves.menu.settings.ButtonSettings
import gg.aquatic.waves.menu.settings.IButtonSettings
import gg.aquatic.waves.menu.settings.PrivateMenuSettings
import gg.aquatic.waves.registry.serializer.ActionSerializer
import gg.aquatic.waves.registry.serializer.ItemSerializer
import gg.aquatic.waves.registry.serializer.RequirementSerializer
import gg.aquatic.waves.util.generic.ConfiguredExecutableObjectWithConditions
import gg.aquatic.waves.util.generic.ConfiguredExecutableObjectsWithConditions
import gg.aquatic.waves.util.getSectionList
import gg.aquatic.waves.util.requirement.ConfiguredRequirementWithFailActions
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.ArrayList
import java.util.HashMap
import java.util.TreeMap

object MenuSerializer {

    fun loadPrivateInventory(section: ConfigurationSection): PrivateMenuSettings {
        val type = if (section.contains("size")) {
            val size = section.getInt("size",54)
            when(size) {
                54 -> InventoryType.GENERIC9X6
                45 -> InventoryType.GENERIC9X5
                36 -> InventoryType.GENERIC9X4
                27 -> InventoryType.GENERIC9X3
                18 -> InventoryType.GENERIC9X2
                9 -> InventoryType.GENERIC9X1
                else -> InventoryType.GENERIC9X6
            }
        } else InventoryType.valueOf(section.getString("type","GENERIC9X6")!!.uppercase())

        val title = section.getString("title") ?: ""
        val components = HashMap<String,IButtonSettings>()
        val componentsSection = section.getConfigurationSection("buttons")
        if (componentsSection != null) {
            for (componentId in componentsSection.getKeys(false)) {
                val componentSection = componentsSection.getConfigurationSection(componentId)
                if (componentSection != null) {
                    components[componentId] = loadButton(componentSection, componentId)
                }
            }
        }
        return PrivateMenuSettings(type, MiniMessage.miniMessage().deserialize(title), components)
    }

    fun loadButton(section: ConfigurationSection, id: String): IButtonSettings {
        val priority = section.getInt("priority")
        val updateEvery = section.getInt("update-every",10)
        val viewRequirements = RequirementSerializer.fromSections<Player>(section.getSectionList("view-requirements"))
        val clickActions = loadClickSettings(section.getSectionList("click-actions"))
        val failComponentSection = section.getConfigurationSection("fail-component")
        val failComponent = failComponentSection?.let { loadButton(it, id) }

        if (section.contains("frames")) { // Animated button
            val frames = TreeMap<Int,IButtonSettings>()
            val framesSection = section.getConfigurationSection("frames")
            if (framesSection != null) {
                for (frameId in framesSection.getKeys(false)) {
                    val frameSection = framesSection.getConfigurationSection(frameId)
                    if (frameSection != null) {
                        frames[frameId.toIntOrNull() ?: continue] = loadButton(frameSection, id)
                    }
                }
            }
            return AnimatedButtonSettings(id, frames, viewRequirements, clickActions, priority, updateEvery, failComponent)
        } else {
            val item = ItemSerializer.fromSection(section)
            val slots = loadSlotSelection(section.getStringList("slots")).slots
            return ButtonSettings(id, item,slots,viewRequirements,clickActions,priority,updateEvery,failComponent)
        }
    }

    fun loadClickSettings(sections: List<ConfigurationSection>): gg.aquatic.waves.menu.settings.ClickSettings {
        val map = HashMap<ButtonType, MutableList<ConfiguredExecutableObjectsWithConditions<Player>>>()
        for (section in sections) {
            val actions = loadActionsWithConditions(section) ?: continue
            for (menuClickActionType in section.getStringList("types")
                .mapNotNull { ButtonType.valueOf(it.uppercase()) }) {
                val list = map.getOrPut(menuClickActionType) { ArrayList() }
                list.add(actions)
            }
        }
        return gg.aquatic.waves.menu.settings.ClickSettings(map)
    }

    fun loadSlotSelection(list: List<String>): SlotSelection {
        val slots = ArrayList<Int>()
        for (slot in list) {
            if (slot.contains("-")) {
                val range = slot.split("-")
                val start = range[0].toInt()
                val end = range[1].toInt()
                slots += start..end
            } else {
                slots += slot.toInt()
            }
        }
        return SlotSelection.of(slots)
    }

    fun loadActionsWithConditions(section: ConfigurationSection): ConfiguredExecutableObjectsWithConditions<Player>? {
        val actions = ArrayList<ConfiguredExecutableObjectWithConditions<Player,Unit>>()
        val actionSections = section.getSectionList("actions")

        for (actionSection in actionSections) {
            actions += loadActionWithCondition(actionSection) ?: continue
        }
        val conditions = ArrayList<ConfiguredRequirementWithFailActions<Player,Unit>>()
        for (conditionSection in section.getSectionList("conditions")) {
            conditions += loadConditionWithFailActions(conditionSection) ?: continue
        }

        if (actions.isEmpty() && conditions.isEmpty()) return null

        val failActions = if (section.isConfigurationSection("fail") && conditions.isNotEmpty()) {
            loadActionsWithConditions(section.getConfigurationSection("fail")!!)
        } else null

        return ConfiguredExecutableObjectsWithConditions(actions, conditions, failActions)
    }

    fun loadActionWithCondition(section: ConfigurationSection): ConfiguredExecutableObjectWithConditions<Player,Unit>? {
        val action = ActionSerializer.fromSection<Player>(section) ?: return null
        val conditions = ArrayList<ConfiguredRequirementWithFailActions<Player,Unit>>()
        for (configurationSection in section.getSectionList("conditions")) {
            conditions += loadConditionWithFailActions(configurationSection) ?: continue
        }
        val failActions = if (section.isConfigurationSection("fail") && conditions.isNotEmpty()) {
            loadActionsWithConditions(section.getConfigurationSection("fail")!!)
        } else null
        return ConfiguredExecutableObjectWithConditions(action, conditions, failActions)
    }

    fun loadConditionWithFailActions(section: ConfigurationSection): ConfiguredRequirementWithFailActions<Player,Unit>? {
        val condition = RequirementSerializer.fromSection<Player>(section) ?: return null
        val failActions = if (section.isConfigurationSection("fail")) {
            loadActionsWithConditions(section.getConfigurationSection("fail")!!)
        } else null
        return ConfiguredRequirementWithFailActions(condition, failActions)
    }

}