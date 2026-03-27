package backend.modules.stats.mapper;

import backend.modules.stats.dto.ActivityHeatVO;
import backend.modules.stats.dto.LeaderboardVO;
import backend.modules.stats.dto.TrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StatsMapper {

    /** 查询排行榜缓存（已按 rank_no 排序） */
    List<LeaderboardVO> selectLeaderboard();

    /** 查询活动热度视图 */
    List<ActivityHeatVO> selectActivityHeat();

    /** 按月统计指定年份的参与趋势 */
    List<TrendVO> selectTrend(@Param("year") int year);
}
