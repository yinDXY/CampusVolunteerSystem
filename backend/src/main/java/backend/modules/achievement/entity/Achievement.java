package backend.modules.achievement.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Achievement {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    /** 解锁所需累计时长（小时）*/
    private BigDecimal requiredHours;
    /** 勋章等级：1=铜 2=银 3=金 */
    private Integer badgeLevel;
    private LocalDateTime createdAt;
}
