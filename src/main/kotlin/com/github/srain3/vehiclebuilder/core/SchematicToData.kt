package com.github.srain3.vehiclebuilder.core

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.io.File
import java.io.FileInputStream

object SchematicToData {
    /**
     * Schematicファイルからブロックデータを取り出す。失敗するとnullを返す
     */
    fun fileToRawBlockData(file: File): MutableMap<Vector, BlockData>? {
        val rawBlockData = mutableMapOf<Vector, BlockData>()
        if (file.isFile) {
            if (file.exists()) {
                // WEのschematicからブロックデータを取得する
                var clipboard: Clipboard
                val format = ClipboardFormats.findByFile(file) ?: return null
                format.getReader(FileInputStream(file)).use { reader -> clipboard = reader.read() }
                val region = clipboard.region.clone()

                for (h in 0 until region.height) {
                    for (w in 0 until region.width) {
                        for (l in 0 until region.length) {

                            val vec3 = clipboard.minimumPoint.add(w,h,l)
                            val blockState = clipboard.getBlock(vec3)
                            val bukkitBlockData = BukkitAdapter.adapt(blockState)
                            if (bukkitBlockData.material != Material.AIR) {
                                val vec = Vector(w,h,l)
                                rawBlockData[vec] = bukkitBlockData
                            }

                        }
                    }
                }

                return rawBlockData
            }
        }
        return null
    }

    /**
     * サイズを返す
     */
    fun size(raw: MutableMap<Vector, BlockData>): Pair<BoundingBox, Pair<Vector, Vector>> {
        // 初期値に極端な値を設定
        var minX = Double.POSITIVE_INFINITY
        var minY = Double.POSITIVE_INFINITY
        var minZ = Double.POSITIVE_INFINITY
        var maxX = Double.NEGATIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY
        var maxZ = Double.NEGATIVE_INFINITY

        // 1回のループで全ての最小・最大を確定させる
        for (v in raw.keys) {
            val x = v.x
            val y = v.y
            val z = v.z

            if (x < minX) minX = x
            if (x > maxX) maxX = x
            if (y < minY) minY = y
            if (y > maxY) maxY = y
            if (z < minZ) minZ = z
            if (z > maxZ) maxZ = z
        }

        val minVec = Vector(minX, minY, minZ)
        val maxVec = Vector(maxX, maxY, maxZ)
        val vec = Vector().apply {
            x = maxVec.x - minVec.x + 1
            y = maxVec.y - minVec.y + 1
            z = maxVec.z - minVec.z + 1
        }
        val box = BoundingBox.of(Vector().zero(), vec)

        return Pair(box,Pair(maxVec,minVec))
    }

    /**
     * RawBlockDataを最適化したものを返す
     */
    fun compressBlockData(raw: MutableMap<Vector,BlockData>, size: Pair<BoundingBox, Pair<Vector, Vector>>): MutableMap<Vector,Pair<BlockData,Vector>> {
        if (raw.isEmpty()) {
            return mutableMapOf()
        }
        val box = size.first
        val maxVec = size.second.first
        val minVec = size.second.second

        val yxz = compressUnified(raw, box, maxVec, minVec, MeshOrder.YXZ)
        val xzy = compressUnified(raw, box, maxVec, minVec, MeshOrder.XZY)
        return if (yxz.size <= xzy.size) {
            yxz
        } else {
            xzy
        }
    }

    /**
     * 探索順序を定義するEnum
     * v1: 最初に伸ばす方向, v2: 次に広げる方向, v3: 最後に厚みを出す方向
     */
    enum class MeshOrder(val v1: Vector, val v2: Vector, val v3: Vector) {
        YXZ(Vector(0, 1, 0), Vector(1, 0, 0), Vector(0, 0, 1)),
        XZY(Vector(1, 0, 0), Vector(0, 0, 1), Vector(0, 1, 0))
    }

