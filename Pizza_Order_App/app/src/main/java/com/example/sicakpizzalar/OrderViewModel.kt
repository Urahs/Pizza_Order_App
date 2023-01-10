package com.example.sicakpizzalar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.FieldPosition


typealias PostNavigationHandler = () -> (Unit)
typealias NavigationProgressConditionHandler = () -> (Boolean)

data class PizzaOrder(
    var pizzaType: PizzaType?,
    var doughType: DoughType?,
    var toppingTypes: MutableSet<ToppingsType>,
    var pizzaPrice: Int?
)

class OrderNavigationItem(val step: OrderStep) {
    var allowsGoingToPreviousStep = true
    var allowsCancellation = false

    var progressConditionHandler: NavigationProgressConditionHandler? = null
    var goBackConditionHandler: PostNavigationHandler? = null

    fun allowsProgress(): Boolean {
        return (progressConditionHandler?.invoke() ?: true)
    }
}


class OrderNavigation {
    private var items: MutableList<OrderNavigationItem> = mutableListOf()
    private var currentStepIndex = 0
    val firstStepOfPizzaSelection = OrderStep.PIZZA_TYPE
    val cartStep = OrderStep.CART

    val currentStep: OrderStep get() = items[currentStepIndex].step
    val currentCancellationAllowance get() = items[currentStepIndex].allowsCancellation
    val currentBackProgressAllowance get() = items[currentStepIndex].allowsGoingToPreviousStep
    val progressAllowance get() = items[currentStepIndex].allowsProgress()
    val cancelCurrentSelection get() = items[currentStepIndex].goBackConditionHandler

    var clearOrderList: (() -> Unit)? = null

    private var postNavigationHandlers: MutableList<PostNavigationHandler> = mutableListOf()

    fun addItem(item: OrderNavigationItem) {
        items.add(item)
    }

    fun addPostNavigationHandler(handler: PostNavigationHandler) {
        postNavigationHandlers.add(handler)
    }

    fun progress() {
        changeNavigation(currentStepIndex + 1)
    }

    fun goBack() {
        cancelCurrentSelection?.invoke()
        changeNavigation(currentStepIndex - 1)
    }

    fun cancel() {

        if(items[currentStepIndex].step == cartStep){
            clearOrderList?.invoke()
        }

        items.forEach { item->
            item.goBackConditionHandler?.invoke()
        }
        changeNavigation(0)
    }

    fun reset(){
        items.forEach { item->
            item.goBackConditionHandler?.invoke()
        }

        changeNavigation( getStepIndex(firstStepOfPizzaSelection) )
    }

    fun openCartScreen(){
        changeNavigation( getStepIndex(cartStep) )
    }

    private fun changeNavigation(targetIndex: Int) {

        if (isTargetIndexInvalid(targetIndex)) {
            throw AssertionError("Target Navigation Index is invalid. current: $currentStepIndex, target: $targetIndex")
        }

        currentStepIndex = targetIndex

        postNavigationHandlers.forEach { handler ->
            handler()
        }
    }

    private fun isTargetIndexInvalid(targetIndex: Int): Boolean {
        return targetIndex < 0 || targetIndex >= items.size
    }

    private fun getStepIndex(step: OrderStep): Int{
        return items.indexOfFirst { i -> i.step == step }
    }
}

class OrderViewModel: ViewModel() {

    private val orderNavigation: OrderNavigation = OrderNavigation().apply {
        addItem(
            OrderNavigationItem(OrderStep.INITIAL).apply {
                allowsGoingToPreviousStep = false
            }
        )
        addItem(
            OrderNavigationItem(OrderStep.PIZZA_TYPE).apply {
                progressConditionHandler = ::progressFromTypeSelectionAllowed
                goBackConditionHandler = ::cancelSelectionForPizzaType
            }
        )
        addItem(
            OrderNavigationItem(OrderStep.DOUGH_TYPE).apply {
                progressConditionHandler = ::progressFromDoughTypeSelectionAllowed
                goBackConditionHandler = ::cancelSelectionForDoughType
            }
        )
        addItem(
            OrderNavigationItem(OrderStep.TOPPINGS).apply {
                goBackConditionHandler = ::cancelSelectionsForToppings
            }
        )
        addItem(
            OrderNavigationItem(OrderStep.SUMMARY).apply {
                allowsCancellation = true
            }
        )
        addItem(
            OrderNavigationItem(OrderStep.CART).apply {
                allowsGoingToPreviousStep = false
                allowsCancellation = true
            }
        )

        addPostNavigationHandler(::postNavigationHandler)
        clearOrderList = ::clearOrders
    }

    var pizzaOrderList = mutableListOf<PizzaOrder>()

    private val _isBackProgressAllowed: MutableLiveData<Boolean> = MutableLiveData(false)
    val isBackProgressAllowed: LiveData<Boolean> = _isBackProgressAllowed

    private val _isCancelAllowed: MutableLiveData<Boolean> = MutableLiveData(false)
    val isCancelAllowed: LiveData<Boolean> = _isCancelAllowed

    private val _isOrderAllowed: MutableLiveData<Boolean> = MutableLiveData(false)
    val isOrderAllowed: LiveData<Boolean> = _isOrderAllowed

    private val _isProgressAllowed: MutableLiveData<Boolean> = MutableLiveData(orderNavigation.progressAllowance)
    val isProgressAllowed: LiveData<Boolean> = _isProgressAllowed

