package com.kpfu.itis.mycamera.presentation.lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kpfu.itis.mycamera.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_param.*

class ParametersItemHolder(
    override val containerView: View,
    private val clickLambda: (Int) -> Unit
) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(image: Int) {
        img_param.setImageResource(image)
        img_param.setOnClickListener {
            clickLambda(image)
        }
    }

    companion object {
        fun create(parent: ViewGroup, clickLambda: (Int) -> Unit) =
            ParametersItemHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_param, parent, false),
                clickLambda
            )
    }
}