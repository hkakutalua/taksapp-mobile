package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.application.taxirequest.backgroundhandlers.TaxiRequestBackgroundHandler
import org.koin.dsl.module

val backgroundHandlersModule = module {
    factory { TaxiRequestBackgroundHandler(get(), get())}
}