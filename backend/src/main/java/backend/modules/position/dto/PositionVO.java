package backend.modules.position.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 岗位列表/详情返回 VO */
@Data
public class PositionVO {
    private Long id;
    private Long activityId;
    private String name;
    private String description;
    private Integer quota;
    private Integer assignedCount;
    private BigDecimal requiredHours;
    private LocalDateTime createdAt;
}
