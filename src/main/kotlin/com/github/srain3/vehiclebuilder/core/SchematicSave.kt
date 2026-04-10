package com.github.srain3.vehiclebuilder.core

import com.github.srain3.vehiclebuilder.util.Tools
import com.sk89q.worldedit.EmptyClipboardException
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.util.formatting.text.TextComponent
import org.bukkit.entity.Player
import java.io.File
import java.io.FileOutputStream

object SchematicSave {
    /**
     * schematicで保存する
     */
    fun saveFile(sender: Player, file: File) {
        val we = Tools.getWorldEditInstance() ?: return
        val actor = BukkitAdapter.adapt(sender)
        val localSession = we.sessionManager.get(actor)
        val holder = try {
            localSession.clipboard
        } catch (ex: EmptyClipboardException) {
            actor.printError(TextComponent.of("Your clipboard is empty."))
            return
        }
        sender.sendMessage("${holder.clipboard.region.width}x${holder.clipboard.region.height}x${holder.clipboard.region.length}")
        file.parentFile?.mkdirs()

        // 特定のバージョンを指定して取得
        val format = ClipboardFormats.findByAlias("sponge.3")
            ?: BuiltInClipboardFormat.SPONGE_SCHEMATIC // 見つからない時のフォールバック
        FileOutputStream(file).use { os ->
            format.getWriter(os).use { writer ->
                writer.write(holder.clipboard)
            }
        }
    }
}