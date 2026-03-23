package backend.modules.profile.dto;

import lombok.Data;

/**
 * Flask /radar 端点响应体
 * {"code": 200, "imageBase64": "iVBORw0KGgo..."}
 */
@Data
public class FlaskRadarResponse {
    private Integer code;
    private String imageBase64;
}
