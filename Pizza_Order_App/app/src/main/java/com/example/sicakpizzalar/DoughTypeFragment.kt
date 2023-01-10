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
import com.example.sicakpizzalar.databinding.FragmentDoughTypeBinding
import com.example.sicakpizzalar.databinding.FragmentInitialBinding
import com.example.sicakpizzalar.databinding.ItemDoughTypeBinding
import com.example.sicakpizzalar.databinding.ItemPizzaTypeBinding

class DoughTypeListDiffCallback(private val doughTypeSelectionStateDataSource: (DoughType) -> (Boolean)): DiffUtil.ItemCallback<DoughType>() {
    override fun areItemsTheSame(oldItem: DoughType, newItem: DoughType): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: DoughType, newItem: DoughType): Boolean {
        return doughTypeSelectionStateDataSource(oldItem) == doughTypeSelectionStateDataSource(newItem)
    }
}

class DoughTypeListAdapter(private val doughTypeSelectionStateDataSource: (DoughType) -> (Boolean),
                           private val itemSelectionHandler: (DoughType) -> (Unit)): ListAdapter<DoughType, DoughTypeListAdapter.DoughTypeViewHolder>(DoughTypeListDiffCallback(doughTypeSelectionStateDataSource)) {

    class DoughTypeViewHolder(val binding: ItemDoughTypeBinding): RecyclerView.ViewHolder(binding.root) {
        lateinit var type: DoughType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoughTypeViewHolder {
        val binding = ItemDoughTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = DoughTypeViewHolder(binding)
        holder.binding.root.setOnClickListener {
            itemSelectionHandler(holder.type)
        }
        return holder
    }

    override fun onBindViewHolder(holder: DoughTypeViewHolder, position: Int) {
        val ctx = holder.itemView.context
        val type = getItem(position)
        holder.type = type

        val doughTypeName = ctx.getString(type.getDoughTypeNameResourceID())
        holder.binding.doughTypeTV.text = "$doughTypeName"

        val isSelected = doughTypeSelectionStateDataSource(type)
        val colorStr = if (isSelected) "#FFAAAA" else "#FFFFFF"
        holder.binding.root.setBackgroundColor(Color.parseColor(colorStr))
    }
}



class DoughTypeFragment : Fragment() {

    private var _binding: FragmentDoughTypeBinding? = null
    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by activityViewModels()

    private var listAdapter: DoughTypeListAdapter? = null

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

        listAdapter = DoughTypeListAdapter(::isDoughTypeSelected) { selectedDoughType ->
            processDoughTypeSelection(selectedDoughType)
        }

        binding.typesRecyclerView.adapter = listAdapter
        listAdapter!!.submitList(orderViewModel.doughTypes)
        listAdapter!!.notifyDataSetChanged()

        orderViewModel.totalPrice.value?.let { setPriceText(it) }
    }

    private fun isDoughTypeSelected(type: DoughType): Boolean {
        return orderViewModel.isDoughTypeSelected(type)
    }

    private fun processDoughTypeSelection(type: DoughType) {
        Log.d("DoughTypeFragment", "Dough Selected: $type")
        orderViewModel.selectDoughType(type)
        listAdapter?.submitList(orderViewModel.doughTypes)
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
        val TAG = "DoughTypeFragment"
    }
}