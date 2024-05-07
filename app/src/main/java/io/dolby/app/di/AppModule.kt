package io.dolby.app.di

import io.dolby.app.ui.navigation.NavigationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { NavigationViewModel() }
}
