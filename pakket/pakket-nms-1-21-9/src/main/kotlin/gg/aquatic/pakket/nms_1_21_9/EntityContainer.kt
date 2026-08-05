package gg.aquatic.pakket.nms_1_21_9

import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class EntityContainer(id: Int) : Entity(EntityType.INTERACTION, MinecraftServer.getServer().overworld()) {

    override fun defineSynchedData(p0: SynchedEntityData.Builder) {}

    override fun hurtServer(
        p0: ServerLevel,
        p1: DamageSource,
        p2: Float
    ): Boolean {
        return false
    }

    override fun readAdditionalSaveData(p0: ValueInput) {}

    override fun addAdditionalSaveData(output: ValueOutput, includeAll: Boolean) {}

    override fun addAdditionalSaveData(p0: ValueOutput) {}

    init {
        this.id = id
        setPosRaw(0.0, 0.0, 0.0)
        setRot(0.0f, 0.0f)
        setOnGround(false)
    }
}