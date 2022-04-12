package com.arny.flightlogbook.di

import com.arny.flightlogbook.presentation.airports.edit.AirportEditFragment
import com.arny.flightlogbook.presentation.airports.list.AirportsFragment
import com.arny.flightlogbook.presentation.customfields.edit.CustomFieldEditFragment
import com.arny.flightlogbook.presentation.customfields.list.CustomFieldsListFragment
import com.arny.flightlogbook.presentation.flights.addedit.view.AddEditFragment
import com.arny.flightlogbook.presentation.flights.viewflights.view.FlightListFragment
import com.arny.flightlogbook.presentation.flighttypes.list.FlightTypesFragment
import com.arny.flightlogbook.presentation.planetypes.edit.PlaneTypeEditFragment
import com.arny.flightlogbook.presentation.planetypes.list.PlaneTypesFragment
import com.arny.flightlogbook.presentation.settings.SettingsFragment
import com.arny.flightlogbook.presentation.statistic.view.StatisticFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentsModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFlightListFragmentInjector(): FlightListFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeEditFragmentInjector(): AddEditFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeSettingsFragmentInjector(): SettingsFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeAirportsFragmentInjector(): AirportsFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeAirportEditFragmentInjector(): AirportEditFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeCustomFieldEditFragmentInjector(): CustomFieldEditFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeCustomFieldsListFragmentInjector(): CustomFieldsListFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFlightTypesFragmentInjector(): FlightTypesFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributePlaneTypeEditFragmentInjector(): PlaneTypeEditFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributePlaneTypesFragmentInjector(): PlaneTypesFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeStatisticFragmentInjector(): StatisticFragment
}
