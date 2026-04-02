package org.hexaware.bookingservice.services;

import org.hexaware.bookingservice.entites.TripInstance;

public interface ArchiveService {
    public void attemptArchive(TripInstance ti);
}
