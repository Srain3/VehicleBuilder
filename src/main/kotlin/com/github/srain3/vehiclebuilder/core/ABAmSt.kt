package com.github.srain3.vehiclebuilder.core

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.event.entity.CreatureSpawnEvent

class ABAmSt(loc: Location, x: Double, y: Double): ArmorStand(
    (loc.world as CraftWorld).handle,
    loc.x,
    loc.y,
    loc.z
) {
    private val aabbX = x * 0.707
    private val aabbY = y

    init {
        (loc.world as CraftWorld).handle.addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)
    }

    override fun makeBoundingBox(position: Vec3): AABB {
        return super.makeBoundingBox(position)
            .expandTowards(aabbX,aabbY,aabbX)
            .expandTowards(-aabbX,0.0,-aabbX)
    }

    override fun getPassengerRidingPosition(entity: Entity): Vec3 {
        return this.position().add(0.0,1.0,0.0)
    }
}