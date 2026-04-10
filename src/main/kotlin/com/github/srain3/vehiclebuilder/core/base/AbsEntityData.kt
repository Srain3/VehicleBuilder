package com.github.srain3.vehiclebuilder.core.base

import com.github.srain3.vehiclebuilder.core.DistanceTraveled
import com.github.srain3.vehiclebuilder.core.VehicleEntityList
import com.github.srain3.vehiclebuilder.core.base.BaseDataType.*
import com.github.srain3.vehiclebuilder.util.GetEntity
import com.github.srain3.vehiclebuilder.util.Tools
import com.github.srain3.vehiclebuilder.util.Tools.copy
import com.github.srain3.vehiclebuilder.util.Tools.toComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Vector3f
import java.util.*

abstract class AbsEntityData(
    val seat1: MutableMap<Pair<Int, ArmorStand>, Vector>,
    val amst1: MutableList<ArmorStand>,
    val display: MutableMap<BaseDataType, MutableMap<Pair<Int, Vector>, MutableSet<BlockDisplay>>>,
    val vehicle1: BaseVehicle,
    var owner: UUID?,
    val summoner: UUID?,
    val baseData: AbsBaseData,
    // val protocolLib: ProtocolManager?,
    val item: ItemStack? = null,
) {
    val displayTypeList: MutableMap<BaseDisplayType, MutableSet<BlockDisplay>> = mutableMapOf()
    val displayDef: MutableMap<BaseDataType, MutableMap<BlockDisplay, Transformation>> = mutableMapOf()

    private val bossBarKey = NamespacedKey(Tools.plugin, "VB_${amst1[0].uniqueId}")
    val bossBar = Bukkit.createBossBar(bossBarKey, "| 0km/h |", BarColor.WHITE, BarStyle.SEGMENTED_10)

    var exit: Boolean = false
    var driveStartSwitch: Boolean = false
    val arrayEID = mutableSetOf<Int>()
    var arrayEntityID = arrayEID.toList()

    fun seatOffsetChange(newOffset: Pair<Int, Vector>) {
        val oldKey = seat1.keys.firstOrNull { it.first == newOffset.first }
        if (oldKey != null) {
            seat1[oldKey] = newOffset.second
        } else {
            val newAmSt = amst1[0].world.spawn(amst1[0].location, ArmorStand::class.java) { newAmSt ->
                newAmSt.isSmall = true
                newAmSt.isSilent = true
                newAmSt.isInvisible = true
                newAmSt.setGravity(false)
                newAmSt.setRotation(amst1[0].location.yaw, 0F)
                newAmSt.setBasePlate(false)
                newAmSt.customName("vb_entity".toComponent())
            }
            val p = Pair(newOffset.first, newAmSt)
            seat1[p] = newOffset.second
            DistanceTraveled.addSeat(amst1[0], p)
        }
    }

    fun removeSeat(delInt: Int) {
        val oldKey = seat1.keys.firstOrNull { it.first == delInt } ?: return
        DistanceTraveled.removeSeat(amst1[0], oldKey)
        seat1.remove(oldKey)
        oldKey.second.remove()
    }

    fun getControlPlayer(): Player? {
        return seat1.keys.firstOrNull { it.first == 0 }?.second?.passengers?.filterIsInstance<Player>()?.firstOrNull()
    }

    /**
     * シートに乗ってるプレイヤー全取得
     */
    fun getCarPlayers(): MutableList<Player> {
        val list = mutableListOf<Player>()
        seat1.keys.forEach { armorStand ->
            val entity = armorStand.second.passengers.firstOrNull { it is Player }
            if (entity != null && entity is Player) {
                list.add(entity)
            }
        }
        return list
    }

    abstract fun sendPlayerSitMessage(player: Player)

    /**
     * シートの近くにいるプレイヤーを自動で座らせるタイマー起動
     */
    fun autoSitStart() {
        val delayList = mutableMapOf<UUID, Int>()
        // 自動で座らせる機能 + シートの羊毛セッティング
        object : BukkitRunnable() {
            override fun run() {
                if (exit) {
                    cancel() ; return
                }
                if (seat1.keys.first().second.isDead) {
                    exit = true
                    exitTask()
                    cancel() ; return
                }

                if (vehicle1.speed.z in -0.05..0.05) {
                    seat1.keys.forEach { armorStand ->
                        if (armorStand.second.passengers.isEmpty()) {
                            // アマスタに乗客がいない場合
                            // 近くのプレイヤーを探す
                            val player = GetEntity.getNearbyPlayers(armorStand.second.eyeLocation, 0.3, 1.5, 0.3).firstOrNull {
                                !it.isSneaking && it.isOnGround
                            }
                            if (player != null) {
                                // delayListに居るかどうかを確認
                                if (delayList[player.uniqueId] == null) {
                                    // delayの必要がないプレイヤーの場合乗せる
                                    armorStand.second.addPassenger(player)
                                    if (armorStand.first == 0) {
                                        sendPlayerSitMessage(player)
                                    }
                                } else if ((delayList[player.uniqueId] ?: 100) <= 0) {
                                    // delayが0以下プレイヤーの場合乗せる
                                    armorStand.second.addPassenger(player)
                                    if (armorStand.first == 0) {
                                        sendPlayerSitMessage(player)
                                    }
                                }
                            }

                            if (armorStand.first == 0) {
                                armorStand.second.setHelmet(ItemStack(Material.GREEN_WOOL))
                            } else {
                                armorStand.second.setHelmet(ItemStack(Material.BLACK_WOOL))
                            }
                        } else {
                            // アマスタに乗客がいる場合
                            // 乗客のUUIDでdelayListに追加する
                            delayList[armorStand.second.passengers.first().uniqueId] = 5

                            // 羊毛
                            val slots = armorStand.second.equipment
                            if (slots.helmet != null) {
                                slots.helmet = null
                            }
                        }
                    }
                    val newList = mutableMapOf<UUID, Int>()
                    delayList.forEach { (uuid, count) ->
                        if (count > 0) {
                            newList[uuid] = count - 1
                        }
                    }
                    delayList.clear()
                    delayList.plusAssign(newList)
                }

                //joinBody()
                if (seat1.keys.first().second.passengers.isNotEmpty() && !driveStartSwitch) {
                    start()
                }
            }
        }.runTaskTimer(Tools.plugin, 20, 10)
    }

    abstract fun start()
    abstract fun exitTask()
    abstract fun refreshDisplayEntityID()

    /**
     * pLibで擬似的に乗せる
     */
    /**fun joinBody() {
        if (protocolLib1 == null) return
        val players = GetEntity.getNearbyPlayers(amst1[0].location, 64.0, 64.0, 64.0)
        val eID = amst1[0].entityId

        players.forEach { player ->
            refreshDisplayEntityID()

            val packet = PacketContainer(PacketType.Play.Server.MOUNT)
            packet.modifier.write(0, eID)
            packet.modifier.write(1, arrayEntityID.toIntArray())

            protocolLib1.sendServerPacket(player, packet)
        }

    }*/

    fun saveDef(type: BaseDataType) {
        displayDef[type]?.clear()
        val map = mutableMapOf<BlockDisplay, Transformation>()
        display[type]?.values?.forEach { set ->
            set.forEach { blockDisplay ->
                map[blockDisplay] = blockDisplay.transformation.copy()
            }
        }
        displayDef[type] = map
    }

    fun reOffsetDisplay(type: BaseDataType, newOffset: MutableMap<Int, Vector>) {
        val hit = mutableListOf<Pair<Int, Vector>>()

        display[type]?.forEach { (offsetPair, set) ->
            val newVec = newOffset[offsetPair.first]?: return@forEach
            hit.add(offsetPair)
            val addVec = newVec.clone().subtract(offsetPair.second)

            if (type == Body) {
                display.forEach { (_, map) ->
                    map.values.forEach { set2 ->
                        set2.forEach { blockDisplay ->
                            blockDisplay.transformation = Transformation(
                                Vector3f(blockDisplay.transformation.translation).add(addVec.toVector3f()),
                                blockDisplay.transformation.leftRotation,
                                blockDisplay.transformation.scale,
                                blockDisplay.transformation.rightRotation
                            )
                        }
                    }
                }
            } else {
                set.forEach { blockDisplay ->
                    blockDisplay.transformation = Transformation(
                        Vector3f(blockDisplay.transformation.translation).add(addVec.clone().toVector3f()),
                        blockDisplay.transformation.leftRotation,
                        blockDisplay.transformation.scale,
                        blockDisplay.transformation.rightRotation
                    )
                }
            }
        }

        hit.forEach { p ->
            var b = false
            display[type]?.keys?.forEach { pair ->
                if (p == pair) {
                    b = true
                }
            }
            if (b) {
                val data = display[type]?.remove(p) ?: return@forEach
                display[type]?.set(Pair(p.first, newOffset[p.first]!!), data)
            }
        }

    }

}