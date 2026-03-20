package backend.modules.registration.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Registration {
    private Long id;
    private Long activityId;
    private Long userId;
    /** 派岗后填入，报名时为 null */
    private Long positionId;
    /**
     * 0=待审核 1=已通过 2=已拒绝 3=已派岗 4=已取消
     */
    private Integer status;
    private String remark;
    /** 活动方对志愿者的评分（1-5） */
    private Integer score;
    /** 志愿者对活动的评分（1-5） */
    private Integer activityScore;
    /** 志愿者对活动的文字评价 */
    private String activityComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
