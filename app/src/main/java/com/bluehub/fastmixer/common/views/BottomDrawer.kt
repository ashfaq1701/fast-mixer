package com.bluehub.fastmixer.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.databinding.BottomDrawerBinding
import io.reactivex.rxjava3.subjects.BehaviorSubject


class BottomDrawer(context: Context, attributeSet: AttributeSet?)
    : ConstraintLayout(context, attributeSet) {

    private var binding: BottomDrawerBinding
    private var mContainerView: LinearLayout

    private val drawerOpen: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private lateinit var transition: Transition

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = BottomDrawerBinding.inflate(inflater, this, true)
        mContainerView = binding.drawerContainer

        setupObservers()
        setupViewEvents()
        setupAnimations()
    }

    private fun setupObservers() {
        drawerOpen.subscribe {
            if (it) {
                TransitionManager.beginDelayedTransition(binding.mainContainer, transition)
                binding.drawerContainer.visibility = View.VISIBLE
                binding.drawerControl.setImageResource(R.drawable.drawer_control_button_close)
            } else {
                TransitionManager.beginDelayedTransition(binding.mainContainer, transition)
                binding.drawerContainer.visibility = View.GONE
                binding.drawerControl.setImageResource(R.drawable.drawer_control_button_open)
            }
        }
    }

    private fun setupViewEvents() {
        binding.drawerControl.setOnClickListener {
            if (!drawerOpen.hasValue() || drawerOpen.value == false) {
                drawerOpen.onNext(true)
            } else {
                drawerOpen.onNext(false)
            }
        }
    }

    private fun setupAnimations() {
        transition = Slide()
        transition.duration = 400
        transition.addTarget(R.id.drawerContainer)
    }

    override fun addView(
        child: View?,
        index: Int,
        params: ViewGroup.LayoutParams?
    ) {
        child?.let { childView ->
            if (!listOf(R.id.mainContainer, R.id.drawerControl, R.id.drawerContainer).contains(
                    childView.id
                )) {
                return mContainerView.addView(childView, index, params)
            }
        }

        return super.addView(child, index, params)
    }
}