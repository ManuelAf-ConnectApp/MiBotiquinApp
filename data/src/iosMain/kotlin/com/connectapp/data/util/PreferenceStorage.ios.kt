package com.connectapp.data.util

import com.connectapp.domain.manager.PreferenceStorage
import platform.Foundation.NSUserDefaults

class IOSPreferenceStorage : PreferenceStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? = userDefaults.stringForKey(key)

    override fun putString(key: String, value: String?) {
        userDefaults.setObject(value, forKey = key)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (userDefaults.objectForKey(key) != null) {
            userDefaults.boolForKey(key)
        } else {
            defaultValue
        }
    }

    override fun putBoolean(key: String, value: Boolean) {
        userDefaults.setBool(value, forKey = key)
    }

    override fun remove(key: String) {
        userDefaults.removeObjectForKey(key)
    }
}

actual fun createPreferenceStorage(context: Any?): PreferenceStorage {
    return IOSPreferenceStorage()
}
