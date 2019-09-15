package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.ui.auth.viewmodels.RiderLoginViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { RiderLoginViewModel(get(), get()) }
}