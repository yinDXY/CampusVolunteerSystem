package backend.modules.registration.controller;

import backend.common.result.PageResult;
import backend.common.result.R;
import backend.modules.registration.dto.*;
import backend.modules.registration.service.RegistrationService;
import backend.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    /**
     * 志愿者提交报名
     * POST /api/registrations
     * 权限：志愿者
     */
    @PostMapping
    @PreAuthorize("hasRole('VOLUNTEER')")
    public R<Long> apply(@Valid @RequestBody ApplyDTO dto) {
        return R.ok(registrationService.apply(dto, SecurityUtil.getCurrentUserId()));
    }

    /**
     * 取消报名
     * DELETE /api/registrations/{id}
     * 权限：报名本人
     */
    @DeleteMapping("/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        registrationService.cancel(id, SecurityUtil.getCurrentUserId());
        return R.ok();
    }

    /**
     * 我的报名列表（分页）
     * GET /api/registrations/my?pageNum=1&pageSize=10&status=0
     * 权限：志愿者
     */
    @GetMapping("/my")
    public R<PageResult<RegistrationVO>> myList(RegistrationQueryDTO query) {
        return R.ok(registrationService.myList(query, SecurityUtil.getCurrentUserId()));
    }

    /**
     * 管理员查看某活动的报名列表（分页）
     * GET /api/registrations/activity/{activityId}?pageNum=1&pageSize=10&status=0
     * 权限：活动管理员、超级管理员
     */
    @GetMapping("/activity/{activityId}")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<PageResult<RegistrationVO>> listByActivity(@PathVariable Long activityId,
                                                         RegistrationQueryDTO query) {
        return R.ok(registrationService.listByActivity(activityId, query,
                SecurityUtil.getCurrentUserRole()));
    }

    /**
     * 审核报名（通过/拒绝）
     * PATCH /api/registrations/{id}/status
     * 权限：活动管理员、超级管理员
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<Void> updateStatus(@PathVariable Long id,
                                @Valid @RequestBody StatusUpdateDTO dto) {
        registrationService.updateStatus(id, dto, SecurityUtil.getCurrentUserRole());
        return R.ok();
    }

    /**
     * 给志愿者打分
     * PATCH /api/registrations/{id}/score
     * 权限：活动管理员、超级管理员
     */
    @PatchMapping("/{id}/score")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<Void> score(@PathVariable Long id,
                         @Valid @RequestBody ScoreDTO dto) {
        registrationService.score(id, dto, SecurityUtil.getCurrentUserRole());
        return R.ok();
    }
}
