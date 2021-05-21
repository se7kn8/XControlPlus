package com.github.se7_kn8.xcontrolplus.app.settings

import com.github.se7_kn8.xcontrolplus.app.util.FileUtil
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class SettingsEntry<T>(val saveName: String, val defaultValue: T)

abstract class Settings(fileName: String, vararg entries: SettingsEntry<*>) {
    companion object {
        private val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()

        private val type = object : TypeToken<HashMap<String, Any>>() {}.type!!

        private val settingsPath = FileUtil.getSpecificPath("settings")
    }

    val settings = HashMap<String, Any>()
    private var hasChanged = false

    private val filePath = settingsPath.resolve("${fileName}.json")

    init {
        load()

        // If settings are missing add them to the config file with default values
        var shouldSave = false
        entries.forEach {
            if (settings[it.saveName] == null) {
                shouldSave = true
                settings[it.saveName] = it.defaultValue as Any
            }
        }
        if (shouldSave) {
            save(true)
        }
    }

    fun load() {
        val newData = loadFileIntoMap()
        settings.clear()
        settings.putAll(newData)
    }

    fun save(forceSave: Boolean = false) {
        if (hasChanged || forceSave) {
            saveMapIntoFile()
        }
    }

    private fun loadFileIntoMap(): HashMap<String, Any> {
        FileUtil.readFileToString(filePath)?.also {
            return gson.fromJson(it, type)
        }
        return HashMap()
    }

    private fun saveMapIntoFile() {
        val json = gson.toJson(settings, type)
        FileUtil.writeStringToFile(filePath, json)
    }

    inline operator fun <reified T> get(entry: SettingsEntry<T>): T {
        val value = settings.getOrPut(entry.saveName) { entry.defaultValue as Any }
        if (value is T) {
            return value
        }
        return entry.defaultValue
    }


    operator fun <T> set(entry: SettingsEntry<T>, value: T) {
        settings[entry.saveName] = value as Any
        hasChanged = true
    }

}

// Application settings will be not visible to the user
class ApplicationSettings : Settings(
    "application",
    LATEST_OPEN_PATH,
    LATEST_SAVE_PATH,
    START_MAXIMIZED,
    WINDOW_X,
    WINDOW_Y,
    WINDOW_WIDTH,
    WINDOW_HEIGHT
) {
    companion object {
        val LATEST_OPEN_PATH = SettingsEntry("latestOpenPath", "")
        val LATEST_SAVE_PATH = SettingsEntry("latestSavePath", "")
        val START_MAXIMIZED = SettingsEntry("startMaximized", false)
        val WINDOW_X = SettingsEntry("windowX", 0.0)
        val WINDOW_Y = SettingsEntry("windowY", 0.0)
        val WINDOW_WIDTH = SettingsEntry("windowWidth", 0.0)
        val WINDOW_HEIGHT = SettingsEntry("windowHeight", 0.0)

        // Maybe remove this and switch to DI
        val INSTANCE by lazy { ApplicationSettings() }
    }
}

// User settings are displayed in the settings menu and can be edited by the user
class UserSettings : Settings(
    "user",
    ASK_BEFORE_EXIT
) {
    companion object {
        val ASK_BEFORE_EXIT = SettingsEntry("askBeforeExit", true)

        // Maybe remove this and switch to DI
        val INSTANCE by lazy { UserSettings() }
    }
}