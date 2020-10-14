package com.arny.flightlogbook.data.di

import com.arny.flightlogbook.customfields.domain.CustomFieldInteractor
import com.arny.flightlogbook.customfields.domain.ICustomFieldInteractor
import com.arny.flightlogbook.customfields.repository.ICustomFieldsRepository
import com.arny.flightlogbook.data.repositories.CustomFieldsRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface CustomFieldsModule {
    @Singleton
    @Binds
    fun bindCustomFieldsInteractor(interactor: CustomFieldInteractor): ICustomFieldInteractor

    @Singleton
    @Binds
    fun bindCustomFieldsRepository(interactor: CustomFieldsRepository): ICustomFieldsRepository
}
