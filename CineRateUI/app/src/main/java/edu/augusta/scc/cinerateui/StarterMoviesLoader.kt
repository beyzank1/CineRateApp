package edu.augusta.scc.cinerateui

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.augusta.scc.cinerateui.api.MovieRecord

object StarterMoviesLoader {

    fun load(context: Context): List<MovieRecord> {
        return try {
            // 🔹 Read the JSON from assets/starter_movies.json
            val inputStream = context.assets.open("starter_movies.json")
            val json = inputStream.bufferedReader().use { it.readText() }

            println("DEBUG: Loaded starter_movies.json length = ${json.length}")

            val listType = object : TypeToken<List<MovieRecord>>() {}.type
            val list: List<MovieRecord> = Gson().fromJson(json, listType)

            println("DEBUG: Parsed starter movie count = ${list.size}")

            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
