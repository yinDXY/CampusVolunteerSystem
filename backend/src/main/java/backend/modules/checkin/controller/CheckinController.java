package backend.modules.checkin.controller;

import backend.common.result.R;
import backend.modules.checkin.dto.CheckinCheckoutDTO;
import backend.modules.checkin.dto.CheckinScanDTO;
import backend.modules.checkin.dto.CheckinVO;
import backend.modules.checkin.service.CheckinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    /**
     * 志愿者扫码签到
     * POST /api/checkin/scan
     * 权限：志愿者
     */
    @PostMapping("/scan")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public R<Void> scan(@Valid @RequestBody CheckinScanDTO dto) {
        checkinService.scan(dto.getActivityId(), dto.getToken());
        return R.ok();
    }

    /**
     * 志愿者签退
     * POST /api/checkin/checkout
     * 权限：志愿者
     * 签退后由触发器 trg_checkin_checkout_update 自动累加 sys_user.total_hours
     */
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public R<Void> checkout(@Valid @RequestBody CheckinCheckoutDTO dto) {
        checkinService.checkout(dto.getActivityId());
        return R.ok();
    }

    /**
     * 管理员查看活动签到列表
     * GET /api/checkin/activity/{activityId}
     * 权限：活动管理员、超级管理员
     */
    @GetMapping("/activity/{activityId}")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<List<CheckinVO>> listByActivity(@PathVariable Long activityId) {
        return R.ok(checkinService.listByActivity(activityId));
    }
}
