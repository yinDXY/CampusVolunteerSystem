package backend.modules.checkin.service.impl;

import backend.common.exception.AppException;
import backend.common.result.ResultCode;
import backend.modules.activity.entity.Activity;
import backend.modules.activity.mapper.ActivityMapper;
import backend.modules.checkin.dto.CheckinVO;
import backend.modules.checkin.entity.Checkin;
import backend.modules.checkin.entity.QrToken;
import backend.modules.checkin.mapper.CheckinMapper;
import backend.modules.checkin.mapper.QrTokenMapper;
import backend.modules.checkin.service.CheckinService;
import backend.modules.registration.entity.Registration;
import backend.modules.registration.mapper.RegistrationMapper;
import backend.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final CheckinMapper checkinMapper;
    private final QrTokenMapper qrTokenMapper;
    private final RegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;

    @Override
    @Transactional
    public void scan(Long activityId, String token) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 1. 校验二维码令牌：令牌匹配 且 未过期
        QrToken qrToken = qrTokenMapper.selectByActivityId(activityId);
        if (qrToken == null
                || !qrToken.getToken().equals(token)
                || LocalDateTime.now().isAfter(qrToken.getExpireTime())) {
            throw new AppException(ResultCode.QR_TOKEN_INVALID);
        }

        // 2. 校验报名状态：status=1(已通过) 或 status=3(已派岗) 才允许签到
        Registration reg = registrationMapper.selectByActivityAndUser(activityId, userId);
        if (reg == null || (reg.getStatus() != 1 && reg.getStatus() != 3)) {
            throw new AppException(ResultCode.REGISTRATION_NOT_APPROVED);
        }

        // 3. 防重复签到
        Checkin existing = checkinMapper.selectByActivityAndUser(activityId, userId);
        if (existing != null) {
            throw new AppException(ResultCode.ALREADY_CHECKED_IN);
        }

        // 4. 计算是否迟到
        Activity activity = activityMapper.selectById(activityId);
        LocalDateTime now = LocalDateTime.now();
        int isLate = (activity != null && now.isAfter(activity.getStartTime())) ? 1 : 0;

        // 5. 插入签到记录
        Checkin checkin = Checkin.builder()
                .activityId(activityId)
                .userId(userId)
                .registrationId(reg.getId())
                .checkinTime(now)
                .qrToken(token)
                .isLate(isLate)
                .build();
        checkinMapper.insert(checkin);
    }

    @Override
    @Transactional
    public void checkout(Long activityId) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 1. 查找签到记录
        Checkin checkin = checkinMapper.selectByActivityAndUser(activityId, userId);
        if (checkin == null) {
            throw new AppException(ResultCode.CHECKIN_NOT_FOUND);
        }

        // 2. 防重复签退
        if (checkin.getCheckoutTime() != null) {
            throw new AppException(ResultCode.ALREADY_CHECKED_OUT);
        }

        // 3. 计算时长（分钟 → 小时，保留两位小数）
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(checkin.getCheckinTime(), now);
        // 至少记录 0.01 小时，避免极短时间导致 0
        BigDecimal durationHours = BigDecimal.valueOf(Math.max(minutes, 1))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        // 4. 更新签退信息；触发器 trg_checkin_checkout_update 自动累加 sys_user.total_hours
        checkinMapper.updateCheckout(checkin.getId(), now, durationHours);
    }

    @Override
    public List<CheckinVO> listByActivity(Long activityId) {
        return checkinMapper.selectListByActivity(activityId);
    }
}
