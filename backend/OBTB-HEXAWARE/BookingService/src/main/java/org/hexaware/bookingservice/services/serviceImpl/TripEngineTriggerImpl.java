package org.hexaware.bookingservice.services.serviceImpl;

import org.hexaware.bookingservice.services.TripEngineTrigger;
import org.hexaware.bookingservice.services.TripLifecycleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TripEngineTriggerImpl implements TripEngineTrigger {

    @Autowired
    private TripLifecycleEngine engine;

    @Override
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        System.out.println("Triggering Trip Engine: Application Startup Sync...");
        engine.processLifecycle();
    }

    @Override
    @Scheduled(fixedRate = 3600000)
    public void periodicSync() {
        engine.processLifecycle();
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    public void dailyRegulation() {
        System.out.println("Triggering Trip Engine: Midnight Regulation...");
        engine.processLifecycle();
    }
}
