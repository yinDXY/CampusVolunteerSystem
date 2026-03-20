package backend.modules.dispatch.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignDTO {

    @NotNull(message = "报名ID不能为空")
    private Long registrationId;

    @NotNull(message = "岗位ID不能为空")
    private Long positionId;
}
