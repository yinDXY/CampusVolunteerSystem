package backend.modules.checkin.entity;

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
public class Checkin {
    private Long id;
    private Long activityId;
    private Long userId;
    private Long registrationId;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    /** 本次参与时长（小时），签退后由业务层计算写入，触发器依赖此值累加 total_hours */
    private BigDecimal durationHours;
    /** 签到时使用的二维码令牌（防重放记录） */
    private String qrToken;
    /** 是否迟到：0=准时 1=迟到 */
    private Integer isLate;
    private String hashProof;
    private LocalDateTime createdAt;
}
