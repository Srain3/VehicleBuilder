package com.github.srain3.vehiclebuilder.core

import com.github.srain3.vehiclebuilder.util.Tools
import com.github.srain3.vehiclebuilder.util.Tools.sendColorMessage
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

object DistanceTraveled {

    private val oldVecMap = mutableMapOf<Entity,Vector>()
    private val mileage = mutableMapOf<Entity,Double>()
    private val passengerMap = mutableMapOf<ArmorStand,Entity?>()

    private val seatList = mutableMapOf<ArmorStand, MutableSet<Pair<Int, ArmorStand>>>()

    private fun task(newVec: Vector, list: MutableMap<Pair<Int, ArmorStand>,Entity?>) {
        list.forEach { (amst, player) ->
            if (player != null) {
                if (player !is Player) return@forEach
                mileage[player] = (mileage[player] ?: 0.0) + (oldVecMap[player] ?: newVec).distance(newVec)
                oldVecMap[player] = newVec
            } else {
                val pairKey = passengerMap.keys.firstOrNull { it.uniqueId == amst.second.uniqueId } ?: return@forEach
                val uuid = passengerMap[pairKey] ?: return@forEach
                vehicleExitTask(uuid,pairKey)
            }
        }
    }

    private fun vehicleExitTask(player: Entity, pairKey: ArmorStand) {

        val mileage0 = mileage[player] ?: return
        oldVecMap.remove(player)
        passengerMap.remove(pairKey)
        mileage.remove(player)
        if (player !is Player) return

        if (player.isOnline) {
            player.sendColorMessage("<blue>乗車距離</blue><gray>: </gray><aqua>${mileage0.toInt()}m</aqua>")
        }
    }

    /**
     * 走行距離(乗車距離)計測スタート
     */
    fun start(mainAmSt: ArmorStand,seatAmSt: MutableSet<Pair<Int, ArmorStand>>) {
        seatList[mainAmSt] = seatAmSt
        object : BukkitRunnable() {
            override fun run() {
                val newVec = mainAmSt.location.toVector()
                val playerList = mutableMapOf<Pair<Int, ArmorStand>,Entity?>()
                seatList[mainAmSt]?.toSet()?.forEach { amst ->
                    val player = amst.second.passengers.firstOrNull { it.type == EntityType.PLAYER }
                    playerList[amst] = player
                }
                task(newVec, playerList)
                if (mainAmSt.isDead) {
                    seatList.remove(mainAmSt)
                    cancel() ; return
                }
            }
        }.runTaskTimer(Tools.plugin,1,1)
    }

    /**
     * シート追加
     */
    fun addSeat(mainAmSt: ArmorStand, addSeat: Pair<Int, ArmorStand>) {
        val set = seatList[mainAmSt]?.toMutableSet() ?: return
        set.add(addSeat)
        seatList[mainAmSt] = set
    }

    /**
     * シート消去
     */
    fun removeSeat(mainAmSt: ArmorStand, removeSeat: Pair<Int, ArmorStand>) {
        seatList[mainAmSt]?.remove(removeSeat)
    }
}