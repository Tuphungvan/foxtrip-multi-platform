package haui.foxtrip.order.service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourTerminatedEvent {
    private UUID tourId;
    private String tourName;
    private boolean isEarlyTermination;
}
