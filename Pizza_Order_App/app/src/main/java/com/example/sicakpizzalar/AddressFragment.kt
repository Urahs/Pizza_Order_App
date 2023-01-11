package com.example.sicakpizzalar

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import com.example.sicakpizzalar.databinding.FragmentAddressBinding
import com.example.sicakpizzalar.databinding.FragmentInitialBinding

class AddressFragment : Fragment() {

    private var _binding: FragmentAddressBinding? = null
    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val minAddressLength = 5

        val addressTextField = binding.addressText
        binding.orderButton.isEnabled = false
        addressTextField.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(currentText: Editable?) {
                binding.orderButton.isEnabled = currentText.toString() != "" && currentText.toString().length >= minAddressLength
            }
        })

        binding.orderButton.setOnClickListener {
            Toast.makeText(activity, getString(R.string.order_approved), Toast.LENGTH_SHORT).show()
            orderViewModel.cancelOrder()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val TAG = "AddressFragment"
    }
}