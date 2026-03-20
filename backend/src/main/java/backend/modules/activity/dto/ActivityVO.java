package backend.modules.activity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 活动列表/详情返回 VO */
@Data
public class ActivityVO {
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
    private Integer status;
    private Long creatorId;
    private String creatorName;   // JOIN sys_user.real_name
    private LocalDateTime createdAt;
}
