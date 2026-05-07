package backend.modules.registration.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 报名列表/详情返回 VO */
@Data
public class RegistrationVO {
    private Long id;
    private Long activityId;
    private String activityTitle;
    /** 所属活动当前状态：0=草稿 1=报名中 2=进行中 3=已结束 4=已取消 */
    private Integer activityStatus;
    private Long userId;
    private String username;
    private String realName;
    /** 派岗后才有值 */
    private Long positionId;
    private String positionName;
    /**
     * 0=待审核 1=已通过 2=已拒绝 3=已派岗 4=已取消
     */
    private Integer status;
    private String remark;
    /** 活动方对志愿者的评分 */
    private Integer score;
    /** 签到时长（小时，签退后才有值） */
    private BigDecimal durationHours;
    private LocalDateTime createdAt;
}
