package backend.modules.stats.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityHeatVO {
    private Long activityId;
    private String title;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer activityStatus;
    private Integer totalQuota;
    private Integer signedCount;
    private Integer checkinCount;
    private Integer positionTotalQuota;
    private Integer positionCount;
    private String activityTags;
    private BigDecimal avgActivityScore;
    private Integer commentCount;
    private Long creatorId;
    private String creatorName;
}
