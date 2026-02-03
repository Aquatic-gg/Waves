package gg.aquatic.kholograms.line

import gg.aquatic.common.toMMComponent
import gg.aquatic.execute.checkConditions
import gg.aquatic.execute.requirement.ConditionHandle
import gg.aquatic.kholograms.HologramLine
import gg.aquatic.kholograms.HologramLineHandle
import gg.aquatic.kholograms.serialize.LineSettings
import gg.aquatic.pakket.Pakket
import gg.aquatic.pakket.api.nms.PacketEntity
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.api.nms.entity.data.impl.display.DisplayEntityData
import gg.aquatic.pakket.api.nms.entity.data.impl.display.TextDisplayEntityData
import gg.aquatic.pakket.sendPacket
import gg.aquatic.replace.placeholder.PlaceholderContext
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.joml.Vector3f
import kotlin.properties.Delegates

class TextHologramLine(
    override var height: Double,
    override var filter: suspend (Player) -> Boolean,
    override var failLine: HologramLine?,
    text: Component,
    lineWidth: Int,
    scale: Float = 1.0f,
    billboard: Billboard = Billboard.CENTER,
    hasShadow: Boolean = true,
    backgroundColor: Color? = null,
    isSeeThrough: Boolean = true,
    transformationDuration: Int = 0,
    teleportInterpolation: Int,
    translation: Vector3f
) : HologramLine {
    override fun spawn(
        location: Location,
        player: Player,
        placeholderContext: PlaceholderContext<Player>,
    ): PacketEntity {
        val packetEntity =
            Pakket.handler.createEntity(location, EntityType.TEXT_DISPLAY, null)
                ?: throw Exception("Failed to create entity")
        val entityData = createInitialData(player, placeholderContext)
        val packet = Pakket.handler.createEntityUpdatePacket(packetEntity.entityId, entityData)
        packetEntity.updatePacket = packet
        return packetEntity
    }

    private val cachedData = HashMap<Int, EntityDataValue>()

    override var teleportInterpolation: Int by Delegates.observable(teleportInterpolation) { _, old, new ->
        if (old == new) return@observable
        cacheData(DisplayEntityData.TeleportationDuration.generate(new))
    }

    override var translation: Vector3f by Delegates.observable(translation) { _, old, new ->
        if (old == new) return@observable
        cacheData(DisplayEntityData.Translation.generate(new))
    }

    override var billboard: Billboard by Delegates.observable(billboard) { _, old, new ->
        if (old == new) return@observable
        cacheData(DisplayEntityData.Billboard.generate(new))
    }

    override var transformationDuration: Int by Delegates.observable(transformationDuration) { _, old, new ->
        if (old == new) return@observable
        cacheData(DisplayEntityData.TransformationInterpolationDuration.generate(new))
    }

    override var scale: Float by Delegates.observable(scale) { _, old, new ->
        if (old == new) return@observable
        cacheData(DisplayEntityData.Scale.generate(Vector3f(new)))
    }

    var lineWidth: Int by Delegates.observable(lineWidth) { _, old, new ->
        if (old == new) return@observable
        cacheData(TextDisplayEntityData.Width.generate(new))
    }

    var hasShadow: Boolean by Delegates.observable(hasShadow) { _, old, new ->
        if (old == new) return@observable
        cacheData(
            TextDisplayEntityData.Flags.generate(new, isSeeThrough, backgroundColor == null)
        )
    }

    var isSeeThrough: Boolean by Delegates.observable(isSeeThrough) { _, old, new ->
        if (old == new) return@observable
        cacheData(
            TextDisplayEntityData.Flags.generate(
                hasShadow,
                new,
                backgroundColor == null,
                TextDisplay.TextAlignment.CENTER,
            )
        )
    }

    var backgroundColor: Color? by Delegates.observable(backgroundColor) { _, old, new ->
        if (old == new) return@observable
        if (new == null) {
            cacheData(
                TextDisplayEntityData.Flags.generate(
                    hasShadow,
                    isSeeThrough,
                    true,
                    TextDisplay.TextAlignment.CENTER,
                )
            )
        } else {
            cacheData(
                TextDisplayEntityData.Flags.generate(
                    hasShadow,
                    isSeeThrough,
                    false,
                    TextDisplay.TextAlignment.CENTER,
                )
            )
            cacheData(TextDisplayEntityData.BackgroundColor.generate(new))
        }
    }

    var text: Component by Delegates.observable(text) { _, old, new ->
        if (old == new) return@observable
        textContextItem = null
    }

    private fun createInitialData(
        player: Player,
        placeholderContext: PlaceholderContext<Player>
    ): List<EntityDataValue> {
        val list = mutableListOf(
            DisplayEntityData.TransformationInterpolationDuration.generate(transformationDuration),
            DisplayEntityData.TeleportationDuration.generate(teleportInterpolation),
            TextDisplayEntityData.Width.generate(lineWidth),
            DisplayEntityData.Billboard.generate(billboard),
            TextDisplayEntityData.Flags.generate(
                hasShadow,
                isSeeThrough,
                backgroundColor == null,
                TextDisplay.TextAlignment.CENTER,
            ),
            DisplayEntityData.Translation.generate(translation),
            DisplayEntityData.Scale.generate(Vector3f(scale)),
        ).flatten().toMutableList()
        backgroundColor?.let {
            list += TextDisplayEntityData.BackgroundColor.generate(it)
        }

        val item = textContextItem
        list += if (item == null) {
            val item = placeholderContext.createItem(player, text)
            textContextItem = item
            TextDisplayEntityData.Text.generate(item.latestState.value)
        } else {
            TextDisplayEntityData.Text.generate(item.latestState.value)
        }

        return list
    }

    private fun cacheData(data: Iterable<EntityDataValue>) {
        for (value in data) {
            cachedData[value.id] = value
        }
    }

    override suspend fun tick(hologramLineHandle: HologramLineHandle) {
        val data = buildData(hologramLineHandle)
        if (data.isEmpty()) return
        val packet = Pakket.handler.createEntityUpdatePacket(hologramLineHandle.packetEntity.entityId, data)
        hologramLineHandle.packetEntity.updatePacket = packet
        hologramLineHandle.player.sendPacket(packet, false)
    }

    private var textContextItem: PlaceholderContext<Player>.ComponentItem? = null

    override fun buildData(
        placeholderContext: PlaceholderContext<Player>,
        player: Player
    ): List<EntityDataValue> {
        val data = ArrayList<EntityDataValue>()

        val item = textContextItem
        if (item == null) {
            val item = placeholderContext.createItem(player, text)
            textContextItem = item

            data += TextDisplayEntityData.Text.generate(item.latestState.value)
        } else {
            val result = item.tryUpdate(player)
            if (result.wasUpdated) {
                data += TextDisplayEntityData.Text.generate(item.latestState.value)
            }
        }

        data += cachedData.values
        cachedData.clear()

        return data
    }

    class Settings(
        val height: Double,
        val text: Component,
        val lineWidth: Int,
        val scale: Float = 1.0f,
        val billboard: Billboard = Billboard.CENTER,
        val conditions: List<ConditionHandle<Player>>,
        val hasShadow: Boolean,
        val backgroundColor: Color?,
        val isSeeThrough: Boolean,
        val transformationDuration: Int,
        val failLine: LineSettings?,
        val teleportInterpolation: Int,
        val translation: Vector3f
    ) : LineSettings {
        override fun create(): HologramLine {
            return TextHologramLine(
                height,
                { p ->
                    conditions.checkConditions(p)
                },
                failLine?.create(),
                text,
                lineWidth,
                scale,
                billboard,
                hasShadow,
                backgroundColor,
                isSeeThrough,
                transformationDuration,
                teleportInterpolation,
                translation
            )
        }
    }

}
