package backend.modules.position.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionUpdateDTO {

    @NotBlank(message = "岗位名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "招募人数不能为空")
    @Min(value = 1, message = "招募人数至少为1")
    private Integer quota;

    private BigDecimal requiredHours;

    /** 岗位需求技能标签名称列表，null 表示不修改，空列表表示清空 */
    private java.util.List<String> requirements;
}
