package org.hexaware.bookingservice.dtos.instanceDtos;



import org.hexaware.bookingservice.dtos.busDtos.SeatLayout;

import java.util.List;
import java.util.UUID;

public record SeatMappingDto(
        UUID instanceId,
        List<EnrichedSeatDto> seats

) { }
