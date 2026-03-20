package backend.modules.activity.service;

import backend.modules.activity.dto.*;
import backend.common.result.PageResult;

public interface ActivityService {

    PageResult<ActivityVO> getPage(ActivityQueryDTO query);

    ActivityVO getById(Long id);

    Long createActivity(ActivityCreateDTO dto, Long creatorId);

    void updateActivity(Long id, ActivityUpdateDTO dto, Long operatorId, Integer operatorRole);

    void deleteActivity(Long id, Long operatorId, Integer operatorRole);

    void updateStatus(Long id, Integer status, Long operatorId, Integer operatorRole);
}
