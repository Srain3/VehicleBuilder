package com.github.srain3.vehiclebuilder.util

import com.github.srain3.vehiclebuilder.VehicleBuilder
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

object Tools {
  /**
   * 自PLUGINを利用するときに呼びやすい変数
   */
  val plugin: JavaPlugin by lazy { JavaPlugin.getPlugin(VehicleBuilder::class.java) }

  /**
   * チャット表示用に&カラーを使用できるように変換する
   */
  fun String.color(char: Char = '&'): String {
    return ChatColor.translateAlternateColorCodes(char, this)
  }

  /**
   * CommandSender達(コンソールやプレイヤーなど)に&カラーメッセージを送る
   */
  fun CommandSender.sendColorMessage(msg: String) {
    this.sendMessage(msg.color())
  }

  /**
   * Loggerでメッセージを出す
   */
  fun sendPluginLogger(msgList: MutableList<String>) {
    msgList.forEach {
      plugin.logger.info(it)
    }
  }

  /**
   * Loggerでメッセージを出す
   */
  fun sendPluginLogger(msgList: List<String>) {
    msgList.forEach {
      plugin.logger.info(it)
    }
  }

  /**
   * アイテムに表示名と説明を書き込む(nullで消す)自動で&カラー文にします
   */
  fun ItemStack.textEdit(
    displayName: String?,
    lore: List<String>?
  ): ItemStack {
    if (!this.hasItemMeta()) return this
    val meta = this.itemMeta?:return this
    meta.setDisplayName(displayName?.color())
    meta.lore = lore?.map { it.color() }
    this.itemMeta = meta
    return this
  }

  /**
   * アイテムの属性やエンチャントなどの詳細情報を非表示に一括設定する
   */
  fun ItemStack.allHide(): ItemStack {
    if (!this.hasItemMeta()) return this
    val meta = this.itemMeta?:return this
    ItemFlag.entries.forEach {
      if (!meta.hasItemFlag(it)) {
        meta.addItemFlags(it)
      }
    }
    this.itemMeta = meta
    return this
  }
}