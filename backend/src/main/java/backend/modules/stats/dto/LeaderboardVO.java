package backend.modules.stats.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LeaderboardVO {
    private Integer rankNo;
    private Long userId;
    private String realName;
    private String avatarUrl;
    private BigDecimal totalHours;
    private Integer activityCount;
    private LocalDateTime refreshedAt;
}
