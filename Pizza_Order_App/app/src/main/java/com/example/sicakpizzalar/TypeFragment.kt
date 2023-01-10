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
import com.example.sicakpizzalar.databinding.FragmentTypeBinding
import com.example.sicakpizzalar.databinding.ItemPizzaTypeBinding


class PizzaTypeListDiffCallback(private val typeSelectionStateDataSource: (PizzaType) -> (Boolean)): DiffUtil.ItemCallback<PizzaType>() {
    override fun areItemsTheSame(oldItem: PizzaType, newItem: PizzaType): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PizzaType, newItem: PizzaType): Boolean {
        return typeSelectionStateDataSource(oldItem) == typeSelectionStateDataSource(newItem)
    }
}


class PizzaTypeListAdapter(private val typeSelectionStateDataSource: (PizzaType) -> (Boolean),
                           private val itemSelectionHandler: (PizzaType) -> (Unit)): ListAdapter<PizzaType, PizzaTypeListAdapter.PizzaTypeViewHolder>(PizzaTypeListDiffCallback(typeSelectionStateDataSource)) {

    class PizzaTypeViewHolder(val binding: ItemPizzaTypeBinding): RecyclerView.ViewHolder(binding.root) {
        lateinit var type: PizzaType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PizzaTypeViewHolder {
        val binding = ItemPizzaTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = PizzaTypeViewHolder(binding)
        holder.binding.root.setOnClickListener {
            itemSelectionHandler(holder.type)
        }
        return holder
    }

    override fun onBindViewHolder(holder: PizzaTypeViewHolder, position: Int) {
        val ctx = holder.itemView.context
        val type = getItem(position)
        holder.type = type

        val pizzaName = ctx.getString(type.getPizzaTypeNameResourceID())
        val pizzaPrice = ctx.getString(R.string.format_price, type.getPrice())

        holder.binding.pizzaNameTV.text = "$pizzaName - $pizzaPrice"

        val isSelected = typeSelectionStateDataSource(type)
        val colorStr = if (isSelected) "#FFAAAA" else "#FFFFFF"
        holder.binding.root.setBackgroundColor(Color.parseColor(colorStr))
    }
}



class TypeFragment : Fragment() {

    private var _binding: FragmentTypeBinding? = null
    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by activityViewModels()

    private var listAdapter: PizzaTypeListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTypeBinding.inflate(inflater, container, false)
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

        listAdapter = PizzaTypeListAdapter(::isTypeSelected) { selectedPizzaType ->
            processPizzaTypeSelection(selectedPizzaType)
        }

        binding.typesRecyclerView.adapter = listAdapter
        listAdapter!!.submitList(orderViewModel.pizzaTypes)
        listAdapter!!.notifyDataSetChanged()
    }

    private fun isTypeSelected(type: PizzaType): Boolean {
        return orderViewModel.isPizzaTypeSelected(type)
    }

    private fun processPizzaTypeSelection(type: PizzaType) {
        orderViewModel.selectPizzaType(type)
        listAdapter?.submitList(orderViewModel.pizzaTypes)
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
        listAdapter = null
    }

    companion object {
        val TAG = "TypeFragment"
    }
}