package backend.modules.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckinScanDTO {

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotBlank(message = "令牌不能为空")
    private String token;
}
