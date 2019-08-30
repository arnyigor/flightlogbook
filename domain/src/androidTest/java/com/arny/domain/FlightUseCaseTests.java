package com.arny.domain;

import com.arny.data.repositories.MainRepositoryImpl;
import com.arny.domain.flights.FlightsUseCase;
import com.arny.domain.models.Flight;

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

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FlightUseCaseTests {
    private FlightsUseCase flightsUseCase;
    @Mock
    private MainRepositoryImpl mockRepository;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Rule public ExpectedException expectedException = ExpectedException.none();
    @Before
    public void setUp() {
        flightsUseCase = new FlightsUseCase(mockRepository);
    }

    @Test
    public void testGetFlightsUseCase() {
        List<Flight> flights = flightsUseCase.loadDBFlights();
        assertThat(flights).isNotEmpty();
    }
}
