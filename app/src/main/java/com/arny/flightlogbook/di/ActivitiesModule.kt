package com.arny.flightlogbook.di

import com.arny.flightlogbook.presentation.common.FragmentContainerActivity
import com.arny.flightlogbook.presentation.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface ActivitiesModule {

    @ActivityScope
    @ContributesAndroidInjector
    fun mainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector
    fun fragmentContainerActivity(): FragmentContainerActivity
}