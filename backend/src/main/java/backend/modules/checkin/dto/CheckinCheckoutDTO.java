package backend.modules.checkin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckinCheckoutDTO {

    @NotNull(message = "活动ID不能为空")
    private Long activityId;
}
