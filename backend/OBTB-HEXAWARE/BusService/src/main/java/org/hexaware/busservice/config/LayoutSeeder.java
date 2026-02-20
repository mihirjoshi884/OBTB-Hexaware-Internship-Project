package org.hexaware.busservice.config;

import org.hexaware.busservice.entities.LayoutTemplate;
import org.hexaware.busservice.enums.BusType;
import org.hexaware.busservice.repositories.LayoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LayoutSeeder implements CommandLineRunner {

    @Autowired
    LayoutRepository layoutRepository;

    @Override
    public void run(String... args) throws Exception {
        if (layoutRepository.count() == 0) {
            List<LayoutTemplate> layouts = new ArrayList<>();
            System.out.println("Creating default layout templates with descriptions...");

            // --- SINGLE DECKER PERMUTATIONS ---
            layouts.add(createLayout("Standard 2+2 Seater",
                    "Classic 4-column seating with a central aisle. Ideal for short to medium distance travel with high passenger capacity.",
                    5, 2, BusType.NON_AC_SEATER, false));

            layouts.add(createLayout("Luxury 1+2 Sleeper",
                    "Premium sleeper configuration with single berths on one side and double on the other. Provides maximum comfort for overnight journeys.",
                    4, 1, BusType.NON_AC_SLEEPER, false));

            layouts.add(createLayout("Executive 2+1 Hybrid",
                    "Spacious semi-sleeper/seater layout with 2 seats on the right and 1 on the left. Offers extra legroom and elbow space for business travel.",
                    4, 2, BusType.NON_AC_HYBRID, false));

            // --- DOUBLE DECKER PERMUTATIONS ---
            layouts.add(createLayout("Double Decker 2+2 Seater",
                    "High-capacity twin-deck seating. Optimized for busy inter-city routes needing to transport large groups comfortably.",
                    5, 2, BusType.NON_AC_SEATER, true));

            layouts.add(createLayout("Double Decker 1+2 Sleeper",
                    "Full sleeper berths on both decks. A 1+2 configuration ensures easy aisle access while maintaining high bed counts.",
                    4, 1, BusType.NON_AC_SLEEPER, true));

            layouts.add(createLayout("Double Decker 2+2 Hybrid",
                    "Versatile twin-deck configuration typically featuring Seating on the lower deck and Sleeper berths on the upper deck.",
                    5, 2, BusType.NON_AC_HYBRID, true));

            layoutRepository.saveAll(layouts);
            System.out.println("Default layouts successfully seeded.");
        }
    }

    // Helper method to keep your seeder clean
    private LayoutTemplate createLayout(String name, String desc,int cols, int aisle, BusType type, boolean isDouble) {
        LayoutTemplate lt = new LayoutTemplate();
        lt.setName(name);
        lt.setDescription(desc);
        lt.setColumns(cols);
        lt.setAisleAfterColumn(aisle);
        lt.setDefaultType(type);
        lt.setIsDoubleDecker(isDouble);
        return lt;
    }
}
