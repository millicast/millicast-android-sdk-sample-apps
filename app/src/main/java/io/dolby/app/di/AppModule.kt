package io.dolby.app.di

import com.millicast.utils.Queue
import io.dolby.app.features.publish.CustomSourcePublishViewModel
import io.dolby.app.features.publish.PublishViewModel
import io.dolby.app.features.subscribe.ui.SubscribeViewModel
import io.dolby.app.navigation.NavigationViewModel
import io.dolby.app.navigation.Navigator
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val navigationModule = module {
    single { Navigator() }
    viewModel { NavigationViewModel(get()) }
}

val publishModule = module {
    //viewModel { PublishViewModel() }
    viewModel { CustomSourcePublishViewModel() }
}

val subscribeModule = module {
    factory { Queue() }
    viewModel { parameters -> SubscribeViewModel(isMultiView = parameters.get()) }
}
