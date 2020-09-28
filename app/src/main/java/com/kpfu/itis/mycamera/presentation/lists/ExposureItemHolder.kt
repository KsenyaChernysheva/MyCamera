package com.kpfu.itis.mycamera.presentation.lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kpfu.itis.mycamera.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_params_settings.*

class ExposureItemHolder(
    override val containerView: View,
    private val clickLambda: (Int) -> Unit
) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(item: Int) {
        if (item > 0) {
            val countWithPlus = "+$item"
            tv_title.text = countWithPlus
        } else {
            tv_title.text = item.toString()
        }
        tv_title.setOnClickListener {
            clickLambda(item)
        }
    }

    companion object {
        fun create(parent: ViewGroup, clickLambda: (Int) -> Unit) =
            ExposureItemHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_params_settings, parent, false),
                clickLambda
            )
    }
}