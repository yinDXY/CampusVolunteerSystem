package backend.modules.registration.service.impl;

import backend.common.exception.AppException;
import backend.common.result.PageResult;
import backend.common.result.ResultCode;
import backend.modules.activity.entity.Activity;
import backend.modules.activity.mapper.ActivityMapper;
import backend.modules.registration.dto.*;
import backend.modules.registration.entity.Registration;
import backend.modules.registration.mapper.RegistrationMapper;
import backend.modules.registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;

    @Override
    @Transactional
    public Long apply(ApplyDTO dto, Long userId) {
        Activity activity = activityMapper.selectById(dto.getActivityId());
        if (activity == null) throw new AppException(ResultCode.ACTIVITY_NOT_FOUND);

        // 活动须处于报名中状态（status=1）
        if (activity.getStatus() != 1) throw new AppException(ResultCode.ACTIVITY_STATUS_ERROR);

        // 检查名额（冗余字段快速判断）
        if (activity.getSignedCount() >= activity.getTotalQuota()) {
            throw new AppException(ResultCode.ACTIVITY_FULL);
        }

        // 防重复报名：查询历史记录
        Registration existing = registrationMapper.selectByActivityAndUser(dto.getActivityId(), userId);
        if (existing != null) {
            if (existing.getStatus() != 4) {
                // 非已取消状态，视为重复报名
                throw new AppException(ResultCode.ALREADY_REGISTERED);
            }
            // 已取消 → 复用记录，UPDATE 重置为待审核，避免触发唯一键冲突
            // signed_count+1 由 trg_registration_status_update 触发器完成（status: 4→0 时自动+1）
            registrationMapper.reApply(existing.getId(), dto.getRemark());
            return existing.getId();
        }

        Registration registration = Registration.builder()
                .activityId(dto.getActivityId())
                .userId(userId)
                .status(0)
                .remark(dto.getRemark())
                .build();
        registrationMapper.insert(registration);
        // signed_count +1 由触发器 trg_registration_insert 完成
        return registration.getId();
    }

    @Override
    @Transactional
    public void cancel(Long id, Long userId) {
        Registration registration = registrationMapper.selectById(id);
        if (registration == null) throw new AppException(ResultCode.REGISTRATION_NOT_FOUND);

        // 只能取消自己的报名
        if (!registration.getUserId().equals(userId)) {
            throw new AppException(ResultCode.FORBIDDEN);
        }
        // 只有待审核(0)或已通过(1)状态可以取消
        if (registration.getStatus() != 0 && registration.getStatus() != 1) {
            throw new AppException(ResultCode.ACTIVITY_STATUS_ERROR);
        }
        registrationMapper.updateStatus(id, 4);
    }

    @Override
    public PageResult<RegistrationVO> myList(RegistrationQueryDTO query, Long userId) {
        long total = registrationMapper.countPageByUser(userId, query);
        if (total == 0) return PageResult.of(0, List.of());
        List<RegistrationVO> list = registrationMapper.selectPageByUser(userId, query);
        return PageResult.of(total, list);
    }

    @Override
    public PageResult<RegistrationVO> listByActivity(Long activityId, RegistrationQueryDTO query,
                                                      Integer operatorRole) {
        if (operatorRole < 1) throw new AppException(ResultCode.FORBIDDEN);
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) throw new AppException(ResultCode.ACTIVITY_NOT_FOUND);

        long total = registrationMapper.countPageByActivity(activityId, query);
        if (total == 0) return PageResult.of(0, List.of());
        List<RegistrationVO> list = registrationMapper.selectPageByActivity(activityId, query);
        return PageResult.of(total, list);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, StatusUpdateDTO dto, Integer operatorRole) {
        if (operatorRole < 1) throw new AppException(ResultCode.FORBIDDEN);

        Registration registration = registrationMapper.selectById(id);
        if (registration == null) throw new AppException(ResultCode.REGISTRATION_NOT_FOUND);

        // 只能对待审核(0)记录进行审核
        if (registration.getStatus() != 0) throw new AppException(ResultCode.ACTIVITY_STATUS_ERROR);

        // 仅允许设为 1=通过 或 2=拒绝
        Integer newStatus = dto.getStatus();
        if (newStatus != 1 && newStatus != 2) throw new AppException(ResultCode.BAD_REQUEST);

        registrationMapper.updateStatus(id, newStatus);
    }

    @Override
    @Transactional
    public void score(Long id, ScoreDTO dto, Integer operatorRole) {
        if (operatorRole < 1) throw new AppException(ResultCode.FORBIDDEN);

        Registration registration = registrationMapper.selectById(id);
        if (registration == null) throw new AppException(ResultCode.REGISTRATION_NOT_FOUND);

        registrationMapper.updateScore(id, dto.getScore());
    }
}
