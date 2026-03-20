package backend.modules.registration.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 报名列表/详情返回 VO */
@Data
public class RegistrationVO {
    private Long id;
    private Long activityId;
    private String activityTitle;
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
    private LocalDateTime createdAt;
}
