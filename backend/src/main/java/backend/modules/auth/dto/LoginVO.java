package backend.modules.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {
    private String  token;
    private Long    userId;
    private String  username;
    private String  realName;
    /** 0=志愿者  1=活动管理员  2=超级管理员 */
    private Integer role;
}
