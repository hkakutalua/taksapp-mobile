package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.data.repositories.AuthenticationRepository
import com.taksapp.taksapp.data.repositories.PlacesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoriesModule = module {
    single { AuthenticationRepository(get(), get()) }
    factory { PlacesRepository(get(), androidContext()) }
}