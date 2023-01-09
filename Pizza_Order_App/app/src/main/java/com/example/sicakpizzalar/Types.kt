package com.example.sicakpizzalar

enum class OrderStep {
    INITIAL,
    PIZZA_TYPE,
    DOUGH_TYPE,
    TOPPINGS,
    SUMMARY
}

enum class PizzaType {
    MARGARITA {
        override fun getPizzaTypeNameResourceID(): Int = R.string.pizza_type_margarita
        override fun getPrice(): Int = 90
    },
    PEPPERONI {
        override fun getPizzaTypeNameResourceID(): Int = R.string.pizza_type_pepperoni
        override fun getPrice(): Int = 95
    },
    MIXED {
        override fun getPizzaTypeNameResourceID(): Int = R.string.pizza_type_mixed
        override fun getPrice(): Int = 110
    },
    VEGAN {
        override fun getPizzaTypeNameResourceID(): Int = R.string.pizza_type_vegan
        override fun getPrice(): Int = 100
    };

    abstract fun getPizzaTypeNameResourceID(): Int
    abstract fun getPrice(): Int
}

enum class DoughType {
    THIN {
        override fun getDoughTypeNameResourceID(): Int = R.string.dough_type_thin
    },
    THICK {
        override fun getDoughTypeNameResourceID(): Int = R.string.dough_type_thick
    },
    EXTRA_THICK {
        override fun getDoughTypeNameResourceID(): Int = R.string.dough_type_extra_thick
    };

    abstract fun getDoughTypeNameResourceID(): Int
}

enum class ToppingsType {
    CORN {
        override fun getToppingsTypeNameResourceID(): Int = R.string.topping_type_corn
        override fun getPrice(): Int = 5
    },
    MUSHROOM {
        override fun getToppingsTypeNameResourceID(): Int = R.string.topping_type_mushroom
        override fun getPrice(): Int = 10
    },
    OLIVE {
        override fun getToppingsTypeNameResourceID(): Int = R.string.topping_type_olive
        override fun getPrice(): Int = 8
    },
    TOMATO {
        override fun getToppingsTypeNameResourceID(): Int = R.string.topping_type_tomato
        override fun getPrice(): Int = 7
    },
    CHEESE {
        override fun getToppingsTypeNameResourceID(): Int = R.string.topping_type_cheese
        override fun getPrice(): Int = 15
    };

    abstract fun getToppingsTypeNameResourceID(): Int
    abstract fun getPrice(): Int
}


