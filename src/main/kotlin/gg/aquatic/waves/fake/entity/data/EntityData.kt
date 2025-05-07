package gg.aquatic.waves.fake.entity.data

import org.bukkit.entity.Entity

interface EntityData {

    val id: String
    fun apply(entity: Entity)

    companion object {
        fun create(id: String, apply: (Entity) -> Unit): EntityData {
            return object : EntityData {
                override val id: String = id
                override fun apply(entity: Entity) = apply(entity)
            }
        }
    }
}