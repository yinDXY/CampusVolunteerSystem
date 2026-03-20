package backend.modules.position.service.impl;

import backend.common.exception.AppException;
import backend.common.result.ResultCode;
import backend.modules.activity.entity.Activity;
import backend.modules.activity.mapper.ActivityMapper;
import backend.modules.position.dto.PositionCreateDTO;
import backend.modules.position.dto.PositionUpdateDTO;
import backend.modules.position.dto.PositionVO;
import backend.modules.position.entity.Position;
import backend.modules.position.mapper.PositionMapper;
import backend.modules.position.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionMapper positionMapper;
    private final ActivityMapper activityMapper;

    @Override
    @Transactional
    public Long createPosition(PositionCreateDTO dto, Long operatorId, Integer operatorRole) {
        Activity activity = activityMapper.selectById(dto.getActivityId());
        if (activity == null) throw new AppException(ResultCode.ACTIVITY_NOT_FOUND);
        checkOwnerOrAdmin(activity.getCreatorId(), operatorId, operatorRole);

        Position position = Position.builder()
                .activityId(dto.getActivityId())
                .name(dto.getName())
                .description(dto.getDescription())
                .quota(dto.getQuota())
                .requiredHours(dto.getRequiredHours())
                .build();
        positionMapper.insert(position);
        return position.getId();
    }

    @Override
    public List<PositionVO> listByActivity(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) throw new AppException(ResultCode.ACTIVITY_NOT_FOUND);
        return positionMapper.selectByActivityId(activityId);
    }

    @Override
    @Transactional
    public void updatePosition(Long id, PositionUpdateDTO dto, Long operatorId, Integer operatorRole) {
        Position existing = positionMapper.selectById(id);
        if (existing == null) throw new AppException(ResultCode.POSITION_NOT_FOUND);

        Activity activity = activityMapper.selectById(existing.getActivityId());
        if (activity == null) throw new AppException(ResultCode.ACTIVITY_NOT_FOUND);
        checkOwnerOrAdmin(activity.getCreatorId(), operatorId, operatorRole);

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setQuota(dto.getQuota());
        existing.setRequiredHours(dto.getRequiredHours());
        positionMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void deletePosition(Long id, Long operatorId, Integer operatorRole) {
        Position existing = positionMapper.selectById(id);
        if (existing == null) throw new AppException(ResultCode.POSITION_NOT_FOUND);

        Activity activity = activityMapper.selectById(existing.getActivityId());
        if (activity == null) throw new AppException(ResultCode.ACTIVITY_NOT_FOUND);
        checkOwnerOrAdmin(activity.getCreatorId(), operatorId, operatorRole);

        positionMapper.deleteById(id);
    }

    private void checkOwnerOrAdmin(Long creatorId, Long operatorId, Integer operatorRole) {
        if (operatorRole != 2 && !creatorId.equals(operatorId)) {
            throw new AppException(ResultCode.FORBIDDEN);
        }
    }
}
