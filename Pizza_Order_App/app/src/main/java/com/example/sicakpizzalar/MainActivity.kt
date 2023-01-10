package com.example.sicakpizzalar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.sicakpizzalar.databinding.ActivityMainBinding
import com.example.sicakpizzalar.databinding.FragmentInitialBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackButton()
        }

        binding.cancelButton.setOnClickListener {
            onCancelButton()
        }

        orderViewModel.navigation.observe(this) { navigation ->
            processNavigationChange(navigation)
        }

        orderViewModel.isBackProgressAllowed.observe(this) { allowed ->
            binding.backButton.visibility = if (allowed) View.VISIBLE else View.GONE
        }

        orderViewModel.isCancelAllowed.observe(this) { allowed ->
            binding.cancelButton.visibility = if (allowed) View.VISIBLE else View.GONE
        }
    }

    private fun processNavigationChange(step: OrderStep) {
        val fragmentTag = orderStepToFragmentTag(step)
        val fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        if (fragment == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(binding.fragmentContainerView.id,
                    orderStepToFragmentInstance(step), fragmentTag)
            }
        }
    }

    private fun onBackButton() {
        orderViewModel.goToPreviousOrderStep()
    }

    private fun onCancelButton() {
        orderViewModel.cancelOrder()
    }

    private fun orderStepToFragmentTag(step: OrderStep): String {
        return when(step) {
            OrderStep.INITIAL -> InitialFragment.TAG
            OrderStep.PIZZA_TYPE -> TypeFragment.TAG
            OrderStep.DOUGH_TYPE -> DoughTypeFragment.TAG
            OrderStep.TOPPINGS -> ToppingsFragment.TAG
            OrderStep.SUMMARY -> SummaryFragment.TAG
            OrderStep.CART -> CartFragment.TAG
        }
    }

    private fun orderStepToFragmentInstance(step: OrderStep): Fragment {
        return when(step) {
            OrderStep.INITIAL -> InitialFragment()
            OrderStep.PIZZA_TYPE -> TypeFragment()
            OrderStep.DOUGH_TYPE -> DoughTypeFragment()
            OrderStep.TOPPINGS -> ToppingsFragment()
            OrderStep.SUMMARY -> SummaryFragment()
            OrderStep.CART -> CartFragment()
        }
    }

}