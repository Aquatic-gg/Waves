package gg.aquatic.waves.fake.entity.data

import org.bukkit.entity.Entity

interface EntityData {

    val id: String
    fun apply(entity: Entity, updater: (String) -> String)

    companion object {
        fun create(id: String, apply: (Entity, (String) -> String) -> Unit): EntityData {
            return object : EntityData {
                override val id: String = id
                override fun apply(entity: Entity, updater: (String) -> String) {
                    apply(entity, updater)
                }
            }
        }
    }
}