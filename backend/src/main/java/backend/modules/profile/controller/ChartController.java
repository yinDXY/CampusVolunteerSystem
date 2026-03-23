package backend.modules.profile.controller;

import backend.common.result.R;
import backend.modules.profile.dto.ProfileVO;
import backend.modules.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ChartController {

    private final ProfileService profileService;

    /**
     * 获取当前登录志愿者的个人画像（含雷达图）
     * GET /api/profile/radar
     * 权限：志愿者（已登录即可）
     */
    @GetMapping("/radar")
    @PreAuthorize("isAuthenticated()")
    public R<ProfileVO> getMyRadar() {
        return R.ok(profileService.getMyProfile());
    }

    /**
     * 管理员查看指定用户的画像（含雷达图）
     * GET /api/profile/radar/{userId}
     * 权限：活动管理员、超级管理员
     */
    @GetMapping("/radar/{userId}")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<ProfileVO> getRadar(@PathVariable Long userId) {
        return R.ok(profileService.getProfile(userId));
    }
}
