package io.dolby.app.di

import io.dolby.app.features.publish.PublishViewModel
import io.dolby.app.navigation.NavigationViewModel
import io.dolby.app.navigation.Navigator
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val navigationModule = module {
    single { Navigator() }
    viewModel { NavigationViewModel(get()) }
}

val publishModule = module {
    viewModel { PublishViewModel() }
}
