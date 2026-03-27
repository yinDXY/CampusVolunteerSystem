package backend.modules.dispatch.service.impl;

import backend.common.exception.AppException;
import backend.common.result.ResultCode;
import backend.modules.dispatch.dto.DispatchCandidateVO;
import backend.modules.dispatch.dto.DispatchRecommendVO;
import backend.modules.dispatch.mapper.DispatchMapper;
import backend.modules.dispatch.service.DispatchService;
import backend.modules.position.entity.Position;
import backend.modules.position.mapper.PositionMapper;
import backend.modules.registration.entity.Registration;
import backend.modules.registration.mapper.RegistrationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DispatchServiceImpl implements DispatchService {

    private final DispatchMapper dispatchMapper;
    private final PositionMapper positionMapper;
    private final RegistrationMapper registrationMapper;

    @Override
    public DispatchRecommendVO recommend(Long positionId) {
        DispatchRecommendVO vo = dispatchMapper.selectPositionInfo(positionId);
        if (vo == null) throw new AppException(ResultCode.POSITION_NOT_FOUND);

        // 补充剩余名额
        vo.setRemainingQuota(Math.max(0, vo.getQuota() - vo.getAssignedCount()));

        List<DispatchCandidateVO> candidates = dispatchMapper.selectCandidates(positionId);

        // 计算 qualified 标志并统计合格人数
        //   qualified 定义：岗位无需求标签(requiredCount=0) 或 所有需求标签全部命中
        int qualifiedCount = 0;
        for (DispatchCandidateVO c : candidates) {
            boolean q = c.getRequiredCount() == 0
                    || c.getMatchedCount().equals(c.getRequiredCount());
            c.setQualified(q);
            if (q) qualifiedCount++;
        }

        vo.setCandidates(candidates);
        vo.setQualifiedCount(qualifiedCount);
        return vo;
    }

    @Override
    @Transactional
    public void assign(Long registrationId, Long positionId) {
        // 1. 验证岗位存在且有剩余名额
        Position position = positionMapper.selectById(positionId);
        if (position == null) throw new AppException(ResultCode.POSITION_NOT_FOUND);
        if (position.getAssignedCount() >= position.getQuota()) {
            throw new AppException(ResultCode.POSITION_QUOTA_FULL);
        }

        // 2. 验证报名记录存在且状态合法（仅已通过=1 可派岗）
        Registration reg = registrationMapper.selectById(registrationId);
        if (reg == null) throw new AppException(ResultCode.REGISTRATION_NOT_FOUND);
        if (reg.getStatus() == 3) throw new AppException(ResultCode.ALREADY_ASSIGNED);
        if (reg.getStatus() != 1) {
            throw new AppException(ResultCode.ACTIVITY_STATUS_ERROR);
        }

        // 3. 确认报名所属活动与岗位所属活动一致
        if (!reg.getActivityId().equals(position.getActivityId())) {
            throw new AppException(ResultCode.BAD_REQUEST);
        }

        // 4. 执行派岗；触发器 trg_registration_assign_position 自动更新 position.assigned_count++
        registrationMapper.assignPosition(registrationId, positionId);
    }

    @Override
    @Transactional
    public void cancelAssign(Long registrationId) {
        Registration reg = registrationMapper.selectById(registrationId);
        if (reg == null) throw new AppException(ResultCode.REGISTRATION_NOT_FOUND);
        if (reg.getStatus() != 3) {
            // 未派岗状态无法取消
            throw new AppException(ResultCode.ACTIVITY_STATUS_ERROR);
        }
        // 触发器 trg_registration_assign_position 自动更新 position.assigned_count--
        registrationMapper.cancelAssign(registrationId);
    }
}
