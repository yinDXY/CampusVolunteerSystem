package backend.modules.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUser {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String studentId;
    private String phone;
    private String email;
    private String avatarUrl;
    /** 0=志愿者  1=活动管理员  2=超级管理员 */
    private Integer role;
    private BigDecimal totalHours;
    /** 1=正常  0=禁用 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
