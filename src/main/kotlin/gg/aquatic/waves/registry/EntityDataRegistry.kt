package gg.aquatic.waves.registry

import gg.aquatic.waves.fake.entity.data.EntityClassLookup
import gg.aquatic.waves.fake.entity.data.EntityData
import gg.aquatic.waves.fake.entity.data.impl.ItemEntityData
import gg.aquatic.waves.fake.entity.data.impl.display.BlockDisplayEntityData
import gg.aquatic.waves.fake.entity.data.impl.display.DisplayEntityData
import gg.aquatic.waves.fake.entity.data.impl.display.ItemDisplayEntityData
import gg.aquatic.waves.fake.entity.data.impl.display.TextDisplayEntityData
import gg.aquatic.waves.fake.entity.data.impl.living.BaseEntityData
import gg.aquatic.waves.util.logger.type.InfoLogger
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import java.util.concurrent.ConcurrentHashMap

fun WavesRegistry.registerEntityData(namespace: String) {
    ENTITY_DATA.getOrPut(BlockDisplay::class.java) { ConcurrentHashMap() }.putAll(
        listOf(
            "block" to BlockDisplayEntityData.BlockState
        )
    )
    ENTITY_DATA.getOrPut(Display::class.java) { ConcurrentHashMap() }.putAll(
        listOf(
            "interpolation-delay" to DisplayEntityData.InterpolationDelay,
            "interpolation-duration" to DisplayEntityData.TransformationInterpolationDuration,
            "teleportation-duration" to DisplayEntityData.TeleportationDuration,
            "translation" to DisplayEntityData.Translation,
            "scale" to DisplayEntityData.Scale,
            "rotation" to DisplayEntityData.Rotation,
            "billboard" to DisplayEntityData.Billboard,
            "brightness" to DisplayEntityData.Brightness,
            "view-range" to DisplayEntityData.ViewRange,
            "shadow-radius" to DisplayEntityData.ShadowRadius,
            "shadow-strength" to DisplayEntityData.ShadowStrength,
            "width" to DisplayEntityData.Width,
            "height" to DisplayEntityData.Height,
        )
    )
    ENTITY_DATA.getOrPut(ItemDisplay::class.java) { ConcurrentHashMap() }.putAll(
        listOf(
            "display-item" to ItemDisplayEntityData.Item,
            "item-display-transform" to ItemDisplayEntityData.ItemDisplayTransform,
        )
    )
    ENTITY_DATA.getOrPut(TextDisplay::class.java) { ConcurrentHashMap() }.putAll(
        listOf(
            "text" to TextDisplayEntityData.Text,
            "line-width" to TextDisplayEntityData.Width,
            "background-color" to TextDisplayEntityData.BackgroundColor,
            "text-opacity" to TextDisplayEntityData.TextOpacity,
            "text-display-flags" to TextDisplayEntityData.Flags,
        )
    )

    ENTITY_DATA.getOrPut(org.bukkit.entity.Item::class.java) { ConcurrentHashMap() }.putAll(
        listOf(
            "item" to ItemEntityData.Item
        )
    )
    ENTITY_DATA.getOrPut(Entity::class.java) { ConcurrentHashMap() }.putAll(
        listOf(
            "visuals" to BaseEntityData.Visuals,
            "custom-name" to BaseEntityData.CustomName,
            "custom-name-visible" to BaseEntityData.CustomNameVisible,
            "silent" to BaseEntityData.Silent,
            "has-gravity" to BaseEntityData.HasGravity,
            "pose" to BaseEntityData.Pose,
        )
    )

    val entityDataClasses = EntityClassLookup.searchForEntityDataClasses(namespace)
    entityDataClasses.forEach {
        val entityData = EntityClassLookup.createEntityDataInstance(it)
        if (entityData != null) {
            val map = ENTITY_DATA.getOrPut(entityData.entityClass) { ConcurrentHashMap() }
            map += entityData.id to entityData
            InfoLogger.send("Registered EntityData with ID: ${entityData.id}, type: ${entityData.entityClass.simpleName}")
        }
    }
}

fun WavesRegistry.entityData(entityType: EntityType): Map<String, EntityData> {
    val data = HashMap<String, EntityData>()
    val entityClazz = entityType.entityClass ?: return data
    for ((key,map) in ENTITY_DATA) {
        if (entityClazz == key || entityClazz.isAssignableFrom(key)) {
            data += map
        }
    }
    return data
}