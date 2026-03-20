package backend.modules.activity.service.impl;

import backend.common.exception.AppException;
import backend.common.result.PageResult;
import backend.common.result.ResultCode;
import backend.modules.activity.dto.*;
import backend.modules.activity.entity.Activity;
import backend.modules.activity.mapper.ActivityMapper;
import backend.modules.activity.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;

    @Override
    public PageResult<ActivityVO> getPage(ActivityQueryDTO query) {
        long total = activityMapper.countPage(query);
        if (total == 0) return PageResult.of(0, List.of());
        List<ActivityVO> list = activityMapper.selectPage(query);
        return PageResult.of(total, list);
    }

    @Override
    public ActivityVO getById(Long id) {
        ActivityVO vo = activityMapper.selectVOById(id);
        if (vo == null) throw new AppException(ResultCode.ACTIVITY_NOT_FOUND);
        return vo;
    }

    @Override
    @Transactional
    public Long createActivity(ActivityCreateDTO dto, Long creatorId) {
        Activity activity = Activity.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .coverUrl(dto.getCoverUrl())
                .location(dto.getLocation())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .signStartTime(dto.getSignStartTime())
                .signEndTime(dto.getSignEndTime())
                .totalQuota(dto.getTotalQuota())
                .status(0)          // 初始为草稿
                .creatorId(creatorId)
                .build();
        activityMapper.insert(activity);
        return activity.getId();
    }

    @Override
    @Transactional
    public void updateActivity(Long id, ActivityUpdateDTO dto, Long operatorId, Integer operatorRole) {
        Activity existing = activityMapper.selectById(id);
        if (existing == null) throw new AppException(ResultCode.ACTIVITY_NOT_FOUND);
        checkOwnerOrAdmin(existing.getCreatorId(), operatorId, operatorRole);

        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setCoverUrl(dto.getCoverUrl());
        existing.setLocation(dto.getLocation());
        existing.setStartTime(dto.getStartTime());
        existing.setEndTime(dto.getEndTime());
        existing.setSignStartTime(dto.getSignStartTime());
        existing.setSignEndTime(dto.getSignEndTime());
        existing.setTotalQuota(dto.getTotalQuota());
        activityMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void deleteActivity(Long id, Long operatorId, Integer operatorRole) {
        Activity existing = activityMapper.selectById(id);
        if (existing == null) throw new AppException(ResultCode.ACTIVITY_NOT_FOUND);
        checkOwnerOrAdmin(existing.getCreatorId(), operatorId, operatorRole);
        activityMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status, Long operatorId, Integer operatorRole) {
        Activity existing = activityMapper.selectById(id);
        if (existing == null) throw new AppException(ResultCode.ACTIVITY_NOT_FOUND);
        checkOwnerOrAdmin(existing.getCreatorId(), operatorId, operatorRole);
        activityMapper.updateStatus(id, status);
    }

    /** 仅活动创建者或超级管理员可操作 */
    private void checkOwnerOrAdmin(Long creatorId, Long operatorId, Integer operatorRole) {
        if (operatorRole != 2 && !creatorId.equals(operatorId)) {
            throw new AppException(ResultCode.FORBIDDEN);
        }
    }
}
