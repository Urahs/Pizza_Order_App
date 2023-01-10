package com.example.sicakpizzalar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import com.example.sicakpizzalar.databinding.FragmentInitialBinding

class InitialFragment : Fragment() {

    private var _binding: FragmentInitialBinding? = null
    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInitialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startButton.setOnClickListener {
            onStartButtonTapped()
        }

        binding.goCartButton.setOnClickListener {
            onGoCartButton()
        }

        orderViewModel.isProgressAllowed.observe(viewLifecycleOwner) { allowed ->
            binding.startButton.isEnabled = allowed
        }

        orderViewModel.isOrderAllowed.observe(viewLifecycleOwner) { allowed ->
            binding.goCartButton.isEnabled = allowed
        }
    }

    private fun onGoCartButton() {
        orderViewModel.openCartScreen()
    }

    private fun onStartButtonTapped() {
        orderViewModel.progress()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val TAG = "InitialFragment"
    }
}