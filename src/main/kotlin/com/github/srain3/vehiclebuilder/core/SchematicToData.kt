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
                //ClipboardFormats.findByInputStream { file.inputStream() } // 非推奨対策
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
        val mSet = raw.keys
        val maxVec = Vector().apply {
            x = mSet.maxOf { it.x }
            y = mSet.maxOf { it.y }
            z = mSet.maxOf { it.z }
        }
        val minVec = Vector().apply {
            x = mSet.minOf { it.x }
            y = mSet.minOf { it.y }
            z = mSet.minOf { it.z }
        }
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

        val yxz = compressYXZ(raw, box, maxVec, minVec)
        val xzy = compressXZY(raw, box, maxVec, minVec)
        return if (yxz.size <= xzy.size) {
            yxz
        } else {
            xzy
        }
    }

    /**
     * 最適化(YXZ順)
     */
    private fun compressYXZ(raw: MutableMap<Vector,BlockData>, box: BoundingBox, max: Vector, min: Vector): MutableMap<Vector,Pair<BlockData,Vector>> {
        val checkList = mutableSetOf<Vector>()
        val compressData = mutableMapOf<Vector,Pair<BlockData,Vector>>()
        val center = box.center

        raw.forEach { (vec, blockData) ->
            if (!checkList.contains(vec)) {
                checkList.add(vec)
                val offsetVec = vec.clone().subtract(min).subtract(center)
                var sizeX = 0 ; var sizeY = 0 ; var sizeZ = 0

                // Y+(縦)のブロックの調査
                for (y in 1..max.y.toInt()) {
                    if (raw[vec.clone().add(Vector(0,y,0))] == blockData) {
                        checkList.add(vec.clone().add(Vector(0,y,0)))
                        sizeY += 1
                    } else {
                        break
                    }
                }
                // X+(横)のブロックの調査
                for (x in 1..max.x.toInt()) {
                    val hitList = mutableListOf<Boolean>()
                    for (y in 0..sizeY) {
                        hitList.add(raw[vec.clone().add(Vector(x,y,0))] == blockData)
                        hitList.add(!checkList.contains(vec.clone().add(Vector(x,y,0))))
                    }
                    if (hitList.none { !it }) {
                        for (y in 0..sizeY) {
                            checkList.add(vec.clone().add(Vector(x,y,0)))
                        }
                        sizeX += 1
                    } else {
                        break
                    }
                }
                // Z+(奥)のブロックの調査
                for (z in 1..max.z.toInt()) {
                    val hitList = mutableListOf<Boolean>()
                    for (y in 0..sizeY) {
                        for (x in 0..sizeX) {
                            hitList.add(raw[vec.clone().add(Vector(x, y, z))] == blockData)
                            hitList.add(!checkList.contains(vec.clone().add(Vector(x, y, z))))
                        }
                    }
                    if (hitList.none { !it }) {
                        for (y in 0..sizeY) {
                            for (x in 0..sizeX) {
                                checkList.add(vec.clone().add(Vector(x, y, z)))
                            }
                        }
                        sizeZ += 1
                    } else {
                        break
                    }
                }

                compressData[offsetVec] = Pair(blockData.clone(), Vector(sizeX+1,sizeY+1,sizeZ+1))
            }
        }

        return compressData
    }

    /**
     * 最適化(XZY順)
     */
    private fun compressXZY(raw: MutableMap<Vector,BlockData>, box: BoundingBox, max: Vector, min: Vector): MutableMap<Vector,Pair<BlockData,Vector>> {
        val checkList = mutableSetOf<Vector>()
        val compressData = mutableMapOf<Vector,Pair<BlockData,Vector>>()
        val center = box.center

        raw.forEach { (vec, blockData) ->
            if (!checkList.contains(vec)) {
                checkList.add(vec)
                val offsetVec = vec.clone().subtract(min).subtract(center)
                var sizeX = 0 ; var sizeY = 0 ; var sizeZ = 0

                // X+(横)のブロックの調査
                for (x in 1..max.x.toInt()) {
                    if (raw[vec.clone().add(Vector(x,0,0))] == blockData) {
                        checkList.add(vec.clone().add(Vector(x,0,0)))
                        sizeX += 1
                    } else {
                        break
                    }
                }
                // Z+(奥)のブロックの調査
                for (z in 1..max.z.toInt()) {
                    val hitList = mutableListOf<Boolean>()
                    for (x in 0..sizeX) {
                        hitList.add(raw[vec.clone().add(Vector(x,0,z))] == blockData)
                        hitList.add(!checkList.contains(vec.clone().add(Vector(x,0,z))))
                    }
                    if (hitList.none { !it }) {
                        for (x in 0..sizeX) {
                            checkList.add(vec.clone().add(Vector(x,0,z)))
                        }
                        sizeZ += 1
                    } else {
                        break
                    }
                }
                // Y+(縦)のブロックの調査
                for (y in 1..max.y.toInt()) {
                    val hitList = mutableListOf<Boolean>()
                    for (x in 0..sizeX) {
                        for (z in 0..sizeZ) {
                            hitList.add(raw[vec.clone().add(Vector(x, y, z))] == blockData)
                            hitList.add(!checkList.contains(vec.clone().add(Vector(x, y, z))))
                        }
                    }
                    if (hitList.none { !it }) {
                        for (x in 0..sizeX) {
                            for (z in 0..sizeZ) {
                                checkList.add(vec.clone().add(Vector(x, y, z)))
                            }
                        }
                        sizeY += 1
                    } else {
                        break
                    }
                }

                compressData[offsetVec] = Pair(blockData.clone(), Vector(sizeX+1,sizeY+1,sizeZ+1))
            }
        }

        return compressData
    }

}