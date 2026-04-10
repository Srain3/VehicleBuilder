package com.github.srain3.vehiclebuilder.core.base

import org.bukkit.util.Vector

abstract class BaseVehicle(
    val speedLimit0: Double,
    val power0: Int,
    val brake0: Int
) {
    val speed: Vector = Vector().zero()

    fun speedCheck(): Boolean {
        return speed.z > speedLimit0
    }
}