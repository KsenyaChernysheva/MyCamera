package com.kpfu.itis.mycamera.presentation.lists

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class ExposureAdapter(
    private val clickLambda: (Int) -> Unit
) : ListAdapter<Int, ExposureItemHolder>(object : DiffUtil.ItemCallback<Int>() {
    override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean =
        oldItem.hashCode() == newItem.hashCode()

    override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean =
        oldItem == newItem
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExposureItemHolder =
        ExposureItemHolder.create(
            parent,
            clickLambda
        )

    override fun onBindViewHolder(holder: ExposureItemHolder, position: Int) {
        holder.bind(getItem(position))
    }

}