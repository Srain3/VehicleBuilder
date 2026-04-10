package com.github.srain3.vehiclebuilder.core

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.event.entity.CreatureSpawnEvent

class LiteAmSt(loc: Location): ArmorStand(
    (loc.world as CraftWorld).handle,
    loc.x,
    loc.y,
    loc.z
) {

    init {
        (loc.world as CraftWorld).handle.addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)
    }

    override fun getPassengerRidingPosition(entity: Entity): Vec3 {
       return this.position().add(0.0,1.0,0.0)
    }
}