/*
 * Copyright (c) 2026 Srain
 * Licensed under the GNU GPL v3.0
 */
package com.github.srain3.vehiclebuilder

import org.bukkit.plugin.java.JavaPlugin

class VehicleBuilder: JavaPlugin() {
  override fun onEnable() {
    server.getPluginCommand("vbmenu")?.setExecutor(VBmenuCmd)
  }

  override fun onDisable() {

  }
}