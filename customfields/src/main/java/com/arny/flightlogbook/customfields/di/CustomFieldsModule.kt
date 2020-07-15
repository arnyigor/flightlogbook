package com.arny.flightlogbook.customfields.di

import com.arny.flightlogbook.customfields.domain.CustomFieldInteractor
import com.arny.flightlogbook.customfields.domain.ICustomFieldInteractor
import com.arny.flightlogbook.customfields.repository.CustomFieldsRepository
import com.arny.flightlogbook.customfields.repository.ICustomFieldsRepository
import dagger.Binds
import dagger.Module

@Module
interface CustomFieldsModule {
    @Binds
    fun bindCustomFieldsInteractor(interactor: CustomFieldInteractor): ICustomFieldInteractor

    @Binds
    fun bindCustomFieldsRepository(interactor: CustomFieldsRepository): ICustomFieldsRepository

}