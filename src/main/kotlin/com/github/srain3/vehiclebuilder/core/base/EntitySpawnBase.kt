package com.github.srain3.vehiclebuilder.core.base

import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.util.BoundingBox
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

interface EntitySpawnBase {
    /**
     * 複数のBlockDisplayをスポーンさせて指定されたアマスタListからランダムに乗せ、スポーンした複数のBlockDisplayを返す
     */
    fun spawnBlockDisplay(
        loc: Location,
        amstList: MutableList<ArmorStand>,
        data: MutableMap<Vector,Pair<BlockData, Vector>>,
        size: Double,
        box: BoundingBox
    ): MutableSet<BlockDisplay> {
        val list = mutableSetOf<BlockDisplay>()
        val spawnLoc = loc.clone().add(0.0,1.0,0.0)
        data.forEach { (vec, map) ->
            val blockDisplay = loc.world?.spawnEntity(spawnLoc, EntityType.BLOCK_DISPLAY) as BlockDisplay
            blockDisplay.block = map.first
            blockDisplay.brightness = Display.Brightness(0,15)
            val scaleVec = map.second
            val fixWidth = size / box.maxX

            blockDisplay.transformation = Transformation(
                vec.clone().multiply(fixWidth).toVector3f(),
                AxisAngle4f(),
                scaleVec.clone().multiply(fixWidth).toVector3f(),
                AxisAngle4f()
            )
            blockDisplay.setRotation(loc.yaw, 0F)
            blockDisplay.customName = "vb_entity"

            list.add(blockDisplay)
        }
        return list
    }
}