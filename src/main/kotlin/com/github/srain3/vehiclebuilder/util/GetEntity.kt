package com.github.srain3.vehiclebuilder.util

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

/**
 * NearやrayTraceのカスタムまとめ
 */
object GetEntity {
    /**
     * オンラインPlayerに対してlocを中心にxyzの箱の中に入っている物のみListで返す
     */
    fun getNearbyPlayers(loc: Location, x: Double, y: Double, z:Double): List<Player> {
        val aabb = BoundingBox.of(loc, x, y, z)
        val players = Bukkit.getOnlinePlayers()
        val list = mutableListOf<Player>()
        players.forEach { player ->
            if (player.world.uid == loc.world?.uid) {
                if (aabb.contains(player.location.x, player.location.y, player.location.z)) {
                    list.add(player)
                }
            }
        }
        return list
    }

    /**
     * listに対してlocを中心にxyzの箱の中に入っている物のみListで返す
     */
    fun getNearbyEntitys(list: List<Entity>, loc: Location, x: Double, y: Double, z:Double): List<Entity> {
        val aabb = BoundingBox.of(loc, x, y, z)
        val reList = mutableListOf<Entity>()
        list.forEach { entity ->
            if (entity.world.uid == loc.world?.uid) {
                if (aabb.contains(entity.location.x, entity.location.y, entity.location.z)) {
                    reList.add(entity)
                }
            }
        }
        return reList
    }

    /**
     * listにあるEntityに対してのみレイトレースを試みてhitしたらtrueを返す。Distanceが0.0以下だとnullを返す。
     */
    fun rayTraceEntities(
        start: Location,
        direction: Vector,
        maxDistance: Double,
        raySize: Double,
        searchEntityList: List<Entity>
    ): Boolean? {
        if (maxDistance < 0.0) {
            return null
        } else {
            val startPos = start.toVector()
            val var17: Iterator<*> = searchEntityList.iterator()
            var hit = false

            while (var17.hasNext()) {
                val entity = var17.next() as Entity
                val boundingBox = entity.boundingBox.expand(raySize)
                val hitResult = boundingBox.rayTrace(startPos, direction, maxDistance)
                if (hitResult != null) {
                    hit = true
                    break
                }
            }

            return hit
        }
    }
}