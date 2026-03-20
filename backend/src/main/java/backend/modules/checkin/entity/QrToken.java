package backend.modules.checkin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrToken {
    private Long id;
    private Long activityId;
    /** 每分钟刷新的 UUID 令牌 */
    private String token;
    /** 过期时间（通常为创建时间 + 1 分钟） */
    private LocalDateTime expireTime;
    private LocalDateTime createdAt;
}
