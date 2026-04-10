/*
 * Copyright (c) 2026 Srain
 * Licensed under the GNU GPL v3.0
 */
package com.github.srain3.vehiclebuilder

import com.github.retrooper.packetevents.PacketEvents
import com.github.srain3.vehiclebuilder.core.VehicleEntityList
import com.github.srain3.vehiclebuilder.core.VehicleInteractListener
import com.github.srain3.vehiclebuilder.util.invgui.GuiInventory
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import org.bukkit.plugin.java.JavaPlugin

class VehicleBuilder: JavaPlugin() {
  override fun onLoad() {
    // PacketEventsの初期化（onEnableより前に行うのが推奨）
    PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
    PacketEvents.getAPI().load()
  }

  override fun onEnable() {
    PacketEvents.getAPI().init()
    server.getPluginCommand("vbmenu")?.setExecutor(VBmenuCmd)

    server.pluginManager.registerEvents(GuiInventory, this)
    server.pluginManager.registerEvents(VehicleInteractListener, this)
  }

  override fun onDisable() {
    PacketEvents.getAPI().terminate()
    GuiInventory.disableTask()
    VehicleEntityList.allExit()
  }
}