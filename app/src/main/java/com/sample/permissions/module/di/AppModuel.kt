package com.sample.permissions.module.di

import com.sample.permissions.module.MainActivityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // single { SampleRepository() }
    viewModel { MainActivityViewModel() }
}