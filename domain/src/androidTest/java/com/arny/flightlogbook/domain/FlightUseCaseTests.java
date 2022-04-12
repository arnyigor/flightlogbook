package com.arny.flightlogbook.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.arny.flightlogbook.customfields.repository.ICustomFieldsRepository;
import com.arny.flightlogbook.domain.common.IPreferencesInteractor;
import com.arny.flightlogbook.domain.common.IResourceProvider;
import com.arny.flightlogbook.domain.files.FilesRepository;
import com.arny.flightlogbook.domain.flights.FlightsInteractor;
import com.arny.flightlogbook.domain.flights.FlightsRepository;
import com.arny.flightlogbook.domain.flighttypes.FlightTypesRepository;
import com.arny.flightlogbook.domain.models.Flight;
import com.arny.flightlogbook.domain.planetypes.AircraftTypesRepository;

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
