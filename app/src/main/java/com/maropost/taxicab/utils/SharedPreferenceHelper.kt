package com.maropost.taxicab.utils

import android.content.Context

class SharedPreferenceHelper {
    val PREF_FILE: String = "PREF"

    companion object {
        private var instance: SharedPreferenceHelper? = null
        fun getInstance() : SharedPreferenceHelper {
            if (instance == null)
                instance = SharedPreferenceHelper()
            return instance as SharedPreferenceHelper
        }
    }

    enum class PreferenceDataType (val stringValue: String) {
        INTEGER("INT"),
        LONG("LONG"),
        FLOAT("FLOAT"),
        STRING("STRING"),
        BOOLEAN("BOOLEAN");

        override fun toString(): String {
            return stringValue
        }
    }


    /**
     * Set data in shared preference
     * @param key   - Key to set shared preference
     * @param value - Value for the key
     */
    fun setSharedPreference(context: Context, key: String, value: Any) {
        val settings = context.getSharedPreferences(PREF_FILE, 0)
        val editor = settings.edit()
        when(value){
            is String   -> editor.putString(key,value)
            is Long     -> editor.putLong(key,value)
            is Int      -> editor.putInt(key,value)
            is Float    -> editor.putFloat(key,value)
            is Boolean  -> editor.putBoolean(key,value)
        }
        editor.apply()
    }

    /**
     * Get shared preference value from key
     * @param key      - Key to look up in shared preferences.
     * @param value - Default value to be returned if shared preference isn't found.
     * @return value - String containing value of the shared preference if found.
     */
    fun getSharedPreference(context: Context, key: String, preferenceType: PreferenceDataType): Any? {
        val settings = context.getSharedPreferences(PREF_FILE, 0)
        return when(preferenceType){
            PreferenceDataType.STRING -> settings.getString(key, "")
            PreferenceDataType.LONG -> settings.getLong(key, 0)
            PreferenceDataType.INTEGER -> settings.getInt(key, 0)
            PreferenceDataType.FLOAT -> settings.getFloat(key, 0.0f)
            PreferenceDataType.BOOLEAN -> settings.getBoolean(key, false)
        }
    }
}