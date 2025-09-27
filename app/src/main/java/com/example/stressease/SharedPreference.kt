package com.example.stressease

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray

object SharedPreference {
    fun saveList(context: Context, key: String, list: List<String>) {
        val prefs = context.getSharedPreferences("StressEasePrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = JSONArray(list).toString()
        editor.putString(key, json)
        editor.apply()
    }
    fun loadList(context: Context, key: String): MutableList<ChatMessage> {
        val prefs = context.getSharedPreferences("StressEasePrefs", Context.MODE_PRIVATE)
        val json = prefs.getString(key, null)?:return mutableListOf()
        val type = object : TypeToken<MutableList<ChatMessage>>() {}.type
        return Gson().fromJson(json, type)
    }
    fun saveChatList(context: Context, key: String, list: List<ChatMessage>) {
        val prefs = context.getSharedPreferences("StressEasePrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(list)
        editor.putString(key, json)
        editor.apply()
    }
}