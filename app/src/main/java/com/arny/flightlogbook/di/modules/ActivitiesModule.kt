package com.arny.flightlogbook.di.modules

import com.arny.flightlogbook.di.ActivityScope
import com.arny.flightlogbook.presentation.navigation.NavigationActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivitiesModule {
    @ActivityScope
    @ContributesAndroidInjector(
        modules = [
            FragmentsModule::class,
        ]
    )
    abstract fun bindMainActivity(): NavigationActivity
}
