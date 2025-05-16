package gg.aquatic.waves.registry

import gg.aquatic.waves.util.price.AbstractPrice
import gg.aquatic.waves.economy.RegisteredCurrency
import gg.aquatic.waves.fake.entity.data.EntityData
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

    val ENTITY_PROPERTY_FACTORIES =
        hashMapOf(
            createProperty<Entity>("is-on-fire") { section, updater, entity ->
                entity.isVisualFire = section.updatedBoolean("is-on-fire", updater)
            },
            createProperty<Entity>("is-sneaking") { section, updater, entity ->
                entity.isSneaking = section.updatedBoolean("is-sneaking", updater)
            },
            createProperty<Entity>("invisible") { section, updater, entity ->
                entity.isInvisible = section.updatedBoolean("invisible", updater)
            },
            createProperty<Entity>("glowing") { section, updater, entity ->
                entity.isGlowing = section.updatedBoolean("glowing", updater)
            },
            createProperty<Entity>("custom-name") { section, updater, entity ->
                entity.customName(section.getString("custom-name")?.let { updater(it).toMMComponent() })
            },
            createProperty<Entity>("custom-name-visible") { section, updater, entity ->
                entity.isCustomNameVisible =
                    section.updatedBoolean("custom-name-visible", updater)
            },
            createProperty<Entity>("is-silent") { section, updater, entity ->
                entity.isSilent = section.updatedBoolean("is-silent", updater)
            },
            createProperty<Entity>("no-gravity") { section, updater, entity ->
                entity.setGravity(!section.updatedBoolean("no-gravity", updater))
            },
            createProperty<Entity>("gravity") { section, updater, entity ->
                entity.setGravity(section.updatedBoolean("gravity", updater))
            },
            createProperty<Entity>("pose") { section, updater, entity ->
                val poseId = updater(section.getString("pose")!!)
                val pose = Pose.valueOf(poseId.uppercase())
                entity.pose = pose
            },
            createProperty<Entity>("item") { section, updater, entity ->
                val item = AquaticItem.loadFromYml(section.getConfigurationSection("item")) ?: return@createProperty
                if (entity is Item) {
                    entity.itemStack = item.getItem()
                } else if (entity is ItemDisplay) {
                    entity.setItemStack(item.getItem())
                }
            },
            createProperty<LivingEntity>("scale") { section, updater, entity ->
                entity.registerAttribute(Attribute.SCALE)
                entity.getAttribute(Attribute.SCALE)!!.baseValue = section.updatedDouble("scale", updater)
            },
            createProperty<ItemDisplay>("item-transform") { section, updater, entity ->
                val transformId = section.getString("item-transform") ?: return@createProperty
                val tranform = ItemDisplay.ItemDisplayTransform.valueOf(transformId.uppercase())
                entity.itemDisplayTransform = tranform
            },
            createProperty<Display>("billboard") { section, updater, entity ->
                val billboardId = section.getString("billboard") ?: return@createProperty
                val billboard = Display.Billboard.valueOf(billboardId.uppercase())
                entity.billboard = billboard
            },
            createProperty<Display>("interpolation-delay") { section, updater, entity ->
                entity.interpolationDelay = section.updatedInt("interpolation-delay", updater)
            },
            createProperty<Display>("interpolation-duration") { section, updater, entity ->
                entity.interpolationDuration = section.updatedInt("interpolation-duration", updater)
            },
            createProperty<Display>("teleport-duration") { section, updater, entity ->
                entity.teleportDuration = section.updatedInt("teleport-duration", updater)
            },
            createProperty<Display>("transformation") { section, updater, entity ->
                val s = section.getConfigurationSection("transformation") ?: return@createProperty

                val scaleStr = s.getString("scale")
                val scale = if (scaleStr != null) {
                    val split = scaleStr.split(";")
                    Vector3f(split[0].toFloat(), split[1].toFloat(), split[2].toFloat())
                } else Vector3f(1f, 1f, 1f)

                val translationStr = s.getString("translation")
                val translation = if (translationStr != null) {
                    val split = translationStr.split(";")
                    Vector3f(split[0].toFloat(), split[1].toFloat(), split[2].toFloat())
                } else Vector3f(0f, 0f, 0f)

                val rotationStr = s.getString("rotation")
                val rotation = if (rotationStr != null) {
                    val split = rotationStr.split(";")
                    if (split.size > 3) {
                        Quaternionf(split[0].toFloat(), split[1].toFloat(), split[2].toFloat(), split[3].toFloat())
                    } else Quaternionf().rotationXYZ(
                        split[0].toFloat().toRadians(),
                        split[1].toFloat().toRadians(),
                        split[2].toFloat().toRadians()
                    )
                } else Quaternionf()

                entity.transformation = Transformation(translation, rotation, scale, Quaternionf())
            },
            createProperty<TextDisplay>("text") { section, updater, entity ->
                entity.text(updater(section.getString("text")!!).toMMComponent())
            },
            createProperty<TextDisplay>("is-see-through") { section, updater, entity ->
                entity.isSeeThrough = section.updatedBoolean("is-see-through", updater)
            },
            createProperty<TextDisplay>("background-color") { section, updater, entity ->
                val colorStr = section.getString("background-color") ?: "0;0;0"
                val color = colorStr.split(";").mapNotNull { it.toIntOrNull() }
                val colorInst = org.bukkit.Color.fromARGB(color.getOrElse(3) { 255 }, color[0], color[1], color[2])
                entity.isDefaultBackground = false
                entity.backgroundColor = colorInst
            },
            createProperty<TextDisplay>("text-opacity") { section, updater, entity ->
                entity.textOpacity = section.updatedInt("text-opacity", updater).toByte()
            },
            createProperty<TextDisplay>("has-shadow") { section, updater, entity ->
                entity.isShadowed = section.updatedBoolean("has-shadow", updater)
            },
            createProperty<TextDisplay>("line-width") { section, updater, entity ->
                entity.lineWidth = section.updatedInt("line-width", updater)
            }
        )

    private fun Float.toRadians() = Math.toRadians(this.toDouble()).toFloat()
    private inline fun <reified T : Entity> createProperty(
        id: String,
        crossinline factory: (ConfigurationSection, (String) -> String, T) -> Unit
    ): Pair<String, (ConfigurationSection, (String) -> String) -> EntityData> {
        return id to { section: ConfigurationSection, updater: (String) -> String ->
            EntityData.create(id) { entity, updater ->
                if (entity !is T) return@create
                factory(section, updater, entity)
            }
        }
    }

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