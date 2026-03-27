package backend.modules.stats.dto;

import lombok.Data;

@Data
public class TrendVO {
    /** 月份（1-12）*/
    private Integer month;
    /** 当月完成签退的唯一志愿者人次 */
    private Integer participantCount;
    /** 当月有签到记录的活动数 */
    private Integer activityCount;
}
