package com.taksapp.taksapp.domain

import java.math.BigDecimal
import java.util.*

data class Money(val currency: Currency, val amount: BigDecimal) {
    override fun toString(): String {
        return "${currency.symbol} %.2f".format(Locale("pt-PT"), amount)
    }
}