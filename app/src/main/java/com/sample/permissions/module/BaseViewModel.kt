package com.sample.permissions.module

import android.app.Application
import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseViewModel : ViewModel(), KoinComponent {
    // DI
    val app: Application by inject()
}