package io.dolby.app.di

import io.dolby.app.ui.navigation.NavigationViewModel
import io.dolby.app.ui.navigation.Navigator
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val navigationModule = module {
    single { Navigator() }
    viewModel { NavigationViewModel(get()) }
}
