package backend.modules.position.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionCreateDTO {

    /** 由路径参数注入，不在请求体校验 */
    private Long activityId;

    @NotBlank(message = "岗位名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "招募人数不能为空")
    @Min(value = 1, message = "招募人数至少为1")
    private Integer quota;

    private BigDecimal requiredHours;
}
