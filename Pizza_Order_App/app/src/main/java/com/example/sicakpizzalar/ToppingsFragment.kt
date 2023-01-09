package com.example.sicakpizzalar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.sicakpizzalar.databinding.FragmentToppingsBinding

class ToppingsFragment : Fragment() {

    private var _binding: FragmentToppingsBinding? = null
    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToppingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.setOnClickListener {
            onNextButtonTapped()
        }


        // Temporary
        var toppingNamesStr: String = ""
        orderViewModel.toppingTypes.forEach { t ->
            toppingNamesStr += getString(t.getToppingsTypeNameResourceID()) + " (+ ${t.getPrice()} TL) \n"
        }

        binding.textView.text = toppingNamesStr
    }

    private fun onNextButtonTapped() {
        orderViewModel.progress()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val TAG = "ToppingsFragment"
    }
}