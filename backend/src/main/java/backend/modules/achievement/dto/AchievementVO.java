package backend.modules.achievement.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 成就定义 VO
 * 用于 GET /api/achievements 响应（含是否已解锁）
 */
@Data
public class AchievementVO {

    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    /** 勋章等级：1=铜 2=银 3=金 */
    private Integer badgeLevel;
    /** 解锁所需时长（小时）*/
    private BigDecimal requiredHours;
    /**
     * 当前登录用户是否已解锁（非志愿者角色始终为 false）
     * 由 Service 层通过查询 user_achievement 表填充
     */
    private Boolean unlocked;
}
