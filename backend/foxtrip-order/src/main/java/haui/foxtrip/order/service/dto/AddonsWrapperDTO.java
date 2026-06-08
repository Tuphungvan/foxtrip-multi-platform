package haui.foxtrip.order.service.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AddonsWrapperDTO {
    
    @Valid
    private List<AddonItemDTO> items = new ArrayList<>();
}
