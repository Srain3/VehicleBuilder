package com.github.srain3.vehiclebuilder.util

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.logging.Level

open class CustomYaml(
    private val fileName: String,
    private val fromJar: Boolean = false,
): YamlConfiguration() {
    private val plugin = Tools.plugin
    private val file = File(plugin.dataFolder, fileName)

    init {
        reload()
    }

    fun saveDefault() {
        val df = plugin.dataFolder
        if (!df.exists() || !df.isDirectory) {
            df.mkdir()
        }
        if (!file.exists()) {
            if (fromJar) {
                plugin.saveResource(fileName, true)
            } else {
                save()
            }
        }
    }

    fun save() {
        try {
            this.save(file)
        } catch (ex: IOException) {
            plugin.logger.log(Level.SEVERE, "Could not save config", ex)
        }
    }

    fun reload() {
        if (file.exists()) {
            this.load(file)
        }
    }

    fun delete() {
        if (file.exists()) {//存在するファイルの場合
            file.delete()
        }
    }
}