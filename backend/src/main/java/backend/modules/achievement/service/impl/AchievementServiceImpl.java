package backend.modules.achievement.service.impl;

import backend.modules.achievement.dto.AchievementVO;
import backend.modules.achievement.dto.UserAchievementVO;
import backend.modules.achievement.entity.Achievement;
import backend.modules.achievement.mapper.AchievementMapper;
import backend.modules.achievement.service.AchievementService;
import backend.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final AchievementMapper achievementMapper;

    @Override
    public List<UserAchievementVO> getMyAchievements() {
        Long userId = SecurityUtil.getCurrentUserId();
        return achievementMapper.selectUnlockedByUserId(userId);
    }

    @Override
    public List<AchievementVO> getAllAchievements() {
        Long userId = SecurityUtil.getCurrentUserId();

        // 查询当前用户已解锁的成就 ID 集合（非志愿者也会返回空集合，不报错）
        Set<Long> unlockedIds = achievementMapper.selectUnlockedIdsByUserId(userId);

        List<Achievement> allAchievements = achievementMapper.selectAll();

        return allAchievements.stream().map(a -> {
            AchievementVO vo = new AchievementVO();
            vo.setId(a.getId());
            vo.setName(a.getName());
            vo.setDescription(a.getDescription());
            vo.setIconUrl(a.getIconUrl());
            vo.setBadgeLevel(a.getBadgeLevel());
            vo.setRequiredHours(a.getRequiredHours());
            vo.setUnlocked(unlockedIds.contains(a.getId()));
            return vo;
        }).collect(Collectors.toList());
    }
}
