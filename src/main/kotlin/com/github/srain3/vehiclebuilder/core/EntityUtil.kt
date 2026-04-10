package com.github.srain3.vehiclebuilder.core

import com.github.srain3.vehiclebuilder.core.base.AbsEntityData
import com.github.srain3.vehiclebuilder.util.ControlKey
import net.minecraft.world.entity.Entity
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftArmorStand
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.collections.forEach

object EntityUtil {
    /**
     * NMSの座標移動タスク
     */
    private fun movePos(entity: Entity, loc: Location) {
        //entity.snapTo(loc.x, loc.y, loc.z)
        entity.teleportTo(loc.x, loc.y, loc.z)
    }

    fun movePos(amst: ArmorStand, loc: Location) {
        movePos((amst as CraftArmorStand).handle, loc)
    }

    /**
     * Yawとvelocity適用、シート位置の更新
     */
    private fun setYawAndVelocity(
        setVec: Vector, setYaw: Float, addYaw: Float,
        bodyArmorStand: MutableList<ArmorStand>,
        seatArmorStand: MutableMap<Pair<Int, ArmorStand>, Vector>,
        displayEntitys: MutableSet<Display>,
        players: MutableList<Player>
    ) {
        val main = bodyArmorStand[0]
        val loc = main.location
        if (setVec.x.isNaN()) {
            setVec.x = 0.0
        }
        if (setVec.y.isNaN()) {
            setVec.y = 0.0
        }
        if (setVec.z.isNaN()) {
            setVec.z = 0.0
        }
        val mainAmstLoc = bodyArmorStand[0].location.clone()
        bodyArmorStand.forEachIndexed { index, it ->
            it.velocity = setVec
            it.setRotation(setYaw, 0F)
            if (index != 0) {
                if (it.location.distance(mainAmstLoc) != 0.0) {
                    movePos((it as CraftArmorStand).handle, loc)
                }
            }
        }

        seatArmorStand.forEach { (amst, vec) ->
            amst.second.setRotation(setYaw, 0F)
            val distance = (Vector().distance(vec.clone().apply { y = 0.0 }) * 0.125) + 1.0
            val newLoc = loc.clone().add(
                vec.clone().rotateAroundY(
                    Math.toRadians(-(setYaw + addYaw * distance))
                )
            )
            movePos((amst.second as CraftArmorStand).handle, newLoc)
        }

        displayEntitys.forEach {
            it.setRotation(setYaw, 0F)
        }

        if (addYaw != 0F) {
            players.forEach { player ->
                rotatePlayerYaw(addYaw, player)
            }
        }

    }

    /**
     * プレイヤーの視点を合わせて回転させるタスク
     */
    private fun rotatePlayerYaw(addYaw: Float, player: Player) {
        //if (!OyasaiVehiclesCmd.camera(player.uniqueId)) return
        player.setRotation(player.eyeLocation.yaw + addYaw, player.eyeLocation.pitch)
    }

    /**
     * Yawとvelocity適用、シート位置の更新
     */
    fun AbsEntityData.setYawAndVelocity(setVec: Vector, setYaw: Float, addYaw: Float) {
        setYawAndVelocity(
            setVec, setYaw, addYaw,
            amst1,
            seat1,
            displayList,
            getCarPlayers()
        )
    }


    /**
     * プレイヤーのWASDキー入力を擬似的に取得する
     */
    @Suppress("UnstableApiUsage")
    fun getWASD(player: Player?): Pair<ControlKey, Vector>? {
        if (player == null) return null
        val input = player.currentInput
        val pVec = player.velocity.clone()
        return when {
            input.isForward -> { // W
                when {
                    input.isLeft -> { // A
                        Pair(ControlKey.WA, pVec)
                    }
                    input.isRight -> { // D
                        Pair(ControlKey.WD, pVec)
                    }
                    else -> {
                        Pair(ControlKey.W, pVec)
                    }
                }
            }
            input.isBackward -> { // S
                when {
                    input.isLeft -> { // A
                        Pair(ControlKey.SA, pVec)
                    }
                    input.isRight -> { // D
                        Pair(ControlKey.SD, pVec)
                    }
                    else -> {
                        Pair(ControlKey.S, pVec)
                    }
                }
            }
            else -> {
                when {
                    input.isLeft -> { // A
                        Pair(ControlKey.A, pVec)
                    }
                    input.isRight -> { // D
                        Pair(ControlKey.D, pVec)
                    }
                    else -> {
                        Pair(ControlKey.NONE, pVec)
                    }
                }
            }
        }
    }
}