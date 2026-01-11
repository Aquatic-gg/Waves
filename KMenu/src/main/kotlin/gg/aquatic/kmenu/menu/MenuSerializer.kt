package gg.aquatic.kmenu.menu

import gg.aquatic.common.getSectionList
import gg.aquatic.execute.ConditionalActionHandle
import gg.aquatic.execute.ConditionalActionsHandle
import gg.aquatic.execute.action.ActionSerializer
import gg.aquatic.execute.requirement.ConditionSerializer
import gg.aquatic.execute.requirement.RequirementHandleWithFailActions
import gg.aquatic.kmenu.inventory.ButtonType
import gg.aquatic.kmenu.inventory.InventoryType
import gg.aquatic.kmenu.menu.settings.*
import gg.aquatic.stacked.ItemSerializer
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.*

object MenuSerializer {

    private val SIZE_TO_TYPE = mapOf(
        9 to InventoryType.GENERIC9X1,
        18 to InventoryType.GENERIC9X2,
        27 to InventoryType.GENERIC9X3,
        36 to InventoryType.GENERIC9X4,
        45 to InventoryType.GENERIC9X5,
        54 to InventoryType.GENERIC9X6
    )

    fun loadPrivateInventory(section: ConfigurationSection): PrivateMenuSettings {
        val type = if (section.contains("size")) {
            val size = section.getInt("size", 54)
            SIZE_TO_TYPE[size] ?: InventoryType.GENERIC9X6
        } else {
            val typeStr = section.getString("type", "GENERIC9X6")!!
            try { InventoryType.valueOf(typeStr.uppercase()) } catch (e: Exception) { InventoryType.GENERIC9X6 }
        }

        val title = section.getString("title") ?: ""
        val components = HashMap<String, IButtonSettings>()

        section.getConfigurationSection("buttons")?.let { buttonsSection ->
            for (id in buttonsSection.getKeys(false)) {
                buttonsSection.getConfigurationSection(id)?.let { btnSection ->
                    components[id] = loadButton(btnSection, id)
                }
            }
        }

        return PrivateMenuSettings(type, MiniMessage.miniMessage().deserialize(title), components)
    }

    fun loadButton(section: ConfigurationSection, id: String): IButtonSettings {
        val priority = section.getInt("priority")
        val updateEvery = section.getInt("update-every", 10)
        val viewRequirements = ConditionSerializer.fromSections<Player>(section.getSectionList("view-requirements"))
        val clickActions = loadClickSettings(section.getSectionList("click-actions"))
        val failComponent = section.getConfigurationSection("fail-component")?.let { loadButton(it, id) }

        if (section.contains("frames")) {
            val frames = TreeMap<Int, IButtonSettings>()
            section.getConfigurationSection("frames")?.let { framesSection ->
                for (frameId in framesSection.getKeys(false)) {
                    framesSection.getConfigurationSection(frameId)?.let { frameSection ->
                        val index = frameId.toIntOrNull() ?: return@let
                        frames[index] = loadButton(frameSection, id)
                    }
                }
            }
            return AnimatedButtonSettings(id, frames, viewRequirements, clickActions, priority, updateEvery, failComponent)
        }

        val item = ItemSerializer.fromSection(section)
        val slots = loadSlotSelection(section.getStringList("slots")).slots
        return ButtonSettings(id, item, slots, viewRequirements, clickActions, priority, updateEvery, failComponent)
    }

    fun loadClickSettings(sections: List<ConfigurationSection>): ClickSettings {
        val map = HashMap<ButtonType, MutableList<ConditionalActionsHandle<Player>>>()
        for (section in sections) {
            val actions = loadActionsWithConditions(section) ?: continue
            section.getStringList("types")
                .mapNotNull { runCatching { ButtonType.valueOf(it.uppercase()) }.getOrNull() }
                .forEach { type ->
                    map.getOrPut(type) { ArrayList() }.add(actions)
                }
        }
        return ClickSettings(map)
    }

    fun loadSlotSelection(list: Collection<String>): SlotSelection {
        val slots = list.flatMap { slot ->
            if (slot.contains("-")) {
                val range = slot.split("-")
                val start = range[0].toIntOrNull() ?: 0
                val end = range[1].toIntOrNull() ?: 0
                (start..end).toList()
            } else {
                listOfNotNull(slot.toIntOrNull())
            }
        }
        return SlotSelection.of(slots)
    }

    fun loadActionsWithConditions(section: ConfigurationSection): ConditionalActionsHandle<Player>? {
        val actions = section.getSectionList("actions").mapNotNull { loadActionWithCondition(it) }
        val conditions = section.getSectionList("conditions").mapNotNull { loadConditionWithFailActions(it) }

        if (actions.isEmpty() && conditions.isEmpty()) return null

        val failActions = if (section.isConfigurationSection("fail") && conditions.isNotEmpty()) {
            loadActionsWithConditions(section.getConfigurationSection("fail")!!)
        } else null

        return ConditionalActionsHandle(ArrayList(actions), ArrayList(conditions), failActions)
    }

    fun loadActionWithCondition(section: ConfigurationSection): ConditionalActionHandle<Player,Unit>? {
        val action = ActionSerializer.fromSection<Player>(section) ?: return null
        val conditions = ArrayList<RequirementHandleWithFailActions<Player,Unit>>()
        for (configurationSection in section.getSectionList("conditions")) {
            conditions += loadConditionWithFailActions(configurationSection) ?: continue
        }
        val failActions = if (section.isConfigurationSection("fail") && conditions.isNotEmpty()) {
            loadActionsWithConditions(section.getConfigurationSection("fail")!!)
        } else null
        return ConditionalActionHandle(action, conditions, failActions)
    }

    fun loadConditionWithFailActions(section: ConfigurationSection): RequirementHandleWithFailActions<Player,Unit>? {
        val condition = ConditionSerializer.fromSection<Player>(section) ?: return null
        val failActions = if (section.isConfigurationSection("fail")) {
            loadActionsWithConditions(section.getConfigurationSection("fail")!!)
        } else null
        return RequirementHandleWithFailActions(condition, failActions)
    }

}