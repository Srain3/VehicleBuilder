package com.github.srain3.vehiclebuilder

import com.github.srain3.vehiclebuilder.core.SchematicSave
import com.github.srain3.vehiclebuilder.core.SchematicToData
import com.github.srain3.vehiclebuilder.core.base.EntitySpawnBase
import com.github.srain3.vehiclebuilder.util.Tools
import com.github.srain3.vehiclebuilder.util.Tools.sendColorMessage
import org.bukkit.block.data.BlockData
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.io.File

object TestUnit: EntitySpawnBase {
    private val body: MutableMap<Vector, Pair<BlockData, Vector>> = mutableMapOf()

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
            if (args.size >= 2) {
                if (args[1] != "force") {
                    // 上書きする
                    SchematicSave.saveFile(sender, file)
                }
            }
        } else {
            SchematicSave.saveFile(sender, file)
        }
        schemTest(sender, file)
    }

    private fun schemTest(
        sender: Player,
        file: File
    ) {
        Thread {
            val raw = SchematicToData.fileToRawBlockData(file)
            if (raw.isNullOrEmpty()) return@Thread

            val size = SchematicToData.size(raw)
            body.plusAssign(SchematicToData.compressBlockData(raw, size))

            object : BukkitRunnable() {
                override fun run() {
                    sender.sendColorMessage("&7[VehicleBuilder]&rTestUnit: Schem save and load OK.")
                }
            }.runTaskLater(Tools.plugin, 1)
            Thread.sleep(500L)

            object : BukkitRunnable() {
                override fun run() {
                    spawnBlockDisplay(sender.location, mutableListOf(), body, 2.4, size.first)
                    sender.sendColorMessage("&7[VehicleBuilder]&rTestUnit: Spawn DisplayEntity OK.")
                }
            }.runTaskLater(Tools.plugin, 1)

            return@Thread
        }.start()
        return
    }
}