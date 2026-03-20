package backend.modules.position.controller;

import backend.common.result.R;
import backend.modules.position.dto.PositionCreateDTO;
import backend.modules.position.dto.PositionUpdateDTO;
import backend.modules.position.dto.PositionVO;
import backend.modules.position.service.PositionService;
import backend.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    /**
     * 创建岗位
     * POST /api/activities/{activityId}/positions
     * 权限：活动管理员、超级管理员
     */
    @PostMapping("/api/activities/{activityId}/positions")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<Long> create(@PathVariable Long activityId,
                          @Valid @RequestBody PositionCreateDTO dto) {
        dto.setActivityId(activityId);
        return R.ok(positionService.createPosition(dto,
                SecurityUtil.getCurrentUserId(),
                SecurityUtil.getCurrentUserRole()));
    }

    /**
     * 查询某活动的岗位列表
     * GET /api/activities/{activityId}/positions
     * 权限：所有已登录用户
     */
    @GetMapping("/api/activities/{activityId}/positions")
    public R<List<PositionVO>> listByActivity(@PathVariable Long activityId) {
        return R.ok(positionService.listByActivity(activityId));
    }

    /**
     * 编辑岗位
     * PUT /api/positions/{id}
     * 权限：活动管理员（活动创建者）、超级管理员
     */
    @PutMapping("/api/positions/{id}")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<Void> update(@PathVariable Long id,
                          @Valid @RequestBody PositionUpdateDTO dto) {
        positionService.updatePosition(id, dto,
                SecurityUtil.getCurrentUserId(),
                SecurityUtil.getCurrentUserRole());
        return R.ok();
    }

    /**
     * 删除岗位（逻辑删除）
     * DELETE /api/positions/{id}
     * 权限：活动管理员（活动创建者）、超级管理员
     */
    @DeleteMapping("/api/positions/{id}")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<Void> delete(@PathVariable Long id) {
        positionService.deletePosition(id,
                SecurityUtil.getCurrentUserId(),
                SecurityUtil.getCurrentUserRole());
        return R.ok();
    }
}
