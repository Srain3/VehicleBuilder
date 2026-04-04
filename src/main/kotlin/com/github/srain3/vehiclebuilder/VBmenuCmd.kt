package com.github.srain3.vehiclebuilder

import com.github.srain3.vehiclebuilder.util.Tools.color
import com.github.srain3.vehiclebuilder.util.Tools.sendColorMessage
import com.github.srain3.vehiclebuilder.util.Tools.textEdit
import com.github.srain3.vehiclebuilder.util.invgui.GuiInventory
import com.github.srain3.vehiclebuilder.util.invgui.GuiInventory.clickSound
import com.github.srain3.vehiclebuilder.util.invgui.GuiInventory.setItem
import com.github.srain3.vehiclebuilder.util.invgui.GuiItem
import com.github.srain3.vehiclebuilder.util.invgui.GuiItem.guiClickEvent
import com.github.srain3.vehiclebuilder.util.invgui.GuiPagePanel
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object VBmenuCmd: CommandExecutor {
    private const val msgHeader = "&7[VehicleBuilder]&r"

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): Boolean {
        if (command.name != "vbmenu") return false
        sender.sendColorMessage("$msgHeader Debug Message!")
        val pageItem = mutableListOf<ItemStack?>()
        for (i in 0..127) {
            /*
            if (i % 3 == 0) {
                pageItem.add(null)
                continue
            }
             */
            var material = Material.entries.random()
            while (!material.isItem) {
                material = Material.entries.random()
            }
            pageItem.add(
                ItemStack(material, 1)
                    .guiClickEvent { clickEvent ->
                        clickEvent.whoClicked.sendColorMessage("$msgHeader Debug: click page item $i")
                        clickEvent.clickSound(Sound.BLOCK_WOOD_HIT)
                    }
                    .textEdit("Random $i", listOf("Debug Item"))
            )
        }
        val inv = GuiInventory.createInventory(6, "&aTEST &7GUI".color(), lock = false)
        inv.setItem(4,5,
            ItemStack(Material.BARRIER)
                .guiClickEvent { clickEvent ->
                clickEvent.whoClicked.sendColorMessage("$msgHeader Debug: close item")
                clickEvent.clickSound()
                }
                .textEdit("&cClose", listOf())
        )
        val guiPage = GuiItem.pageItem(9,5,pageItem)
        val page = GuiPagePanel(0,0,9,5,guiPage)
        page.reflash(inv)
        inv.setItem(8,5, page.nextPageItem())
        inv.setItem(0,5, page.backPageItem())
        if (sender is Player) {
            sender.openInventory(inv)
        }
        return true
    }
}