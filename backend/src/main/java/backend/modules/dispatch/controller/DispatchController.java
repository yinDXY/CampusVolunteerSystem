package backend.modules.dispatch.controller;

import backend.common.result.R;
import backend.modules.dispatch.dto.AssignDTO;
import backend.modules.dispatch.dto.DispatchRecommendVO;
import backend.modules.dispatch.service.DispatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    /**
     * 智能派岗推荐列表
     * GET /api/dispatch/recommend/{positionId}
     * 权限：活动管理员、超级管理员
     *
     * 返回该岗位下所有候选志愿者（同活动、待审核或已通过的报名），
     * 按标签匹配分 + 时长达标加成降序排列，完全合格者置顶。
     */
    @GetMapping("/recommend/{positionId}")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<DispatchRecommendVO> recommend(@PathVariable Long positionId) {
        return R.ok(dispatchService.recommend(positionId));
    }

    /**
     * 确认派岗
     * POST /api/dispatch/assign
     * 权限：活动管理员、超级管理员
     *
     * 将报名记录状态改为 3（已派岗）并绑定岗位，
     * 触发器自动维护 position.assigned_count++。
     */
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<Void> assign(@Valid @RequestBody AssignDTO dto) {
        dispatchService.assign(dto.getRegistrationId(), dto.getPositionId());
        return R.ok();
    }

    /**
     * 取消派岗
     * DELETE /api/dispatch/assign/{registrationId}
     * 权限：活动管理员、超级管理员
     *
     * 将报名记录状态回退至 1（已通过）并清除岗位绑定，
     * 触发器自动维护 position.assigned_count--。
     */
    @DeleteMapping("/assign/{registrationId}")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<Void> cancelAssign(@PathVariable Long registrationId) {
        dispatchService.cancelAssign(registrationId);
        return R.ok();
    }
}
