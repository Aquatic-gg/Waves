package gg.aquatic.waves.registry

import gg.aquatic.waves.economy.RegisteredCurrency
import gg.aquatic.waves.fake.entity.data.EntityData
import gg.aquatic.waves.input.impl.ChatInput
import gg.aquatic.waves.input.impl.VanillaMenuInput
import gg.aquatic.waves.interactable.settings.*
import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.item.factory.*
import gg.aquatic.waves.util.currency.Currency
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.generic.Condition
import gg.aquatic.waves.util.price.AbstractPrice
import gg.aquatic.waves.util.price.impl.ItemPrice
import gg.aquatic.waves.util.price.impl.VaultPrice
import gg.aquatic.waves.util.statistic.StatisticType
import gg.aquatic.waves.util.statistic.impl.BlockBreakStatistic
import gg.aquatic.waves.util.statistic.impl.BlockPlaceStatistic
import gg.aquatic.waves.util.statistic.impl.DamageDealtStatistic
import gg.aquatic.waves.util.statistic.impl.DeathStatistic
import gg.aquatic.waves.util.statistic.impl.ItemCraftStatistic
import gg.aquatic.waves.util.statistic.impl.KillStatistic
import gg.aquatic.waves.util.statistic.impl.PlaceholderStatistic
import gg.aquatic.waves.util.statistic.impl.TravelStatistic
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

object WavesRegistry {

    val INDEX_TO_CURRENCY = HashMap<Int, RegisteredCurrency>()
    val ECONOMY = HashMap<String, Currency>()
    val ACTION = ConcurrentHashMap<Class<*>, ConcurrentHashMap<String, Action<*>>>()
    val REQUIREMENT = ConcurrentHashMap<Class<*>, ConcurrentHashMap<String, Condition<*>>>()
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
        "CRAFTENGINE" to CraftEngineFactory,
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
        "text" to gg.aquatic.waves.hologram.line.TextHologramLine.Companion,
        "item" to gg.aquatic.waves.hologram.line.ItemHologramLine.Companion,
        "animated" to gg.aquatic.waves.hologram.line.AnimatedHologramLine.Companion,
        /*
        "text" to TextHologramLine.Companion,
        "animated" to AnimatedHologramLine.Companion,

         */
    )

    val ENTITY_DATA = ConcurrentHashMap<Class<out Entity>, ConcurrentHashMap<String, EntityData>>()
    //val ENTITY_DATA = HashMap<String, EntityData>()

    init {
        registerEntityData("gg.aquatic.waves.fake.entity.data.impl")
    }

    val ITEM = HashMap<String, AquaticItem>()

    val STATISTIC_TYPES = HashMap<Class<*>, MutableMap<String, StatisticType<*>>>().apply {
        val p = getOrPut(Player::class.java) { HashMap() }
        p["BLOCK_BREAK"] = BlockBreakStatistic
        p["KILL"] = KillStatistic
        p["ITEM_CRAFT"] = ItemCraftStatistic
        p["DAMAGE_DEALT"] = DamageDealtStatistic
        p["DEATH"] = DeathStatistic
        p["BLOCK_PLACE"] = BlockPlaceStatistic
        p["PLACEHOLDER"] = PlaceholderStatistic
        p["TRAVEL"] = TravelStatistic

    }

    val INPUT_TYPES = mutableMapOf(
        "chat" to ChatInput,
        "vanilla-menu" to VanillaMenuInput
    )
}