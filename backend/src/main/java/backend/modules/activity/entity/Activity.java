package backend.modules.activity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime signStartTime;
    private LocalDateTime signEndTime;
    private Integer totalQuota;
    private Integer signedCount;
    /**
     * 0=草稿 1=报名中 2=进行中 3=已结束 4=已取消
     */
    private Integer status;
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
