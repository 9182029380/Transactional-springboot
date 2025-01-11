package com.example.airline.controller;

import com.example.airline.dto.FlightDTO;
import com.example.airline.entity.Flight;
import com.example.airline.service.FlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @PostMapping
    public ResponseEntity<Flight> addFlight(@RequestBody FlightDTO flightDTO) {
        return ResponseEntity.ok(flightService.addFlight(flightDTO));
    }

    @GetMapping
    public ResponseEntity<List<Flight>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllFlights());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Flight> getFlight(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlight(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateFlightStatus(
            @PathVariable Long id,
            @RequestParam Flight.FlightStatus status
    ) {
        flightService.updateFlightStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
