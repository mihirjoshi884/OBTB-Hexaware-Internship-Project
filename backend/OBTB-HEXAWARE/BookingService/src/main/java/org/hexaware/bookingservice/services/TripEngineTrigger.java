package org.hexaware.bookingservice.services;

public interface TripEngineTrigger {

//    public void onStartup();
    public void periodicSync();
    public void dailyRegulation();
}
