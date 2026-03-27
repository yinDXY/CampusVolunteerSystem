package backend.modules.stats.controller;

import backend.common.result.R;
import backend.modules.stats.dto.ActivityHeatVO;
import backend.modules.stats.dto.LeaderboardVO;
import backend.modules.stats.dto.TrendVO;
import backend.modules.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 时长排行榜
     * GET /api/stats/leaderboard
     * 权限：所有已登录用户
     */
    @GetMapping("/leaderboard")
    @PreAuthorize("isAuthenticated()")
    public R<List<LeaderboardVO>> getLeaderboard() {
        return R.ok(statsService.getLeaderboard());
    }

    /**
     * 活动热度统计
     * GET /api/stats/activity-heat
     * 权限：活动管理员、超级管理员
     */
    @GetMapping("/activity-heat")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<List<ActivityHeatVO>> getActivityHeat() {
        return R.ok(statsService.getActivityHeat());
    }

    /**
     * 按月参与趋势
     * GET /api/stats/trend?year=2026
     * 权限：活动管理员、超级管理员
     */
    @GetMapping("/trend")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<List<TrendVO>> getTrend(
            @RequestParam(required = false) Integer year) {
        return R.ok(statsService.getTrend(year));
    }
}
