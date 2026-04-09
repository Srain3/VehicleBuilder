package com.github.srain3.vehiclebuilder.util

import com.github.srain3.vehiclebuilder.VehicleBuilder
import com.sk89q.worldedit.WorldEdit
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

object Tools {
  /**
   * 自PLUGINを利用するときに呼びやすい変数
   */
  val plugin: JavaPlugin by lazy { JavaPlugin.getPlugin(VehicleBuilder::class.java) }

  // MiniMessageのインスタンス（タグ形式: <red>text</red> 用）
  private val mm = MiniMessage.miniMessage()

  /**
   * 文字列をComponentに変換する (MiniMessage優先) https://webui.advntr.dev/
   */
  fun String.toComponent(): Component {
    return mm.deserialize(this)
  }

  /**
   * CommandSender達(コンソールやプレイヤーなど)に&カラーメッセージを送る
   */
  fun CommandSender.sendColorMessage(msg: String) {
    this.sendMessage(msg.toComponent())
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
    meta.displayName(displayName?.toComponent())
    meta.lore(lore?.map { it.toComponent() })
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

  /**
   * WorldEditのインスタンスを取得、出来ない場合はログを吐く
   */
  fun getWorldEditInstance(): WorldEdit? {
    val we = WorldEdit.getInstance()
    if (we == null) {
      plugin.logger.warning("WorldEdit cannot be loaded.")
    }
    return we
  }
}