package org.hexaware.bookingservice.services.serviceImpl;


import org.hexaware.bookingservice.entites.TripArchive;
import org.hexaware.bookingservice.entites.TripInstance;
import org.hexaware.bookingservice.repositories.ArchiveRepository;
import org.hexaware.bookingservice.services.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ArchiveServiceImpl implements ArchiveService {

    @Autowired
    private ArchiveRepository archiveRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void attemptArchive(TripInstance ti) {
        if (!archiveRepository.existsById(ti.getInstanceId())) {
            TripArchive archive = new TripArchive();
            archive.setInstanceId(ti.getInstanceId());
            archive.setTemplateId(ti.getTemplate().getTemplateId());
            archive.setActualDeparture(ti.getActualDeparture());
            archive.setActualArrival(ti.getActualArrival());
            archive.setFinalFare(ti.getTemplate().getBaseFare());
            archive.setArchivedAt(LocalDateTime.now());

            try {
                archiveRepository.saveAndFlush(archive);
            } catch (DataIntegrityViolationException e) {
                System.out.println("Archive write failed due to race condition for instance: " + ti.getInstanceId());
            }
        } else {
            System.out.println("Archive already exists for instance: " + ti.getInstanceId() + ". Skipping insert.");
        }
    }
}
