package backend.modules.registration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateDTO {

    /** 1=通过 2=拒绝 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
