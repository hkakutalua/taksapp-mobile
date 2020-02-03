package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.application.auth.viewmodels.LoginViewModel
import com.taksapp.taksapp.application.auth.viewmodels.RiderSignUpOtpConfirmationViewModel
import com.taksapp.taksapp.application.auth.viewmodels.RiderSignUpViewModel
import com.taksapp.taksapp.application.launch.viewmodels.LaunchViewModel
import com.taksapp.taksapp.application.riders.taxirequests.viewmodels.AutocompletePlaceChooserViewModel
import com.taksapp.taksapp.application.riders.taxirequests.viewmodels.FareEstimationViewModel
import com.taksapp.taksapp.application.riders.taxirequests.viewmodels.TaxiRequestViewModel
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
    viewModel {
        AutocompletePlaceChooserViewModel(
            get()
        )
    }
    viewModel {
        FareEstimationViewModel(
            get(),
            get(),
            androidContext()
        )
    }
    viewModel { (taxiRequest: TaxiRequest) ->
        TaxiRequestViewModel(
            taxiRequest,
            get(),
            get(),
            androidContext()
        )
    }
}