package com.github.srain3.vehiclebuilder.core

import com.github.srain3.vehiclebuilder.util.Tools
import com.sk89q.worldedit.EmptyClipboardException
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.util.formatting.text.TextComponent
import org.bukkit.configuration.file.YamlConfiguration
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
        val holder: ClipboardHolder
        try {
            holder = localSession.clipboard
        } catch (ex: EmptyClipboardException) {
            actor.printError(TextComponent.of("Your clipboard is empty."))
            return
        }
        sender.sendMessage("${holder.clipboard.region.width}x${holder.clipboard.region.height}x${holder.clipboard.region.length}")

        if (!file.exists()) {
            YamlConfiguration.loadConfiguration(file).save(file)
        }
        BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(FileOutputStream(file)).use { writer ->
            writer.write(
                holder.clipboard
            )
        }
    }
}