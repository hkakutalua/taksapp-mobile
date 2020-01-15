package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.data.repositories.AuthenticationRepository
import com.taksapp.taksapp.data.repositories.PlacesRepository
import com.taksapp.taksapp.data.repositories.RemoteFareRepository
import com.taksapp.taksapp.domain.interfaces.FareRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoriesModule = module {
    single { AuthenticationRepository(get(), get()) }
    factory { PlacesRepository(get(), androidContext()) }
    factory<FareRepository> { RemoteFareRepository(get(), androidContext()) }
}