package backend.modules.achievement.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 志愿者已解锁成就 VO
 * 用于 GET /api/achievements/my 响应
 */
@Data
public class UserAchievementVO {

    private Long achievementId;
    private String name;
    private String description;
    private String iconUrl;
    /** 勋章等级：1=铜 2=银 3=金 */
    private Integer badgeLevel;
    /** 解锁所需时长（小时），用于前端进度条展示 */
    private BigDecimal requiredHours;
    /** 解锁时间 */
    private LocalDateTime unlockedAt;
}
