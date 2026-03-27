package backend.modules.achievement.service;

import backend.modules.achievement.dto.AchievementVO;
import backend.modules.achievement.dto.UserAchievementVO;

import java.util.List;

public interface AchievementService {

    /** 获取当前登录志愿者已解锁的成就列表 */
    List<UserAchievementVO> getMyAchievements();

    /**
     * 获取所有成就定义，并标记当前登录用户的解锁状态。
     * 非志愿者角色 unlocked 全部为 false（仅展示定义）。
     */
    List<AchievementVO> getAllAchievements();
}
