package com.example.sicakpizzalar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.transition.Visibility
import com.example.sicakpizzalar.databinding.FragmentInitialBinding
import com.example.sicakpizzalar.databinding.FragmentSummaryBinding

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.orderButton.setOnClickListener {
            onOrderButtonTapped()
        }

        orderViewModel.totalPrice.observe(viewLifecycleOwner) { totalPrice ->
            setPriceText(totalPrice)
        }


        binding.pizzaTypeTV.text = "${getString(orderViewModel.selectedPizzaType!!.getPizzaTypeNameResourceID())}"
        binding.doughTypeTV.text = "${getString(orderViewModel.selectedDoughType!!.getDoughTypeNameResourceID())}"

        var toppings = ""
        orderViewModel.selectedToppingTypes.forEach {
            toppings += "${getString(it.getToppingsTypeNameResourceID())}\n"
        }
        binding.toppingsTV.text = toppings

        binding.toppingsTitleTV.visibility = if (orderViewModel.isToppingSelected()) View.VISIBLE else View.GONE
        binding.toppingsTV.visibility = if (orderViewModel.isToppingSelected()) View.VISIBLE else View.GONE
    }

    private fun setPriceText(price: Int) {
        val priceStr = getString(R.string.format_price, price)
        val totalPrice = getString(R.string.format_total_price, priceStr)
        binding.priceTV.text = totalPrice
    }

    private fun onOrderButtonTapped() {
        orderViewModel.progress()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val TAG = "SummaryFragment"
    }
}