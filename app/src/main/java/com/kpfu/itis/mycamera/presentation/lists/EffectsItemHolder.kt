package com.kpfu.itis.mycamera.presentation.lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kpfu.itis.mycamera.R
import com.kpfu.itis.mycamera.data.models.categories.Effect
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_params_settings.*

class EffectsItemHolder(
    override val containerView: View,
    private val clickLambda: (Int) -> Unit
) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(item: Effect) {
        tv_title.text = item.title
        tv_title.setOnClickListener {
            clickLambda(item.value)
        }
    }

    companion object {
        fun create(parent: ViewGroup, clickLambda: (Int) -> Unit) =
            EffectsItemHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_params_settings, parent, false),
                clickLambda
            )
    }
}