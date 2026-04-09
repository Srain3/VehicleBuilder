package com.github.srain3.vehiclebuilder

import com.github.srain3.vehiclebuilder.core.SchematicToData
import com.github.srain3.vehiclebuilder.core.base.EntitySpawnBase
import com.github.srain3.vehiclebuilder.util.Tools
import com.github.srain3.vehiclebuilder.util.Tools.sendColorMessage
import org.bukkit.block.data.BlockData
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.io.File
import kotlin.time.measureTime

object TestUnit: EntitySpawnBase {
    private val body: MutableMap<Vector, Pair<BlockData, Vector>> = mutableMapOf()
    private val wheel: MutableMap<Vector, Pair<BlockData, Vector>> = mutableMapOf()
    private val headlight: MutableMap<Vector, Pair<BlockData, Vector>> = mutableMapOf()
    private const val HEADER = "<gray>[VehicleBuilder]</gray>"

    fun testCmd(
        sender: CommandSender,
        args: Array<out String>
    ) {
        if (sender !is Player) return
        if (args.isEmpty()) return
        val fileName = args[0]
        if (fileName.isBlank()) return
        val file = File(Tools.plugin.dataFolder, "$fileName.schem")
        if (file.exists()) {
            // ファイルがある場合
            val wheelFile = File(Tools.plugin.dataFolder, "${fileName}_wheel.schem")
            val headlightFile = File(Tools.plugin.dataFolder, "${fileName}_headlight.schem")
            schemTest(sender, file, wheelFile, headlightFile)
        } else {
            //SchematicSave.saveFile(sender, file)
            sender.sendColorMessage("$HEADER TestUnit:<red>The file does not exist.</red>")
            return
        }

    }

    private fun schemTest(
        sender: Player,
        file: File,
        wheelFile: File,
        headlightFile: File
    ) {
        Thread {
            val size : Pair<BoundingBox, Pair<Vector, Vector>>
            val duration = measureTime {
                // 計測したい処理
                val raw = SchematicToData.fileToRawBlockData(file)
                if (raw.isNullOrEmpty()) return@Thread

                size = SchematicToData.size(raw)
                body.plusAssign(SchematicToData.compressBlockData(raw, size))

                if (wheelFile.exists()) {
                    val wRaw = SchematicToData.fileToRawBlockData(wheelFile)
                    if (!wRaw.isNullOrEmpty()) {
                        val wSize = SchematicToData.size(wRaw)
                        wheel.plusAssign(SchematicToData.compressBlockData(wRaw, wSize))
                    }
                }
                if (headlightFile.exists()) {
                    val hRaw = SchematicToData.fileToRawBlockData(headlightFile)
                    if (!hRaw.isNullOrEmpty()) {
                        val hSize = SchematicToData.size(hRaw)
                        headlight.plusAssign(SchematicToData.compressBlockData(hRaw, hSize))
                    }
                }
            }

            object : BukkitRunnable() {
                override fun run() {
                    sender.sendColorMessage("$HEADER TestUnit: compress = $duration")
                    spawnBlockDisplay(sender.location, mutableListOf(), body, 2.4, size.first)
                    sender.sendColorMessage("$HEADER TestUnit: Spawn DisplayEntity OK.")
                }
            }.runTaskLater(Tools.plugin, 1)

            return@Thread
        }.start()
        return
    }
}