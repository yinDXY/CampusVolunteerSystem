package backend.modules.profile.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 志愿者画像响应 VO
 * 数据来源：v_volunteer_profile 视图 + Flask 雷达图微服务
 */
@Data
public class ProfileVO {

    private Long userId;
    private String realName;
    private String studentId;
    private String avatarUrl;

    /** 累计志愿时长（小时）*/
    private BigDecimal totalHours;
    /** 参与活动场次 */
    private Integer activityCount;
    /** 活动类型多样性（种数）*/
    private Integer typeDiversity;
    /** 历史平均评分（0-5）*/
    private BigDecimal avgScore;
    /** 准时率（0-100）*/
    private BigDecimal punctualityRate;

    /** 已解锁勋章数 */
    private Integer badgeCount;
    /** 技能标签（逗号分隔）*/
    private String skillTags;

    /** Flask 生成的雷达图，格式：data:image/png;base64,... */
    private String radarImageBase64;
}