    /**
     * 統合された最適化関数
     */
    private fun compressUnified(
        raw: MutableMap<Vector, BlockData>,
        box: BoundingBox,
        max: Vector,
        min: Vector,
        order: MeshOrder
    ): MutableMap<Vector, Pair<BlockData, Vector>> {
        val checkList = mutableSetOf<Vector>()
        val compressData = mutableMapOf<Vector, Pair<BlockData, Vector>>()
        val center = box.center

        raw.forEach { (vec, blockData) ->
            if (vec !in checkList) {
                checkList.add(vec)
                val offsetVec = vec.clone().subtract(min).subtract(center)

                // 探索中のサイズを保持（s1, s2, s3 は order の v1, v2, v3 に対応）
                var s1 = 0; var s2 = 0; var s3 = 0

                // 1. 第1軸方向 (v1) の調査
                val limit1 = getLimitForVector(order.v1, max)
                for (i in 1..limit1) {
                    val next = vec.clone().add(order.v1.clone().multiply(i))
                    if (raw[next] == blockData && next !in checkList) {
                        checkList.add(next)
                        s1++
                    } else break
                }

                // 2. 第2軸方向 (v2) の面調査
                val limit2 = getLimitForVector(order.v2, max)
                for (i in 1..limit2) {
                    // 現在の(s1)の範囲すべてにおいて、v2方向にブロックがあるかチェック
                    val canExpand = (0..s1).all { i1 ->
                        val target = vec.clone()
                            .add(order.v1.clone().multiply(i1))
                            .add(order.v2.clone().multiply(i))
                        raw[target] == blockData && target !in checkList
                    }

                    if (canExpand) {
                        (0..s1).forEach { i1 ->
                            checkList.add(vec.clone().add(order.v1.clone().multiply(i1)).add(order.v2.clone().multiply(i)))
                        }
                        s2++
                    } else break
                }

                // 3. 第3軸方向 (v3) の体積調査
                val limit3 = getLimitForVector(order.v3, max)
                for (i in 1..limit3) {
                    // 現在の(s1, s2)の面すべてにおいて、v3方向にブロックがあるかチェック
                    val canExpand = (0..s1).all { i1 ->
                        (0..s2).all { i2 ->
                            val target = vec.clone()
                                .add(order.v1.clone().multiply(i1))
                                .add(order.v2.clone().multiply(i2))
                                .add(order.v3.clone().multiply(i))
                            raw[target] == blockData && target !in checkList
                        }
                    }

                    if (canExpand) {
                        (0..s1).forEach { i1 ->
                            (0..s2).forEach { i2 ->
                                checkList.add(vec.clone()
                                    .add(order.v1.clone().multiply(i1))
                                    .add(order.v2.clone().multiply(i2))
                                    .add(order.v3.clone().multiply(i)))
                            }
                        }
                        s3++
                    } else break
                }

                // サイズを X, Y, Z にマッピングし直す
                val finalSize = calculateFinalSize(order, s1 + 1, s2 + 1, s3 + 1)
                compressData[offsetVec] = Pair(blockData.clone(), finalSize)
            }
        }
        return compressData
    }

    /**
     * 特定のベクトルの向きにおける最大探索距離を取得
     */
    private fun getLimitForVector(v: Vector, max: Vector): Int {
        return when {
            v.x > 0 -> max.x.toInt()
            v.y > 0 -> max.y.toInt()
            v.z > 0 -> max.z.toInt()
            else -> 0
        }
    }

    /**
     * 探索順序(s1,s2,s3)を実際の(X,Y,Z)サイズに変換
     */
    private fun calculateFinalSize(order: MeshOrder, s1: Int, s2: Int, s3: Int): Vector {
        val size = DoubleArray(3)
        val orders = listOf(order.v1, order.v2, order.v3)
        val values = listOf(s1.toDouble(), s2.toDouble(), s3.toDouble())

        orders.forEachIndexed { index, v ->
            if (v.x > 0) size[0] = values[index]
            if (v.y > 0) size[1] = values[index]
            if (v.z > 0) size[2] = values[index]
        }
        return Vector(size[0], size[1], size[2])
    }
}