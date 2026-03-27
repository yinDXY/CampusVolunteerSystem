package backend.modules.achievement.mapper;

import backend.modules.achievement.dto.AchievementVO;
import backend.modules.achievement.dto.UserAchievementVO;
import backend.modules.achievement.entity.Achievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface AchievementMapper {

    /** 查询所有成就定义（按 required_hours 升序） */
    List<Achievement> selectAll();

    /** 查询指定用户已解锁的成就列表（含解锁时间） */
    List<UserAchievementVO> selectUnlockedByUserId(@Param("userId") Long userId);

    /** 查询指定用户已解锁的成就 ID 集合（用于批量判断 unlocked 标志） */
    Set<Long> selectUnlockedIdsByUserId(@Param("userId") Long userId);
}
