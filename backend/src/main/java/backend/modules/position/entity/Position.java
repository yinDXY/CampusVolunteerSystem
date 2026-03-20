package backend.modules.position.entity;

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
public class Position {
    private Long id;
    private Long activityId;
    private String name;
    private String description;
    /** 岗位招募人数 */
    private Integer quota;
    /** 已派岗人数（由触发器维护） */
    private Integer assignedCount;
    /** 要求的最低志愿时长（小时） */
    private BigDecimal requiredHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
