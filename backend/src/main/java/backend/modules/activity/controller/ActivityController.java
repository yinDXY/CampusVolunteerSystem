package backend.modules.activity.controller;

import backend.common.result.PageResult;
import backend.common.result.R;
import backend.modules.activity.dto.*;
import backend.modules.activity.service.ActivityService;
import backend.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    /**
     * 活动列表（分页+关键词+状态筛选）
     * GET /api/activities?pageNum=1&pageSize=10&keyword=xx&status=1
     * 权限：所有已登录用户
     */
    @GetMapping
    public R<PageResult<ActivityVO>> list(ActivityQueryDTO query) {
        return R.ok(activityService.getPage(query));
    }

    /**
     * 活动详情
     * GET /api/activities/{id}
     * 权限：所有已登录用户
     */
    @GetMapping("/{id}")
    public R<ActivityVO> getById(@PathVariable Long id) {
        return R.ok(activityService.getById(id));
    }

    /**
     * 发布活动（初始为草稿）
     * POST /api/activities
     * 权限：活动管理员、超级管理员
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<Long> create(@Valid @RequestBody ActivityCreateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(activityService.createActivity(dto, userId));
    }

    /**
     * 编辑活动
     * PUT /api/activities/{id}
     * 权限：活动创建者 或 超级管理员
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<Void> update(@PathVariable Long id,
                          @Valid @RequestBody ActivityUpdateDTO dto) {
        activityService.updateActivity(id, dto,
                SecurityUtil.getCurrentUserId(),
                SecurityUtil.getCurrentUserRole());
        return R.ok();
    }

    /**
     * 删除活动（逻辑删除）
     * DELETE /api/activities/{id}
     * 权限：活动创建者 或 超级管理员
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<Void> delete(@PathVariable Long id) {
        activityService.deleteActivity(id,
                SecurityUtil.getCurrentUserId(),
                SecurityUtil.getCurrentUserRole());
        return R.ok();
    }

    /**
     * 更新活动状态（草稿→报名中→进行中→已结束）
     * PATCH /api/activities/{id}/status?status=1
     * 权限：活动创建者 或 超级管理员
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<Void> updateStatus(@PathVariable Long id,
                                @RequestParam Integer status) {
        activityService.updateStatus(id, status,
                SecurityUtil.getCurrentUserId(),
                SecurityUtil.getCurrentUserRole());
        return R.ok();
    }
}
