package com.github.srain3.vehiclebuilder.core.base

import com.github.srain3.vehiclebuilder.core.ABAmSt
import com.github.srain3.vehiclebuilder.core.LiteAmSt
import com.github.srain3.vehiclebuilder.util.Tools.toComponent
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.Rotatable
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f

interface EntitySpawnBase {
    /**
     * 座席用アマスタをスポーンしてオフセットVecと一緒に返す
     */
    fun spawnSeat(loc: Location, vecList: MutableMap<Int, Vector>): MutableMap<Pair<Int, ArmorStand>, Vector> {
        val list = mutableMapOf<Pair<Int, ArmorStand>, Vector>()

        vecList.forEach { (i, vec) ->
            val addVec = vec.clone().rotateAroundY(Math.toRadians(loc.yaw.toDouble()))
            val armorStand = loc.world?.spawnEntity(loc.clone().add(addVec), EntityType.ARMOR_STAND) as ArmorStand
            armorStand.isSmall = true
            armorStand.isSilent = true
            armorStand.isInvisible = true
            armorStand.setGravity(false)
            armorStand.setRotation(loc.yaw, 0F)
            armorStand.setBasePlate(false)
            armorStand.customName("vb_entity".toComponent())
            if (i == 0) {
                armorStand.equipment.setHelmet(ItemStack.of(Material.GREEN_WOOL), true)
            } else {
                armorStand.equipment.setHelmet(ItemStack.of(Material.BLACK_WOOL), true)
            }
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING)

            list[Pair(i, armorStand)] = vec
        }
        return list
    }

    /**
     * 大体100体のDisplay用のアーマースタンドの用意
     */
    fun spawnAmSt(bodyAmStList: MutableList<ArmorStand>, loc: Location, x: Double, y: Double) {
        for (i in bodyAmStList.indices) {
            val amst = if (i == 0) {
                ABAmSt(loc, x, y).bukkitEntity as ArmorStand
            } else {
                LiteAmSt(loc).bukkitEntity as ArmorStand
            }
            amst.isSmall = true
            amst.isInvisible = true
            amst.isSilent = true
            amst.setGravity(false)
            amst.setRotation(loc.yaw, 0F)
            amst.customName("vb_entity".toComponent())
            if (i != 0) {
                amst.isMarker = true
            }

            bodyAmStList.add(amst)
        }
    }

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
            if (!Tag.ITEMS_SKULLS.isTagged(map.first.material)) {
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
                blockDisplay.customName("vb_entity".toComponent())

                list.add(blockDisplay)
            }
        }
        return list
    }

    /**
     * フリップ版BlockDisplayタスク
     */
    fun spawnAutoFlip(
        loc: Location,
        amstList: MutableList<ArmorStand>,
        blockData: MutableMap<Vector, Pair<BlockData, Vector>>,
        offsetData: MutableMap<Int, Vector>,
        size: Double,
        box: BoundingBox
    ): MutableMap<Pair<Int, Vector>, MutableSet<BlockDisplay>> {
        val map = mutableMapOf<Pair<Int, Vector>, MutableSet<BlockDisplay>>()
        offsetData.forEach { (i, vec) ->
            val blockDisplays = if (vec.x >= 0.0) {
                spawnBlockDisplay(
                    loc, amstList, blockData, size, box
                )
            } else {
                val newBD = mutableMapOf<Vector, Pair<BlockData, Vector>>()
                blockData.forEach { (wVec, data) ->
                    val newScale = data.second.clone()
                    val newVec = wVec.clone().apply {
                        x = -x
                        x -= newScale.x
                    }

                    val bd = data.first.clone()
                    if (bd is Directional) {
                        when (bd.facing) {
                            BlockFace.EAST -> {
                                bd.facing = BlockFace.WEST
                            }
                            BlockFace.WEST -> {
                                bd.facing = BlockFace.EAST
                            }
                            else -> {}
                        }
                    }
                    if (bd is Rotatable) {
                        when (bd.rotation) {
                            BlockFace.EAST -> {
                                bd.rotation = BlockFace.WEST
                            }
                            BlockFace.WEST -> {
                                bd.rotation = BlockFace.EAST
                            }
                            else -> {}
                        }
                    }
                    if (bd is MultipleFacing) {
                        var west = false
                        var east = false
                        bd.faces.forEach {
                            if (it == BlockFace.WEST) {
                                east = true
                            } else if (it == BlockFace.EAST) {
                                west = true
                            }
                        }
                        if (west) {
                            bd.setFace(BlockFace.EAST, false)
                            bd.setFace(BlockFace.WEST, true)
                        }
                        if (east) {
                            if (!west) {
                                bd.setFace(BlockFace.WEST, false)
                            }
                            bd.setFace(BlockFace.EAST, true)
                        }
                    }

                    newBD[newVec] = Pair(bd,newScale)
                }
                spawnBlockDisplay(
                    loc, amstList, newBD, size, box
                )
            }
            map[Pair(i, vec)] = blockDisplays
        }
        return map
    }
}