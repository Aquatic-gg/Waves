package gg.aquatic.waves.registry.serializer
/*
import gg.aquatic.aquaticseries.lib.betterinventory2.SlotSelection
import gg.aquatic.aquaticseries.lib.betterinventory2.action.ConfiguredActionWithConditions
import gg.aquatic.aquaticseries.lib.betterinventory2.action.ConfiguredActionsWithConditions
import gg.aquatic.aquaticseries.lib.betterinventory2.action.ConfiguredConditionWithFailActions
import gg.aquatic.aquaticseries.lib.util.getSectionList
import gg.aquatic.aquaticseries.lib.util.toAquatic
import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.item.loadFromYml
import gg.aquatic.waves.registry.serializer.ItemSerializer.fromSection
import gg.aquatic.waves.util.inventory.AnimatedButtonSettings
import gg.aquatic.waves.util.inventory.ButtonSettings
import gg.aquatic.waves.util.inventory.ClickSettings
import gg.aquatic.waves.util.inventory.InventorySettings
import gg.aquatic.waves.util.inventory.StaticButtonSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import java.util.*
import java.util.function.Function

object InventorySerializer {

    fun loadInventory(section: ConfigurationSection): InventorySettings? {
        val title = section.getString("title") ?: return null
        val size = section.getInt("size")
        val inventoryType = section.getString("inventory-type")?.let {
            try {
                InventoryType.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
        val buttons = ArrayList<ButtonSettings>()
        val buttonsSection = section.getConfigurationSection("buttons")
        if (buttonsSection != null) {
            for (key in buttonsSection.getKeys(false)) {
                val button = loadButton(buttonsSection.getConfigurationSection(key) ?: continue, key) ?: continue
                buttons += button
            }
        }
        val onOpen = ActionSerializer.fromSections<Player>(section.getSectionList("on-open"))
        val onClose = ActionSerializer.fromSections<Player>(section.getSectionList("on-close"))
        return InventorySettings(
            title.toAquatic(),
            size,
            inventoryType,
            buttons,
            onOpen,
            onClose
        )
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

    fun loadButton(
        section: ConfigurationSection,
        id: String,
        modifier: (ConfigurationSection, ButtonSettings) -> Unit = { _, _ -> }
    ): ButtonSettings? {
        val priority = section.getInt("priority", 0)
        val failItemSection = section.getConfigurationSection("fail-item")

        val failItem = if (failItemSection != null) {
            loadButton(failItemSection, "fail-item", modifier)
        } else null

        val conditions = HashMap<Function<Player, Boolean>, ButtonSettings?>()
        for (conditionSection in section.getSectionList("view-conditions")) {
            val condition = RequirementSerializer.fromSection<Player>(conditionSection) ?: continue
            val conditionFailItemSection = conditionSection.getConfigurationSection("fail-item")
            val conditionFailItem: ButtonSettings? = if (conditionFailItemSection != null) {
                loadButton(conditionFailItemSection, "fail-item", modifier)
            } else null
            conditions += Function<Player, Boolean> { t: Player ->
                condition.check(t)
            } to conditionFailItem
        }
        val clickSettings = loadClickSettings(section.getSectionList("click-actions"))

        val updateEvery = section.getInt("update-every", 10)
        val framesSection = section.getConfigurationSection("frames")
        if (framesSection != null) {
            val frames = TreeMap<Int, ButtonSettings>()
            for (key in framesSection.getKeys(false)) {
                val frameSection = framesSection.getConfigurationSection(key) ?: continue
                val time = key.toInt()
                val btn = loadButton(frameSection, "frame", modifier) ?: continue
                frames[time] = btn
            }
            if (frames.isEmpty()) return null
            val settings = AnimatedButtonSettings(
                id, priority, conditions, failItem, clickSettings, updateEvery,
                frames
            )
            modifier(section, settings)
            return settings
        } else {
            val item = AquaticItem.loadFromYml(section) ?: return null
            val slots = loadSlotSelection(section.getStringList("slots"))
            if (slots.slots.isEmpty()) {
                slots.slots += section.getInt("slot")
            }
            val settings = StaticButtonSettings(
                id, priority, conditions, failItem, clickSettings, updateEvery,
                item,
                slots
            )
            modifier(section, settings)
            return settings
        }
    }

    fun loadClickSettings(sections: List<ConfigurationSection>): ClickSettings {
        val map = HashMap<ClickSettings.MenuClickActionType, MutableList<ConfiguredActionsWithConditions>>()
        for (section in sections) {
            val actions = loadActionsWithConditions(section) ?: continue
            for (menuClickActionType in section.getStringList("types")
                .mapNotNull { ClickSettings.MenuClickActionType.valueOf(it.uppercase()) }) {
                val list = map.getOrPut(menuClickActionType) { ArrayList() }
                list.add(actions)
            }
        }
        return ClickSettings(map) { u, t -> t }
    }

    fun loadActionsWithConditions(section: ConfigurationSection): ConfiguredActionsWithConditions? {
        val actions = ArrayList<ConfiguredActionWithConditions>()
        val actionSections = section.getSectionList("actions")

        for (actionSection in actionSections) {
            actions += loadActionWithCondition(actionSection) ?: continue
        }
        val conditions = ArrayList<ConfiguredConditionWithFailActions>()
        for (conditionSection in section.getSectionList("conditions")) {
            conditions += loadConditionWithFailActions(conditionSection) ?: continue
        }

        if (actions.isEmpty() && conditions.isEmpty()) return null

        val failActions = if (section.isConfigurationSection("fail") && conditions.isNotEmpty()) {
            loadActionsWithConditions(section.getConfigurationSection("fail")!!)
        } else null

        return ConfiguredActionsWithConditions(actions, conditions, failActions)
    }

    fun loadActionWithCondition(section: ConfigurationSection): ConfiguredActionWithConditions? {
        val action = ActionSerializer.fromSection<Player>(section) ?: return null
        val conditions = ArrayList<ConfiguredConditionWithFailActions>()
        for (configurationSection in section.getSectionList("conditions")) {
            conditions += loadConditionWithFailActions(configurationSection) ?: continue
        }
        val failActions = if (section.isConfigurationSection("fail") && conditions.isNotEmpty()) {
            loadActionsWithConditions(section.getConfigurationSection("fail")!!)
        } else null
        return ConfiguredActionWithConditions(action, conditions, failActions)
    }

    fun loadConditionWithFailActions(section: ConfigurationSection): ConfiguredConditionWithFailActions? {
        val condition = RequirementSerializer.fromSection<Player>(section) ?: return null
        val failActions = if (section.isConfigurationSection("fail")) {
            loadActionsWithConditions(section.getConfigurationSection("fail")!!)
        } else null
        return ConfiguredConditionWithFailActions(condition, failActions)
    }
}
 */