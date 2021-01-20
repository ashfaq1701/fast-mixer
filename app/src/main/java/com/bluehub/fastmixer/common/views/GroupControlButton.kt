package com.bluehub.fastmixer.common.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.bluehub.fastmixer.databinding.GroupControlButtonBinding


@BindingMethods(
    value = [
        BindingMethod(
            type = GroupControlButton::class,
            attribute = "btnDrawable",
            method = "setBtnDrawable"
        ),
        BindingMethod(
            type = GroupControlButton::class,
            attribute = "btnLabel",
            method = "setBtnLabel"
        ),
        BindingMethod(
            type = GroupControlButton::class,
            attribute = "clickListener",
            method = "setClickListener"
        )
    ]
)
class GroupControlButton(context: Context, attributeSet: AttributeSet?)
    : ConstraintLayout(context, attributeSet) {

    private val binding: GroupControlButtonBinding

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = GroupControlButtonBinding.inflate(inflater, this, true)
    }

    fun setBtnDrawable(drawable: Drawable?) {
        drawable?.let { binding.imgButton.setImageDrawable(it) }
    }

    fun setBtnLabel(str: String) {
        binding.imgTxt.text = str
    }

    fun setBtnEnabled(isEnabled: Boolean) {
        binding.imgButton.isEnabled = isEnabled
    }

    fun setClickListener(clickListener: () -> Unit) {
        binding.imgButton.setOnClickListener {
            clickListener()
        }
    }
}
