package com.github.srain3.vehiclebuilder.core.base

import com.github.srain3.vehiclebuilder.util.CustomYaml
import com.github.srain3.vehiclebuilder.core.base.BaseDataType.*
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.UUID

abstract class AbsConfigData(
    path: String
): CustomYaml(path) {
    /**
     * シート設定を返す、設定がない場合はオール0の運転席のみを返す
     */
    fun getSeatList(): MutableMap<Int, Vector> {
        val keyList = this.getKeys(true)
        val seatList = sortedMapOf<Int, Vector>()
        keyList.forEach { i ->
            if (Regex("""Seat.[0-9]+""").matches(i)) {
                val vec = this.getVector(i) ?: Vector().zero()
                val num = i.replace("Seat.","").toInt()
                seatList[num] = vec
            }
        }
        if (seatList.isEmpty()) {
            seatList[0] = Vector(0.0,0.1,0.0)
        }
        return seatList
    }

    /**
     * サイズを返す
     */
    fun getSize(type: BaseDataType): Double {
        val def = when(type) {
            Body -> { 2.8 }
            Wheel -> { 0.35 }
            Wheel2 -> { 0.35 }
            HeadLight -> { 0.35 }
        }
        return this.getDouble("${type.configName}.Size", def)
    }

    /**
     * Bodyオフセットを返す
     */
    fun getOffsetBody(): Vector {
        return this.getVector("Body.Offset") ?: Vector().zero()
    }

    /**
     * 複数あるオフセットを返す(Wheel/HeadLightなど)
     */
    fun getOffset(type: BaseDataType): MutableMap<Int, Vector> {
        val keyList = this.getKeys(true)
        val list = sortedMapOf<Int, Vector>()
        keyList.forEach { i ->
            if (Regex("""${type.configName}.Offset.[0-9]+""").matches(i)) {
                val vec = this.getVector(i) ?: Vector().zero()
                val num = i.replace("${type.configName}.Offset.","").toInt()
                list[num] = vec
            }
        }
        return list
    }

    /**
     * 保存者UUIDを返す、無ければnullを返す
     */
    fun getOwnerUUID(): UUID? {
        val str = this.getString("owner") ?: return null
        return UUID.fromString(str)
    }

    /**
     * 保存者である、又はOP権限持ちの場合true。他人の場合false
     */
    fun isOwnerOrOP(sender: CommandSender): Boolean {
        val playerUUID = when (sender) {
            is Player -> {
                if (sender.isOp) return true
                sender.uniqueId
            }

            is ConsoleCommandSender -> {
                return true
            }

            is BlockCommandSender -> {
                return true
            }

            else -> {
                return false
            }
        }
        val ownerUUID = getOwnerUUID()
        return ownerUUID?.toString() == playerUUID.toString()
    }
}