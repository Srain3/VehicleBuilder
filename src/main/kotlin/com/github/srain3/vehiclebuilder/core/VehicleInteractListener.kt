package com.github.srain3.vehiclebuilder.core

import com.github.srain3.vehiclebuilder.core.base.AbsEntityData
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.world.EntitiesUnloadEvent

object VehicleInteractListener: Listener {

    /**
     * アマスタのダメージイベント時、運転席以外の場合破壊を防ぐ。運転席の場合壊す処理を行う
     */
    @EventHandler
    fun amstDamageEvent(event: EntityDamageEvent) {
        if (event.entity.type != EntityType.ARMOR_STAND) return
        if (!VehicleEntityList.checkArmorStandUUID(event.entity.uniqueId)) return
        val data = VehicleEntityList.checkDrivingSeat(event.entity.uniqueId)
        if (data != null) {
            if (data.seat1.keys.firstOrNull()?.second?.passengers?.isEmpty() == true) {
                if (!data.exit) {
                    data.exit = true
                    data.exitTask()
                }
                event.isCancelled = true
                return
            }
        }
        event.isCancelled = true
    }

    /**
     * 壁に当たるダメージ軽減
     */
    @EventHandler
    fun wallDamageCancel(event: EntityDamageEvent) {
        if (!(event.cause == EntityDamageEvent.DamageCause.FALL ||
                    event.cause == EntityDamageEvent.DamageCause.SUFFOCATION)) return
        if (event.entity !is Player) return
        if (!VehicleEntityList.checkSeatPlayerUUID(event.entity.uniqueId)) return
        event.isCancelled = true
    }

    /**
     * エンティティアンロード時に車を消去する処理
     */
    @EventHandler
    fun unloadEntity(event: EntitiesUnloadEvent) {
        val amstList = event.entities.filter { it.type == EntityType.ARMOR_STAND }
        val dataList = mutableSetOf<AbsEntityData>()
        amstList.forEach {
            val data = VehicleEntityList.getData(it.uniqueId)
            if (data != null) {
                dataList.add(data)
            }
        }
        dataList.distinct()

        dataList.forEach { data ->
            if (!data.exit) {
                data.exit = true
                data.exitTask()
            }
        }
    }

    /**
     * 右クリックで出現させる
     */
    /** @EventHandler (priority = EventPriority.HIGH)
    fun playerRightClickEvent(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val item = event.item?.clone() ?: return
        if (!event.hasBlock()) return
        if (!CarBuilder2Item.checkCarItem(item)) return
        if (event.blockFace != BlockFace.UP) return
        if (event.isCancelled) return
        if (railList.contains(event.clickedBlock!!.type)) {
            event.isCancelled = true
        }

        val clickLoc = event.clickedBlock?.location?.add(0.5,1.0,0.5) ?: return

        val carName = CarBuilder2Item.getCarName(item.itemMeta!!) ?: return
        val data = CB2BaseCache.getBaseData(carName) ?: return
        CarBuilder2Spawn.spawn(clickLoc,item,event.player,event.player,data,event.player.eyeLocation.yaw)

        event.player.inventory.itemInMainHand.amount = 0
    }*/

    /**
     * シートを右クリックでも乗れるようにする
     */
    @EventHandler
    fun playerArmorStandRightClick(event: PlayerInteractAtEntityEvent) {
        if (event.rightClicked.type != EntityType.ARMOR_STAND) return
        if (event.rightClicked.passengers.isNotEmpty()) return
        if (VehicleEntityList.checkSeatAmSt(event.rightClicked.uniqueId)) {
            event.rightClicked.addPassenger(event.player)
            event.isCancelled = true
            val data = VehicleEntityList.checkDrivingSeat(event.rightClicked.uniqueId)
            data?.sendPlayerSitMessage(event.player)
        }
    }
}