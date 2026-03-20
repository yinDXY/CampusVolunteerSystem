package backend.modules.checkin.service;

import backend.modules.checkin.dto.CheckinVO;

import java.util.List;

public interface CheckinService {

    /** 志愿者扫码签到：校验令牌 → 校验报名状态 → 写入签到记录 */
    void scan(Long activityId, String token);

    /** 志愿者签退：计算时长 → 更新记录（触发器自动累加 total_hours） */
    void checkout(Long activityId);

    /** 管理员查看活动全量签到列表 */
    List<CheckinVO> listByActivity(Long activityId);
}