    private val _navigation: MutableLiveData<OrderStep> = MutableLiveData(orderNavigation.currentStep)
    val navigation: LiveData<OrderStep> = _navigation

    private val _pizzaPrice: MutableLiveData<Int> = MutableLiveData(0)
    val pizzaPrice: LiveData<Int> = _pizzaPrice

    private val _totalPrice: MutableLiveData<Int> = MutableLiveData(0)
    val totalPrice: LiveData<Int> = _totalPrice

    val pizzaTypes: List<PizzaType> = listOf(
        PizzaType.MARGARITA,
        PizzaType.PEPPERONI,
        PizzaType.MIXED,
        PizzaType.VEGAN
    )

    var selectedPizzaType: PizzaType? = null

    val doughTypes: List<DoughType> = listOf(
        DoughType.THIN,
        DoughType.THICK,
        DoughType.EXTRA_THICK
    )

    var selectedDoughType: DoughType? = null

    val toppingTypes: List<ToppingsType> = listOf(
        ToppingsType.CORN,
        ToppingsType.MUSHROOM,
        ToppingsType.OLIVE,
        ToppingsType.CHEESE,
        ToppingsType.TOMATO
    )

    val selectedToppingTypes: MutableSet<ToppingsType> = mutableSetOf()

    fun progress() {
        orderNavigation.progress()
    }

    fun goToPreviousOrderStep() {
        orderNavigation.goBack()
    }

    fun isPizzaTypeSelected(type: PizzaType): Boolean {
        return type == selectedPizzaType
    }

    private fun isPizzaTypeSelected(): Boolean {
        return selectedPizzaType != null
    }

    fun isDoughTypeSelected(type: DoughType): Boolean {
        return type == selectedDoughType
    }

    private fun isDoughTypeSelected(): Boolean {
        return selectedDoughType != null
    }

    fun isToppingSelected(type: ToppingsType): Boolean {
        return type in selectedToppingTypes
    }

    fun isToppingSelected(): Boolean {
        return selectedToppingTypes.size != 0
    }

    fun selectPizzaType(type: PizzaType) {
        selectedPizzaType = type
        calculateTotalPriceAndNotifyForPizzaType()
        _isProgressAllowed.value = progressFromTypeSelectionAllowed()
    }

    fun selectDoughType(type: DoughType) {
        selectedDoughType = type
        _isProgressAllowed.value =  progressFromDoughTypeSelectionAllowed()
    }

    fun selectTopping(type: ToppingsType) {
        if(type in selectedToppingTypes){
            selectedToppingTypes.remove(type)
            addToppingPriceToTotalPrice( -type.getPrice() )
        }
        else{
            selectedToppingTypes.add(type)
            addToppingPriceToTotalPrice( type.getPrice() )
        }
    }

    fun cancelOrder() {
        orderNavigation.cancel()
    }

    private fun progressFromTypeSelectionAllowed(): Boolean {
        return isPizzaTypeSelected()
    }

    private fun progressFromDoughTypeSelectionAllowed(): Boolean {
        return isDoughTypeSelected()
    }

    private fun calculateTotalPriceAndNotifyForPizzaType() {
        var sum = selectedPizzaType?.getPrice() ?: 0
        _pizzaPrice.value = sum
    }

    private fun addToppingPriceToTotalPrice(price: Int) {
        _pizzaPrice.value = _pizzaPrice.value?.plus(price)
    }

    private fun cancelSelectionForPizzaType(){
        if(selectedPizzaType != null){
            _pizzaPrice.value = _pizzaPrice.value?.minus(selectedPizzaType!!.getPrice())
            selectedPizzaType = null
        }
    }

    private fun cancelSelectionForDoughType(){
        selectedDoughType = null
    }

    private fun cancelSelectionsForToppings(){
        var price = _pizzaPrice.value ?: 0
        selectedToppingTypes.forEach {
            price -= it.getPrice()
        }
        _pizzaPrice.value = price
        selectedToppingTypes.clear()
    }

    fun addPizzaOrderToCart(){
        val order = PizzaOrder(
            selectedPizzaType,
            selectedDoughType,
            selectedToppingTypes.toMutableSet(),
            _pizzaPrice.value
        )
        pizzaOrderList.add(order)

        _totalPrice.value = _pizzaPrice.value?.let { _totalPrice.value?.plus(it) }
        updateOrderAllowed()
    }

    private fun postNavigationHandler() {
        _navigation.value = orderNavigation.currentStep
        _isCancelAllowed.value = orderNavigation.currentCancellationAllowance
        _isBackProgressAllowed.value = orderNavigation.currentBackProgressAllowance
        _isProgressAllowed.value = orderNavigation.progressAllowance
    }

    fun resetOrder() {
        orderNavigation.reset()
    }

    fun deleteOrderFromPizzaOrderList(position: Int){
        pizzaOrderList.get(position).pizzaPrice?.let {
            _totalPrice.value = _totalPrice.value!!.minus(it)
        }
        pizzaOrderList.removeAt(position)

        updateOrderAllowed()
    }

    private fun updateOrderAllowed(){
        _isOrderAllowed.value = if(pizzaOrderList.size != 0) true else false
    }

    fun openCartScreen(){
        orderNavigation.openCartScreen()
    }

    private fun clearOrders(){
        pizzaOrderList.clear()
        updateOrderAllowed()
        _totalPrice.value = 0
    }
}