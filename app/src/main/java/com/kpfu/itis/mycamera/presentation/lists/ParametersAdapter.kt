package com.kpfu.itis.mycamera.presentation.lists

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class ParametersAdapter(
    private val clickLambda: (Int) -> Unit
) : ListAdapter<Int, ParametersItemHolder>(object : DiffUtil.ItemCallback<Int>() {
    override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean =
        oldItem.hashCode() == newItem.hashCode()
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParametersItemHolder =
        ParametersItemHolder.create(
            parent,
            clickLambda
        )

    override fun onBindViewHolder(holder: ParametersItemHolder, position: Int) {
        holder.bind(getItem(position))
    }

}