package com.github.srain3.vehiclebuilder.core.base

import com.github.srain3.vehiclebuilder.core.base.BaseDataType.Body
import com.github.srain3.vehiclebuilder.util.Tools
import com.sk89q.worldedit.WorldEdit
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.io.File

abstract class AbsBaseData(
    val name: String,
    val config: AbsConfigData
) {
    val boxSize: MutableMap<BaseDataType, BoundingBox> = mutableMapOf()
    val body: MutableMap<Vector, Pair<BlockData, Vector>> = mutableMapOf()
    val others: MutableMap<BaseDataType, MutableMap<Vector, Pair<BlockData, Vector>>> = mutableMapOf()

    init {
        load()
    }

    fun isEmpty(): Boolean {
        return totalEntity() == 0
    }

    /**
     * 一個あたりのエンティティ数を返す
     */
    fun getEntityCount(type: BaseDataType): Int {
        return when(type) {
            Body -> { body.size }
            else -> {
                others[type]?.size ?: 0
            }
        }
    }

    /**
     * ボディで最初に使われてるブロックの取得(GUI上の一覧用)
     */
    fun getBlock(): BlockData {
        return body.values.firstOrNull()?.first ?: Material.BARRIER.createBlockData()
    }

    /**
     * 出現する合計エンティティ数を返す
     */
    fun totalEntity(): Int {
        var count = 0
        BaseDataType.entries.forEach { type ->
            count += getEntityCount(type)
        }
        return count
    }

    abstract fun getFile(type: BaseDataType): File
    abstract fun reloadData(type: BaseDataType, worldEdit: WorldEdit?): Boolean

    private fun load() {
        val we = Tools.getWorldEditInstance() ?: return
        BaseDataType.entries.forEach { type ->
            reloadData(type, we)
        }
    }

    abstract fun deleteFiles(): Boolean
}