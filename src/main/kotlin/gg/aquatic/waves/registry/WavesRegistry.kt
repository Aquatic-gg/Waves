package gg.aquatic.waves.registry

import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose
import gg.aquatic.waves.util.price.AbstractPrice
import gg.aquatic.waves.economy.RegisteredCurrency
import gg.aquatic.waves.hologram.line.AnimatedHologramLine
import gg.aquatic.waves.hologram.line.ItemHologramLine
import gg.aquatic.waves.hologram.line.TextHologramLine
import gg.aquatic.waves.input.Input
import gg.aquatic.waves.input.impl.ChatInput
import gg.aquatic.waves.input.impl.VanillaMenuInput
import gg.aquatic.waves.interactable.settings.*
import gg.aquatic.waves.interactable.settings.entityproperty.EntityProperty
import gg.aquatic.waves.interactable.settings.entityproperty.display.DisplayEntityProperty
import gg.aquatic.waves.interactable.settings.entityproperty.display.ItemDisplayEntityProperty
import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.item.factory.*
import gg.aquatic.waves.packetevents.EntityDataBuilder
import gg.aquatic.waves.packetevents.type.ItemEntityDataBuilder
import gg.aquatic.waves.packetevents.type.TextDisplayEntityDataBuilder
import gg.aquatic.waves.util.action.*
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
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.awt.Color

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
        "MMOITEM" to MMOFactory
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
        "ITEM_MODEL" to ItemDisplayInteractableSettings.Companion
    )

    val HOLOGRAM_LINE_FACTORIES = hashMapOf(
        "item" to ItemHologramLine.Companion,
        "text" to TextHologramLine.Companion,
        "animated" to AnimatedHologramLine.Companion,
    )

    val ENTITY_PROPERTY_FACTORIES = hashMapOf(
        createProperty("is-on-fire") { s, str, builder, _ ->
            builder.isOnFire(s.getBoolean(str))
        },
        createProperty("is-sneaking") { s, str, builder, _ ->
            builder.isSneaking(s.getBoolean(str))
        },
        createProperty("is-sprinting") { s, str, builder, _ ->
            builder.isSprinting(s.getBoolean(str))
        },
        createProperty("is-swimming") { s, str, builder, _ ->
            builder.isSwimming(s.getBoolean(str))
        },
        createProperty("invisible") { s, str, builder, _ ->
            builder.isInvisible(s.getBoolean(str))
        },
        createProperty("glowing") { s, str, builder, _ ->
            builder.isGlowing(s.getBoolean(str))
        },
        createProperty("is-flying") { s, str, builder, _ ->
            builder.isFlying(s.getBoolean(str))
        },
        createProperty("custom-name") { s, str, builder, updater ->
            var name = s.getString(str) ?: ""
            name = updater(name)
            builder.setCustomName(name.toMMComponent())
        },
        createProperty("custom-name-visible") { s, str, builder, _ ->
            builder.isCustomNameVisible(s.getBoolean(str))
        },
        createProperty("is-silent") { s, str, builder, _ ->
            builder.isSilent(s.getBoolean(str))
        },
        createProperty("no-gravity") { s, str, builder, _ ->
            builder.hasNoGravity(s.getBoolean(str))
        },
        createProperty("pose") { s, str, builder, _ ->
            builder.setPose(EntityPose.valueOf(s.getString(str, "STANDING")!!.uppercase()))
        },
        createProperty("entity-item") { s, str, builder, _ ->
            (builder as ItemEntityDataBuilder).setItem(AquaticItem.loadFromYml(s)?.getItem() ?: return@createProperty)
        },
        "item" to ItemDisplayEntityProperty.Item.Companion,
        "item-transform" to ItemDisplayEntityProperty.ItemTransformation.Companion,
        "billboard" to DisplayEntityProperty.Billboard.Companion,
        "interpolation-delay" to DisplayEntityProperty.InterpolationDelay.Companion,
        "interpolation-duration" to DisplayEntityProperty.InterpolationDuration.Companion,
        "teleport-interpolation-duration" to DisplayEntityProperty.TeleportInterpolationDuration.Companion,
        "transformation" to DisplayEntityProperty.Transformation.Companion,
        createProperty("text") { s, str, builder, updater ->
            if (builder is TextDisplayEntityDataBuilder) {
                builder.setText(updater(s.getString(str) ?: "").toMMComponent())
            }
        }, createProperty("is-see-through") { s, str, builder, updater ->
            if (builder is TextDisplayEntityDataBuilder) {
                val boolean = s.getBoolean(str)
                builder.isSeeThrough(boolean)
            }
        }, createProperty("background-color") { s, str, builder, updater ->
            if (builder is TextDisplayEntityDataBuilder) {
                val colorStr = s.getString(str) ?: "0;0;0"
                val color = colorStr.split(";").mapNotNull { it.toIntOrNull() }
                val colorInst = Color(color[0], color[1], color[2])
                builder.useDefaultBackgroundColor(false)
                builder.setBackgroundColor(colorInst.rgb)
            }
        }, createProperty("text-opacity") { s, str, builder, updater ->
            if (builder is TextDisplayEntityDataBuilder) {
                val opacity = s.getInt(str, 255)
                builder.setTextOpacity(opacity.toByte())
            }
        }, createProperty("has-shadow") { s, str, builder, updater ->
            if (builder is TextDisplayEntityDataBuilder) {
                val has = s.getBoolean(str)
                builder.hasShadow(has)
            }
        },
        createProperty("line-width") { s, str, builder, updater ->
            if (builder is TextDisplayEntityDataBuilder) {
                builder.setLineWidth(updater(s.getString(str) ?: "").toIntOrNull() ?: 150)
            }
        }
    )
    val ITEM = HashMap<String, AquaticItem>()

    val STATISTIC_TYPES = HashMap<Class<*>, MutableMap<String, StatisticType<*>>>().apply {
        val p = getOrPut(Player::class.java) { HashMap() }
        p["BLOCK_BREAK"] = BlockBreakStatistic
    }

    val INPUT_TYPES = mutableMapOf(
        "chat" to ChatInput,
        "vanilla-menu" to VanillaMenuInput
    )

    private fun createProperty(
        path: String,
        factory: (ConfigurationSection, String, EntityDataBuilder, updater: (String) -> String) -> Unit
    ): Pair<String, EntityProperty.Serializer> {
        return path to object : EntityProperty.Serializer {
            override fun serialize(section: ConfigurationSection): EntityProperty {
                return object : EntityProperty {
                    override fun apply(builder: EntityDataBuilder, updater: (String) -> String) {
                        factory(section, path, builder, updater)
                    }
                }
            }
        }
    }
}