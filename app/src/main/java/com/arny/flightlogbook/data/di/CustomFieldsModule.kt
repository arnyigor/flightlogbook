package com.arny.flightlogbook.data.di

import com.arny.flightlogbook.data.repository.CustomFieldsRepository
import com.arny.flightlogbook.domain.customfields.CustomFieldInteractor
import com.arny.flightlogbook.domain.customfields.ICustomFieldInteractor
import com.arny.flightlogbook.domain.customfields.ICustomFieldsRepository
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
