package com.kpfu.itis.mycamera.presentation.lists

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kpfu.itis.mycamera.data.models.Preset

class PresetsAdapter(
    private val clickLambda: (Preset) -> Unit
) : ListAdapter<Preset, PresetsItemHolder>(object : DiffUtil.ItemCallback<Preset>() {
    override fun areItemsTheSame(oldItem: Preset, newItem: Preset): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Preset, newItem: Preset): Boolean {
        return oldItem.whiteBalance == newItem.whiteBalance
    }

}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetsItemHolder =
        PresetsItemHolder.create(
            parent,
            clickLambda
        )


    override fun onBindViewHolder(holder: PresetsItemHolder, position: Int) {
        holder.bind(getItem(position))
    }

}