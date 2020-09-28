package com.kpfu.itis.mycamera.presentation.lists

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kpfu.itis.mycamera.data.models.categories.Effect

class EffectsAdapter(
    private val clickLambda: (Int) -> Unit
) : ListAdapter<Effect, EffectsItemHolder>(object : DiffUtil.ItemCallback<Effect>() {
    override fun areItemsTheSame(oldItem: Effect, newItem: Effect): Boolean =
        oldItem.title == newItem.title

    override fun areContentsTheSame(oldItem: Effect, newItem: Effect): Boolean =
        oldItem.value == newItem.value
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EffectsItemHolder =
        EffectsItemHolder.create(
            parent,
            clickLambda
        )

    override fun onBindViewHolder(holder: EffectsItemHolder, position: Int) {
        holder.bind(getItem(position))
    }

}