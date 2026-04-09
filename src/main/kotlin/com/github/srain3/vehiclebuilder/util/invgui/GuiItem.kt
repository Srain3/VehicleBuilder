package com.github.srain3.vehiclebuilder.util.invgui

import com.github.srain3.vehiclebuilder.util.Tools
import org.bukkit.NamespacedKey
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random

object GuiItem {
    //GuiItemのデータのKey用
    private val clickKey : NamespacedKey by lazy {
        NamespacedKey(Tools.plugin, "gui_click")
    }
    //GuiItemID別のUnit保存用
    private val cacheClick = mutableMapOf<Long, Pair<Boolean, (InventoryClickEvent) -> Unit>>()

    /**
     * Gui用のクリック処理を登録
     * @param cansel クリックイベントをキャンセル扱いするか
     * @param run ここに処理したい内容
     */
    fun ItemStack.guiClickEvent(
        cansel: Boolean = true,
        run: (InventoryClickEvent) -> Unit = {}
    ): ItemStack {
        val meta = this.itemMeta
        val idLong = Random.nextLong()
        meta?.persistentDataContainer?.set(clickKey, PersistentDataType.LONG, idLong)
        this.itemMeta = meta
        cacheClick[idLong] = Pair(cansel, run)
        return this
    }

    /**
     * ClickEventから操作アイテムを取得してrunが存在すれば実行する
     * @return 実行成功時trueを返す
     */
    fun itemToRun(e: InventoryClickEvent): Boolean {
        val clickItem = e.currentItem ?: return false
        val idLong = clickItem.itemMeta
            ?.persistentDataContainer?.get(clickKey, PersistentDataType.LONG) ?: return false
        cacheClick[idLong]?.run {
            if (this.first) e.isCancelled = true
            this.second(e)
        }
        return true
    }

    /**
     * PagePanel用のアイテムリスト作成補助
     * @param x ページ表示に使う横のスロット数(1-9)
     * @param y ページ表示に使う縦のスロット数(1-6)
     */
    fun pageItem(x: Int, y: Int, itemList: MutableList<ItemStack?>): MutableMap<Int, MutableList<ItemStack?>> {
        val pageMaxIndex = x * y - 1
        val pageIndex = itemList.size / pageMaxIndex
        if (pageIndex == 0) return mutableMapOf()

        val map = mutableMapOf<Int, MutableList<ItemStack?>>()
        val iterator = itemList.iterator()
        for (page in 0..pageIndex) {
            val list = mutableListOf<ItemStack?>()
            repeat(pageMaxIndex + 1) {
                if (!iterator.hasNext()) {
                    list.add(null)
                } else {
                    list.add(iterator.next())
                }
            }
            map[page] = list
        }
        return map
    }
}