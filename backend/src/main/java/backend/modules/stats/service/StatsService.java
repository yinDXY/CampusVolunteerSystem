package backend.modules.stats.service;

import backend.modules.stats.dto.ActivityHeatVO;
import backend.modules.stats.dto.LeaderboardVO;
import backend.modules.stats.dto.TrendVO;

import java.util.List;

public interface StatsService {

    List<LeaderboardVO> getLeaderboard();

    List<ActivityHeatVO> getActivityHeat();

    List<TrendVO> getTrend(Integer year);
}
