package com.example.sicakpizzalar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sicakpizzalar.databinding.FragmentCartBinding
import com.example.sicakpizzalar.databinding.ItemPizzaBinding

typealias AdapterFunctionsSignature = (Int) -> (Unit)

class CartListDiffCallback(): DiffUtil.ItemCallback<PizzaOrder>() {
    override fun areItemsTheSame(oldItem: PizzaOrder, newItem: PizzaOrder): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PizzaOrder, newItem: PizzaOrder): Boolean {
        return oldItem == newItem
    }
}


class CartListAdapter(var deleteItemFromOrderList: AdapterFunctionsSignature,
                      var editItemFromOrderList: AdapterFunctionsSignature,
                      var increasePizzaNumber: AdapterFunctionsSignature,
                      var decreasePizzaNumber: AdapterFunctionsSignature): ListAdapter<PizzaOrder, CartListAdapter.CartViewHolder>(CartListDiffCallback()) {

    class CartViewHolder(val binding: ItemPizzaBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemPizzaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = CartViewHolder(binding)

        return holder
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {

        holder.binding.deleteItemButton.setOnClickListener {
            deleteItemFromOrderList(position)
        }

        holder.binding.editItemButton.setOnClickListener {
            editItemFromOrderList(position)
        }

        holder.binding.increasePizzaAmountTV.setOnClickListener {
            increasePizzaNumber(position)
        }

        holder.binding.decreasePizzaAmountTV.setOnClickListener {
            decreasePizzaNumber(position)
        }

        val ctx = holder.itemView.context
        val order = getItem(position)

        val pizzaName = ctx.getString(order.pizzaType!!.getPizzaTypeNameResourceID())
        val doughType = ctx.getString(order.doughType!!.getDoughTypeNameResourceID())
        val pizzaPrice = ctx.getString(R.string.format_price, order.pizzaPrice?.times(order.pizzaCount) ?: 0)
        var toppings = ""
        for (i in order.toppingTypes.indices){
            toppings += ctx.getString(order.toppingTypes.elementAt(i).getToppingsTypeNameResourceID())
            if(i != order.toppingTypes.size - 1)
                toppings += "\n"
        }

        holder.binding.pizzaTypeTV.text = "$pizzaName"
        holder.binding.pizzaNumberTV.text = "${position+1}"
        holder.binding.priceTV.text = pizzaPrice
        holder.binding.doughTypeTV.text = doughType
        holder.binding.toppingsTV.text = toppings
        holder.binding.pizzaAmountTV.text = "${order.pizzaCount}"

        holder.binding.toppingsTitleTV.visibility = if (order.toppingTypes.size == 0) View.GONE else View.VISIBLE
        holder.binding.toppingsTV.visibility = if (order.toppingTypes.size == 0) View.GONE else View.VISIBLE
    }
}



class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by activityViewModels()

    private var listAdapter: CartListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.orderButton.setOnClickListener {
            onOrderButtonTapped()
        }

        binding.addPizzaButton.setOnClickListener {
            onAddNewPizzaItemButton()
        }

        orderViewModel.totalPrice.observe(viewLifecycleOwner) { totalPrice ->
            setPriceText(totalPrice)
        }

        orderViewModel.isOrderAllowed.observe(viewLifecycleOwner) { allowed ->
            binding.orderButton.isEnabled = allowed
        }

        listAdapter = CartListAdapter(::deleteItem, ::editItem, ::increasePizzaCount, ::decreasePizzaCount)

        binding.typesRecyclerView.adapter = listAdapter
        listAdapter!!.submitList(orderViewModel.pizzaOrderList)
        listAdapter!!.notifyDataSetChanged()
    }

    private fun onAddNewPizzaItemButton() {
        orderViewModel.resetOrder()
    }

    private fun setPriceText(price: Int) {
        val priceStr = getString(R.string.format_price, price)
        val totalPrice = getString(R.string.format_total_price, priceStr)
        binding.priceTV.text = totalPrice
    }

    private fun onOrderButtonTapped() {
        //TODO
    }

    private fun deleteItem(position: Int){
        orderViewModel.deleteOrderFromPizzaOrderList(position)
        listAdapter!!.notifyDataSetChanged()
    }

    private fun editItem(position: Int){
        orderViewModel.editPizzaOrder(position)
    }

    private fun increasePizzaCount(position: Int){
        orderViewModel.increasePizzaCount(position)
        listAdapter!!.notifyDataSetChanged()
    }

    private fun decreasePizzaCount(position: Int){
        orderViewModel.decreasePizzaCount(position)
        listAdapter!!.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val TAG = "CartFragment"
    }
}