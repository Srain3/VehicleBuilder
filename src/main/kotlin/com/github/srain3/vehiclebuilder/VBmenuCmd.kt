package com.github.srain3.vehiclebuilder

import com.github.srain3.vehiclebuilder.util.Tools.sendColorMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object VBmenuCmd: CommandExecutor {
    private const val HEADER = "<gray>[VehicleBuilder]</gray>"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (command.name != "vbmenu") return false
        sender.sendColorMessage("$HEADER Debug Message!")
        return true
    }
}