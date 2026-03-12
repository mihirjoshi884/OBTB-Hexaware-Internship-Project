package org.hexaware.bookingservice.services;

import org.hexaware.bookingservice.entites.TripInstance;
import org.hexaware.bookingservice.entites.TripTemplate;

import java.time.LocalDate;
import java.time.LocalTime;

public interface TripLifecycleEngine {

    public void processLifecycle();
    public TripInstance instantiate(TripTemplate template, LocalTime time);
}
