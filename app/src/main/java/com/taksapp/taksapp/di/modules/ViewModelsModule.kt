package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.ui.auth.viewmodels.LoginViewModel
import com.taksapp.taksapp.ui.auth.viewmodels.RiderSignUpOtpConfirmationViewModel
import com.taksapp.taksapp.ui.auth.viewmodels.RiderSignUpViewModel
import com.taksapp.taksapp.ui.launch.viewmodels.LaunchViewModel
import com.taksapp.taksapp.ui.taxi.viewmodels.AutocompletePlaceChooserViewModel
import com.taksapp.taksapp.ui.taxi.viewmodels.FareEstimationViewModel
import com.taksapp.taksapp.ui.taxi.viewmodels.TaxiRequestViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import kotlin.time.ExperimentalTime

@ExperimentalTime
val viewModelsModule = module {
    viewModel { LaunchViewModel(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { RiderSignUpViewModel(get(), get()) }
    viewModel { RiderSignUpOtpConfirmationViewModel(get(), get()) }
    viewModel { AutocompletePlaceChooserViewModel(get()) }
    viewModel { FareEstimationViewModel(get()) }
    viewModel { TaxiRequestViewModel(get(), androidContext()) }
}