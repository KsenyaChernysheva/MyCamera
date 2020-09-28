package com.kpfu.itis.mycamera.presentation.lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kpfu.itis.mycamera.R
import com.kpfu.itis.mycamera.data.models.Preset
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_presets_item.*

class PresetsItemHolder(
    override val containerView: View,
    private val clickLambda: (Preset) -> Unit
) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(preset: Preset) {
        tv_preset_name.text = preset.name
        tv_preset_name.setOnClickListener {
            clickLambda(preset)
        }
    }

    companion object {
        fun create(parent: ViewGroup, clickLambda: (Preset) -> Unit) =
            PresetsItemHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.fragment_presets_item, parent, false),
                clickLambda
            )
    }
}