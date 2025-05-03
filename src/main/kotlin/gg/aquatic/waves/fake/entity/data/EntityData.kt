package gg.aquatic.waves.fake.entity.data

import org.bukkit.entity.Entity

interface EntityData {

    val id: String
    fun apply(entity: Entity)

}