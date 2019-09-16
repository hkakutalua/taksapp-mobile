package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.data.repositories.AuthenticationRepository
import com.taksapp.taksapp.data.repositories.DriverAuthenticationRepository
import com.taksapp.taksapp.data.repositories.RiderAuthenticationRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoriesModule = module {
    single(named("RiderAuthenticationRepository")) { RiderAuthenticationRepository(get(), get()) }
    single(named("DriverAuthenticationRepository")) { DriverAuthenticationRepository(get(), get()) }
}