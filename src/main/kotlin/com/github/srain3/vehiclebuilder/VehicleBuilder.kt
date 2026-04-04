/*
 * Copyright (c) 2026 Srain
 * Licensed under the GNU GPL v3.0
 */
package com.github.srain3.vehiclebuilder

import com.github.srain3.vehiclebuilder.util.invgui.GuiInventory
import org.bukkit.plugin.java.JavaPlugin

class VehicleBuilder: JavaPlugin() {
  override fun onEnable() {
    server.getPluginCommand("vbmenu")?.setExecutor(VBmenuCmd)

    server.pluginManager.registerEvents(GuiInventory, this)
  }

  override fun onDisable() {
    GuiInventory.disableTask()
  }
}