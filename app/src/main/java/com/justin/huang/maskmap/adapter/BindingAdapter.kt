package com.justin.huang.maskmap.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.justin.huang.maskmap.R

@BindingAdapter("maskAmountBackground")
fun bindMaskAmountBackground(view: View, amount: Int) {
    val color = ContextCompat.getColor(
        view.context, when (amount) {
            0 -> R.color.color_mask_empty
            in 1 until 20 -> R.color.color_mask_few
            in 20 until 200 -> R.color.color_mask_less
            else -> R.color.color_mask_many
        }
    )
    view.setBackgroundColor(color)
}