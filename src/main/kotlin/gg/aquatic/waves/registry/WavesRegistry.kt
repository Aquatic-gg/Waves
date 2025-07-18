package gg.aquatic.waves.registry

import gg.aquatic.waves.util.price.AbstractPrice
import gg.aquatic.waves.economy.RegisteredCurrency
import gg.aquatic.waves.fake.entity.data.EntityClassLookup
import gg.aquatic.waves.fake.entity.data.EntityData
import gg.aquatic.waves.fake.entity.data.impl.display.DisplayEntityData
import gg.aquatic.waves.hologram.line.AnimatedHologramLine
import gg.aquatic.waves.hologram.line.ItemHologramLine
import gg.aquatic.waves.hologram.line.TextHologramLine
import gg.aquatic.waves.input.impl.ChatInput
import gg.aquatic.waves.input.impl.VanillaMenuInput
import gg.aquatic.waves.interactable.settings.*
import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.item.factory.*
import gg.aquatic.waves.util.action.impl.discord.DiscordWebhookAction
import gg.aquatic.waves.util.action.impl.*
import gg.aquatic.waves.util.currency.Currency
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.generic.Condition
import gg.aquatic.waves.util.item.loadFromYml
import gg.aquatic.waves.util.price.impl.ItemPrice
import gg.aquatic.waves.util.price.impl.VaultPrice
import gg.aquatic.waves.util.statistic.StatisticType
import gg.aquatic.waves.util.statistic.impl.BlockBreakStatistic
import gg.aquatic.waves.util.toMMComponent
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f

object WavesRegistry {

    val INDEX_TO_CURRENCY = HashMap<Int, RegisteredCurrency>()
    val ECONOMY = HashMap<String, Currency>()
    val ACTION = HashMap<Class<*>, MutableMap<String, Action<*>>>().apply {
        val p = getOrPut(Player::class.java) { HashMap() }
        p["actionbar"] = ActionbarAction()
        p["bossbar"] = BossbarAction()
        p["broadcast"] = BroadcastAction()
        p["command"] = CommandAction()
        p["giveitem"] = GiveItemAction()
        p["message"] = MessageAction()
        p["title"] = TitleAction()
        p["sound"] = SoundAction()
        p["discord-webhook"] = DiscordWebhookAction()
    }
    val REQUIREMENT = HashMap<Class<*>, MutableMap<String, Condition<*>>>().apply {
        //val p = getOrPut(Player::class.java) { HashMap() }
        //p += "expression" to ExpressionPlayerRequirement()
    }
    val PRICE by lazy {
        HashMap<Class<*>, MutableMap<String, AbstractPrice<*>>>().apply {
            val p = getOrPut(Player::class.java) { HashMap() }
            p["item"] = ItemPrice()
            if (Bukkit.getPluginManager().getPlugin("Vault") != null)
                p["vault"] = VaultPrice()
        }
    }
    val ITEM_FACTORIES = hashMapOf(
        "MYTHICITEM" to MMFactory,
        "ORAXEN" to OraxenFactory,
        "HDB" to HDBFactory,
        "ITEMSADDER" to IAFactory,
        "ECO" to EcoFactory,
        "BASE64" to Base64Factory,
        "MMOITEM" to MMOFactory,
        "NEXO" to NexoFactory
    )

    val BLOCK_FACTORIES = hashMapOf(
        "ITEMSADDER" to gg.aquatic.waves.util.block.factory.IAFactory,
        "ORAXEN" to gg.aquatic.waves.util.block.factory.OraxenFactory,
    )

    val INTERACTABLE_FACTORIES = hashMapOf(
        "ORAXEN_FURNITURE" to OraxenEntityInteractableSettings.Companion,
        "ENTITY" to EntityInteractableSettings.Companion,
        "NPC" to NPCInteractableSettings.Companion,
        "BLOCK" to BlockInteractableSettings.Companion,
        "MODELENGINE" to MEGInteractableSettings.Companion,
        "BETTERMODEL" to BMInteractableSettings.Companion,
        "ITEM_MODEL" to ItemDisplayInteractableSettings.Companion
    )

    val HOLOGRAM_LINE_FACTORIES = hashMapOf(
        "item" to ItemHologramLine.Companion,
        "text" to TextHologramLine.Companion,
        "animated" to AnimatedHologramLine.Companion,
    )

    val ENTITY_DATA = HashMap<Class<out Entity>, HashMap<String, EntityData>>()
    //val ENTITY_DATA = HashMap<String, EntityData>()

    init {
        registerEntityData("gg.aquatic.waves.fake.entity.data.impl")
    }

    private fun Float.toRadians() = Math.toRadians(this.toDouble()).toFloat()

    private fun ConfigurationSection.updatedBoolean(
        id: String,
        updater: (String) -> String
    ): Boolean {
        return this.getString(id)!!.let {
            updater(it).toBoolean()
        }
    }

    private fun ConfigurationSection.updatedInt(
        id: String,
        updater: (String) -> String
    ): Int {
        return this.getString(id)!!.let {
            updater(it).toInt()
        }
    }
    private fun ConfigurationSection.updatedDouble(
        id: String,
        updater: (String) -> String
    ): Double {
        return this.getString(id)!!.let {
            updater(it).toDouble()
        }
    }
    val ITEM = HashMap<String, AquaticItem>()

    val STATISTIC_TYPES = HashMap<Class<*>, MutableMap<String, StatisticType<*>>>().apply {
        val p = getOrPut(Player::class.java) { HashMap() }
        p["BLOCK_BREAK"] = BlockBreakStatistic
    }

    val INPUT_TYPES = mutableMapOf(
        "chat" to ChatInput,
        "vanilla-menu" to VanillaMenuInput
    )
}