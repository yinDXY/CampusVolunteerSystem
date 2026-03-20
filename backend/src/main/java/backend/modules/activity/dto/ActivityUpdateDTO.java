package backend.modules.activity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityUpdateDTO {

    @NotBlank(message = "活动标题不能为空")
    private String title;

    private String description;
    private String coverUrl;
    private String location;

    @NotNull(message = "活动开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "活动结束时间不能为空")
    private LocalDateTime endTime;

    private LocalDateTime signStartTime;
    private LocalDateTime signEndTime;

    @NotNull(message = "招募人数不能为空")
    @Min(value = 1, message = "招募人数至少为1人")
    private Integer totalQuota;
}
