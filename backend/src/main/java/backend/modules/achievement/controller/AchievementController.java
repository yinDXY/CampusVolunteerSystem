package backend.modules.achievement.controller;

import backend.common.result.R;
import backend.modules.achievement.dto.AchievementVO;
import backend.modules.achievement.dto.UserAchievementVO;
import backend.modules.achievement.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    /**
     * 获取我的勋章列表（已解锁）
     * GET /api/achievements/my
     * 权限：志愿者
     * 触发器 trg_user_achievement_unlock 在签退后自动解锁，此接口仅查询
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public R<List<UserAchievementVO>> getMyAchievements() {
        return R.ok(achievementService.getMyAchievements());
    }

    /**
     * 获取全部成就定义（含当前用户解锁状态）
     * GET /api/achievements
     * 权限：所有已登录用户
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public R<List<AchievementVO>> getAllAchievements() {
        return R.ok(achievementService.getAllAchievements());
    }
}
