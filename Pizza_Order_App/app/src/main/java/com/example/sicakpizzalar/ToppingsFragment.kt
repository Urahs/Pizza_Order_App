package com.example.sicakpizzalar

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sicakpizzalar.databinding.FragmentToppingsBinding
import com.example.sicakpizzalar.databinding.ItemToppingBinding

class ToppingsListDiffCallback(private val toppingsSelectionStateDataSource: (ToppingsType) -> (Boolean)): DiffUtil.ItemCallback<ToppingsType>() {
    override fun areItemsTheSame(oldItem: ToppingsType, newItem: ToppingsType): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ToppingsType, newItem: ToppingsType): Boolean {
        return toppingsSelectionStateDataSource(oldItem) == toppingsSelectionStateDataSource(newItem)
    }
}


class ToppingsListAdapter(private val toppingsSelectionStateDataSource: (ToppingsType) -> (Boolean),
                           private val itemSelectionHandler: (ToppingsType) -> (Unit)): ListAdapter<ToppingsType, ToppingsListAdapter.ToppingsViewHolder>(ToppingsListDiffCallback(toppingsSelectionStateDataSource)) {

    class ToppingsViewHolder(val binding: ItemToppingBinding): RecyclerView.ViewHolder(binding.root) {
        lateinit var type: ToppingsType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToppingsViewHolder {
        val binding = ItemToppingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ToppingsViewHolder(binding)
        holder.binding.root.setOnClickListener {
            itemSelectionHandler(holder.type)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ToppingsViewHolder, position: Int) {
        val ctx = holder.itemView.context
        val type = getItem(position)
        holder.type = type

        val toppingName = ctx.getString(type.getToppingsTypeNameResourceID())
        val toppingPrice = ctx.getString(R.string.format_price, type.getPrice())

        holder.binding.toppingNameTV.text = "$toppingName - $toppingPrice"

        val isSelected = toppingsSelectionStateDataSource(type)
        val colorStr = if (isSelected) "#FFAAAA" else "#FFFFFF"
        holder.binding.root.setBackgroundColor(Color.parseColor(colorStr))
    }
}



class ToppingsFragment : Fragment() {

    private var _binding: FragmentToppingsBinding? = null
    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by activityViewModels()

    private var listAdapter: ToppingsListAdapter? = null

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

        orderViewModel.pizzaPrice.observe(viewLifecycleOwner) { totalPrice ->
            setPriceText(totalPrice)
        }

        orderViewModel.isProgressAllowed.observe(viewLifecycleOwner) { allowed ->
            binding.nextButton.isEnabled = allowed
        }

        listAdapter = ToppingsListAdapter(::isToppingSelected) { selectedPizzaType ->
            processToppingSelection(selectedPizzaType)
        }

        binding.typesRecyclerView.adapter = listAdapter
        listAdapter!!.submitList(orderViewModel.toppingTypes)
        listAdapter!!.notifyDataSetChanged()
    }

    private fun isToppingSelected(type: ToppingsType): Boolean {
        return orderViewModel.isToppingSelected(type)
    }

    private fun processToppingSelection(type: ToppingsType) {
        Log.d("TypeFragment", "Pizza Selected: $type")
        orderViewModel.selectTopping(type)
        listAdapter?.submitList(orderViewModel.toppingTypes)
        listAdapter?.notifyDataSetChanged()
    }

    private fun setPriceText(price: Int) {
        val priceStr = getString(R.string.format_price, price)
        val totalPrice = getString(R.string.format_pizza_price, priceStr)
        binding.priceTV.text = totalPrice
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