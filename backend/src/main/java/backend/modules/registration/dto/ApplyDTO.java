package backend.modules.registration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplyDTO {

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    /** 志愿者报名留言（选填） */
    private String remark;
}
