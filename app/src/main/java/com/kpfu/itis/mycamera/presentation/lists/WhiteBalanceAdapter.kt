package com.kpfu.itis.mycamera.presentation.lists

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kpfu.itis.mycamera.data.models.categories.WhiteBalance

class WhiteBalanceAdapter(
    private val clickLambda: (Int) -> Unit
) : ListAdapter<WhiteBalance, WhiteBalanceItemHolder>(object :
    DiffUtil.ItemCallback<WhiteBalance>() {
    override fun areItemsTheSame(oldItem: WhiteBalance, newItem: WhiteBalance): Boolean =
        oldItem.title == newItem.title

    override fun areContentsTheSame(oldItem: WhiteBalance, newItem: WhiteBalance): Boolean =
        oldItem.value == newItem.value
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhiteBalanceItemHolder =
        WhiteBalanceItemHolder.create(
            parent,
            clickLambda
        )

    override fun onBindViewHolder(holder: WhiteBalanceItemHolder, position: Int) {
        holder.bind(getItem(position))
    }
}