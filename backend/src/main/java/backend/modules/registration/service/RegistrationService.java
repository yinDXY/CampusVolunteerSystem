package backend.modules.registration.service;

import backend.common.result.PageResult;
import backend.modules.registration.dto.*;

public interface RegistrationService {

    /** 志愿者提交报名 */
    Long apply(ApplyDTO dto, Long userId);

    /** 志愿者取消报名 */
    void cancel(Long id, Long userId);

    /** 志愿者查询自己的报名列表 */
    PageResult<RegistrationVO> myList(RegistrationQueryDTO query, Long userId);

    /** 管理员查询某活动的报名列表 */
    PageResult<RegistrationVO> listByActivity(Long activityId, RegistrationQueryDTO query,
                                               Integer operatorRole);

    /** 管理员审核报名（通过/拒绝） */
    void updateStatus(Long id, StatusUpdateDTO dto, Integer operatorRole);

    /** 管理员给志愿者打分 */
    void score(Long id, ScoreDTO dto, Integer operatorRole);
}
