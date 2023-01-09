package com.example.sicakpizzalar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


typealias PostNavigationHandler = () -> (Unit)
typealias NavigationProgressConditionHandler = () -> (Boolean)

class OrderNavigationItem(val step: OrderStep) {
    var allowsGoingToPreviousStep = true
    var allowsCancellation = false

    var progressConditionHandler: NavigationProgressConditionHandler? = null

    fun allowsProgress(): Boolean {
        return (progressConditionHandler?.invoke() ?: true)
    }

}


class OrderNavigation {
    private var items: MutableList<OrderNavigationItem> = mutableListOf()
    private var currentStepIndex = 0

    val currentStep: OrderStep get() = items[currentStepIndex].step
    val currentCancellationAllowance get() = items[currentStepIndex].allowsCancellation
    val currentBackProgressAllowance get() = items[currentStepIndex].allowsGoingToPreviousStep
    val progressAllowance get() = items[currentStepIndex].allowsProgress()

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
        changeNavigation(currentStepIndex - 1)
    }

    fun reset() {
        changeNavigation(0)
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
            }
        )
        addItem(
            OrderNavigationItem(OrderStep.DOUGH_TYPE).apply {
                progressConditionHandler = ::progressFromDoughTypeSelectionAllowed
            }
        )
        addItem(OrderNavigationItem(OrderStep.TOPPINGS))
        addItem(
            OrderNavigationItem(OrderStep.SUMMARY).apply {
                allowsCancellation = true
            }
        )

        addPostNavigationHandler(::postNavigationHandler)
    }

    private val _isBackProgressAllowed: MutableLiveData<Boolean> = MutableLiveData(false)
    val isBackProgressAllowed: LiveData<Boolean> = _isBackProgressAllowed

    private val _isCancelAllowed: MutableLiveData<Boolean> = MutableLiveData(false)
    val isCancelAllowed: LiveData<Boolean> = _isCancelAllowed

    private val _isProgressAllowed: MutableLiveData<Boolean> = MutableLiveData(orderNavigation.progressAllowance)
    val isProgressAllowed: LiveData<Boolean> = _isProgressAllowed

    private val _navigation: MutableLiveData<OrderStep> = MutableLiveData(orderNavigation.currentStep)
    val navigation: LiveData<OrderStep> = _navigation

    private val _totalPrice: MutableLiveData<Int> = MutableLiveData(0)
    val totalPrice: LiveData<Int> = _totalPrice

    val pizzaTypes: List<PizzaType> = listOf(
        PizzaType.MARGARITA,
        PizzaType.PEPPERONI,
        PizzaType.MIXED,
        PizzaType.VEGAN
    )
    private var selectedPizzaType: PizzaType? = null

    val doughTypes: List<DoughType> = listOf(
        DoughType.THIN,
        DoughType.THICK,
        DoughType.EXTRA_THICK
    )

    private var selectedDoughType: DoughType? = null

    val toppingTypes: List<ToppingsType> = listOf(
        ToppingsType.CORN,
        ToppingsType.MUSHROOM,
        ToppingsType.OLIVE,
        ToppingsType.CHEESE,
        ToppingsType.TOMATO
    )

    private val selectedToppingTypes: MutableSet<ToppingsType> = mutableSetOf()

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

    fun selectPizzaType(type: PizzaType) {
        selectedPizzaType = type
        calculateTotalPriceAndNotify()
        _isProgressAllowed.value = progressFromTypeSelectionAllowed()
    }

    fun selectDoughType(type: DoughType) {
        selectedDoughType = type
        _isProgressAllowed.value =  progressFromDoughTypeSelectionAllowed()
    }

    fun cancelOrder() {
        orderNavigation.reset()
    }

    fun isDoughTypeSelected(type: DoughType): Boolean {
        return type == selectedDoughType
    }

    private fun isDoughTypeSelected(): Boolean {
        return selectedDoughType != null
    }

    private fun progressFromTypeSelectionAllowed(): Boolean {
        return isPizzaTypeSelected()
    }

    private fun progressFromDoughTypeSelectionAllowed(): Boolean {
        return isDoughTypeSelected()
    }

    private fun calculateTotalPriceAndNotify() {
        var sum = 0
        sum += selectedPizzaType?.getPrice() ?: 0
        _totalPrice.value = sum
    }

    private fun postNavigationHandler() {
        _navigation.value = orderNavigation.currentStep
        _isCancelAllowed.value = orderNavigation.currentCancellationAllowance
        _isBackProgressAllowed.value = orderNavigation.currentBackProgressAllowance
        _isProgressAllowed.value = orderNavigation.progressAllowance
    }

}