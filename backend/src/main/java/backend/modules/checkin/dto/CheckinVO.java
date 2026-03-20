package backend.modules.checkin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CheckinVO {
    private Long id;
    private Long activityId;
    private String activityTitle;
    private Long userId;
    private String username;
    private String realName;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private BigDecimal durationHours;
    /** 0=准时 1=迟到 */
    private Integer isLate;
}
