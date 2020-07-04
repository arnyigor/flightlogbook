package com.arny.flightlogbook.di

import com.arny.domain.common.PreferencesProvider
import com.arny.domain.common.ResourcesProvider
import com.arny.flightlogbook.data.repositories.PreferencesProviderImpl
import com.arny.flightlogbook.data.repositories.ResourcesProviderImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface ResourceModule {
    @Binds
    @Singleton
    fun bindResources(sources: ResourcesProviderImpl): ResourcesProvider

    @Binds
    @Singleton
    fun bindPrefs(sources: PreferencesProviderImpl): PreferencesProvider
}
