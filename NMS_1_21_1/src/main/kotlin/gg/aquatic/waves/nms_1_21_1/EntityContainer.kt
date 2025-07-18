package gg.aquatic.waves.nms_1_21_1

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType

class EntityContainer(id: Int) : Entity(EntityType.INTERACTION, MinecraftServer.getServer().overworld()) {

    override fun defineSynchedData(p0: SynchedEntityData.Builder) {

    }

    override fun readAdditionalSaveData(p0: CompoundTag) {

    }

    override fun addAdditionalSaveData(p0: CompoundTag) {

    }

    init {
        this.id = id
        setPosRaw(0.0, 0.0, 0.0)
        setRot(0.0f, 0.0f)
        setOnGround(false)
    }

}