package com.example.airline.service;

import com.example.airline.dto.FlightDTO;
import com.example.airline.entity.Flight;
import com.example.airline.exception.ResourceNotFoundException;
import com.example.airline.repository.FlightRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FlightService {

    private final FlightRepository flightRepository;

    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional
    public Flight addFlight(FlightDTO flightDTO) {
        Flight flight = new Flight();
        BeanUtils.copyProperties(flightDTO, flight);
        flight.setStatus(Flight.FlightStatus.SCHEDULED);
        return flightRepository.save(flight);
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public Flight getFlight(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
    }

    @Transactional
    public void updateFlightStatus(Long id, Flight.FlightStatus status) {
        Flight flight = getFlight(id);
        flight.setStatus(status);
        flightRepository.save(flight);
    }
    @Transactional
    public Flight updateFlight(Flight flight) {
        Flight existingFlight = flightRepository.findById(flight.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        // Update the existing flight
        existingFlight.setFlightNumber(flight.getFlightNumber());
        existingFlight.setSource(flight.getSource());
        existingFlight.setDestination(flight.getDestination());
        existingFlight.setDepartureTime(flight.getDepartureTime());
        existingFlight.setArrivalTime(flight.getArrivalTime());
        existingFlight.setBasePrice(flight.getBasePrice());
        existingFlight.setAvailableSeats(flight.getAvailableSeats());
        existingFlight.setStatus(flight.getStatus());

        return flightRepository.save(existingFlight);
    }

    @Transactional
    public Flight updateFlightSeats(Long flightId, Integer seatsChange) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        int newSeatCount = flight.getAvailableSeats() + seatsChange;
        if (newSeatCount < 0) {
            throw new RuntimeException("Invalid seat update: would result in negative seats");
        }

        flight.setAvailableSeats(newSeatCount);
        return flightRepository.save(flight);
    }
}


