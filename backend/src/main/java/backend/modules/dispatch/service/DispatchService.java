package backend.modules.dispatch.service;

import backend.modules.dispatch.dto.DispatchRecommendVO;

public interface DispatchService {

    /**
     * 智能派岗推荐：返回该岗位的候选志愿者列表及标签匹配分。
     *
     * @param positionId 岗位ID
     * @return 推荐结果（含岗位信息 + 候选人列表，按 matchScore/totalHours 降序）
     */
    DispatchRecommendVO recommend(Long positionId);

    /**
     * 确认派岗：将报名记录状态改为 3（已派岗），绑定岗位ID。
     * 由触发器 trg_registration_assign_position 自动维护 position.assigned_count++。
     *
     * @param registrationId 报名记录ID
     * @param positionId     目标岗位ID
     */
    void assign(Long registrationId, Long positionId);

    /**
     * 取消派岗：将报名记录状态回退至 1（已通过），清除岗位绑定。
     * 由触发器自动维护 position.assigned_count--。
     *
     * @param registrationId 报名记录ID
     */
    void cancelAssign(Long registrationId);
}
