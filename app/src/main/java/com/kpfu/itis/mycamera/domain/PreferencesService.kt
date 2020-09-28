package com.kpfu.itis.mycamera.domain

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.kpfu.itis.mycamera.R
import com.kpfu.itis.mycamera.data.models.Preset

class PreferencesService {

    fun initPreference(context: Context) {
        val key = context.getString(R.string.saved_presets_preferences)
        val sharedPref = context.getSharedPreferences(key, Context.MODE_PRIVATE)
        sharedPref?.getString(
            key,
            context.getString(R.string.pref_default)
        )
    }

    fun createNewEntry(name: String, value: String) = "/!$name/?$value"

    fun isNameCorrect(name: String) =
        (!name.contains("/!") && !name.contains("/?") && !name.contains("/#"))

    fun createParametersSet(values: IntArray): String {
        var parametersSet = ""
        for (i in values) {
            parametersSet += "$i/#"
        }
        parametersSet = parametersSet.substring(0, parametersSet.length - 2)
        return parametersSet
    }

    fun parseFromPreference(preference: String): List<Preset> {
        val unparsedPresets = preference.split("/!")
        val result = listOf<Preset>().toMutableList()
        Log.d("Size of presets list", unparsedPresets.size.toString())
        for (i in 1 until unparsedPresets.size) {
            val serializedPreset = unparsedPresets[i].split("/?")
            val name = serializedPreset[0]
            val params = serializedPreset[1].split("/#")
            result.add(
                Preset(
                    name,
                    params[0].toInt(),
                    params[1].toInt(),
                    params[2].toInt(),
                    params[3].toInt(),
                    params[4].toInt()
                )
            )
        }
        return result
    }

    fun updatePresetsPreference(pref: SharedPreferences?, key: String, newList: List<Preset>) {
        val presetString = createNewPreference(newList)
        with(pref?.edit()) {
            this?.putString(key, presetString)
            this?.commit()
        }
    }

    private fun createNewPreference(list: List<Preset>): String {
        var result = ""
        if (list.isNotEmpty()) {
            for (i in list) {
                result += createNewEntry(
                    i.name,
                    createParametersSet(
                        arrayOf(
                            i.controlMode,
                            i.whiteBalance,
                            i.ISO,
                            i.effect,
                            i.exposure
                        ).toIntArray()
                    )
                )
            }
            return result.substring(1)
        }
        return result
    }

}