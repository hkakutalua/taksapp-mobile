package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.data.repositories.RiderAuthenticationRepository
import org.koin.dsl.module

val repositoriesModule = module {
    single { RiderAuthenticationRepository(get(), get()) }
}