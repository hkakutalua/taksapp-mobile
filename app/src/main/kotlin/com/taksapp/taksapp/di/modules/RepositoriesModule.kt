package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.data.repositories.AuthenticationRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoriesModule = module {
    single {AuthenticationRepository(get())}
}