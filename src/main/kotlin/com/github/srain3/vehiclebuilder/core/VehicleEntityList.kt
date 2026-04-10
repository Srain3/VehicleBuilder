package com.github.srain3.vehiclebuilder.core

import com.github.srain3.vehiclebuilder.core.base.AbsEntityData
import org.bukkit.entity.ArmorStand
import java.util.UUID

object VehicleEntityList {
    private val cache = mutableSetOf<AbsEntityData>()

    fun addVehicleEntity(data: AbsEntityData) {
        cache.add(data)
    }

    fun removeVehicleEntity(data: AbsEntityData) {
        cache.remove(data)
    }

    /**
     * すべてのアマスタのUUIDで一致するかを返す、一致するものがあればtrue
     */
    fun checkArmorStandUUID(uuid: UUID): Boolean {
        cache.forEach { data ->
            data.seat1.forEach { (amst, _) ->
                if (amst.second.uniqueId == uuid) {
                    return true
                }
            }
            data.amst1.forEach { amst ->
                if (amst.uniqueId == uuid) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * すべての運転席アマスタからUUIDで一致したものがあれば[AbsEntityData]を返す、無ければnullを返す
     */
    fun checkDrivingSeat(uuid: UUID): AbsEntityData? {
        return cache.firstOrNull { data ->
            data.seat1.keys.firstOrNull {it.first == 0}?.second?.uniqueId == uuid
        }
    }

    /**
     * すべての座席アマスタから一致するかを返す、一致すればtrue
     */
    fun checkSeatAmSt(uuid: UUID): Boolean {
        cache.forEach { data ->
            data.seat1.forEach { (amst, _) ->
                if (amst.second.uniqueId == uuid) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * すべての車の乗客(乗ってるプレイヤー)からUUIDで一つでも一致したらtrueを返す
     */
    fun checkSeatPlayerUUID(uuid: UUID): Boolean {
        cache.forEach { data ->
            data.getCarPlayers().forEach { player ->
                if (player.uniqueId == uuid) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * すべてのアマスタのUUIDで一致したものを含む[AbsEntityData]を返す、無ければnull
     */
    fun getData(uuid: UUID): AbsEntityData? {
        cache.forEach { data ->
            data.seat1.keys.forEach { amst ->
                if (amst.second.uniqueId == uuid) {
                    return data
                }
            }
            data.amst1.forEach { amst ->
                if (amst.uniqueId == uuid) {
                    return data
                }
            }
        }
        return null
    }

    /**
     * すべてのメインアマスタのUUIDで一致したものを含む[AbsEntityData]を返す、無ければnull
     */
    fun getDataIsMainAmSt(uuid: UUID): AbsEntityData? {
        cache.forEach { data ->
            if (data.amst1.first().uniqueId == uuid) {
                return data
            }
        }
        return null
    }

    /**
     * 元のBaseData名と一致したものを含む[AbsEntityData]を返す、無ければEmpty
     */
    fun getData(name: String): MutableList<AbsEntityData> {
        val list = mutableListOf<AbsEntityData>()
        cache.forEach { data ->
            if (data.baseData.name == name) {
                list.add(data)
            }
        }
        return list
    }

    /**
     * 指定されたUUID(のユーザー)の出した車両を壊す。壊した台数を返す
     */
    fun breakUserCar(uuid: UUID): Int {
        var count = 0
        cache.forEach { data ->
            if (data.summoner == uuid) {
                if (!data.exit) {
                    data.exit = true
                    data.exitTask()
                    count++
                }
            }
        }
        return count
    }

    /**
     * すべての車両に終了処理を行わせる
     */
    fun allExit() {
        cache.forEach { data ->
            if (!data.exit) {
                data.exit = true
                data.exitTask()
            }
        }
    }

    /**
     * スリップ判定用のアマスタリストを返す
     */
    fun getMainAmStList(uuid: UUID?): List<ArmorStand> {
        val list = mutableListOf<ArmorStand>()
        cache.forEach { data ->
            if (data.amst1.first().uniqueId != uuid) {
                list.add(data.amst1.first())
            }
        }
        return list
    }
}