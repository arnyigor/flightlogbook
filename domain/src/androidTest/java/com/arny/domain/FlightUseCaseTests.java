package com.arny.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.arny.domain.common.IPreferencesInteractor;
import com.arny.domain.common.IResourceProvider;
import com.arny.domain.files.FilesRepository;
import com.arny.domain.flights.FlightsInteractor;
import com.arny.domain.flights.FlightsRepository;
import com.arny.domain.flighttypes.FlightTypesRepository;
import com.arny.domain.models.Flight;
import com.arny.domain.planetypes.AircraftTypesRepository;
import com.arny.flightlogbook.customfields.repository.ICustomFieldsRepository;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FlightUseCaseTests {
    private FlightsInteractor flightsInteractor;
    @Mock
    private FlightTypesRepository flightTypesRepository;
    @Mock
    private FlightsRepository flightsRepository;
    @Mock
    private IResourceProvider resourcesProvider;
    @Mock
    private AircraftTypesRepository aircraftTypesRepository;
    @Mock
    private IPreferencesInteractor prefsInteractor;
    @Mock
    private ICustomFieldsRepository customFieldsRepository;
    @Mock
    private FilesRepository filesRepository;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        flightsInteractor = new FlightsInteractor(
                flightTypesRepository,
                flightsRepository,
                resourcesProvider,
                aircraftTypesRepository,
                customFieldsRepository,
                prefsInteractor,
                filesRepository
        );
    }

    @Test
    public void testGetFlightsUseCase() {
        List<Flight> flights = flightsInteractor.loadDBFlights();
        assertThat(flights).isNotEmpty();
    }
}
