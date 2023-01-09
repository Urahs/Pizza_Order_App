package com.example.sicakpizzalar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.sicakpizzalar.databinding.FragmentDoughTypeBinding
import com.example.sicakpizzalar.databinding.FragmentInitialBinding

class DoughTypeFragment : Fragment() {

    private var _binding: FragmentDoughTypeBinding? = null
    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDoughTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.setOnClickListener {
            onNextButtonTapped()
        }

        orderViewModel.isProgressAllowed.observe(viewLifecycleOwner) { allowed ->
            binding.nextButton.isEnabled = allowed
        }

        // Temporary
        var doughTypeNamesStr: String = ""
        orderViewModel.doughTypes.forEach { t ->
            doughTypeNamesStr += getString(t.getDoughTypeNameResourceID()) + "\n"
        }

        binding.textView.text = doughTypeNamesStr
    }

    private fun onNextButtonTapped() {
        orderViewModel.progress()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val TAG = "DoughTypeFragment"
    }
}