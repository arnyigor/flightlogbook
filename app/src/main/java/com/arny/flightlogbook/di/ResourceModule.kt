package com.arny.flightlogbook.di

import com.arny.flightlogbook.data.repositories.PreferencesProviderImpl
import com.arny.flightlogbook.data.repositories.ResourcesProviderImpl
import com.arny.flightlogbook.domain.common.IPreferencesInteractor
import com.arny.flightlogbook.domain.common.IResourceProvider
import com.arny.flightlogbook.domain.common.PreferencesInteractor
import com.arny.flightlogbook.domain.common.PreferencesProvider
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface ResourceModule {
    @Binds
    @Singleton
    fun bindResources(sources: ResourcesProviderImpl): IResourceProvider

    @Binds
    @Singleton
    fun bindPrefs(sources: PreferencesProviderImpl): PreferencesProvider

    @Binds
    @Singleton
    fun bindPrefsInteractor(interactor: PreferencesInteractor): IPreferencesInteractor
}
