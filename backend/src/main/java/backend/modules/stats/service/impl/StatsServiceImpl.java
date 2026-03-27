package backend.modules.stats.service.impl;

import backend.modules.stats.dto.ActivityHeatVO;
import backend.modules.stats.dto.LeaderboardVO;
import backend.modules.stats.dto.TrendVO;
import backend.modules.stats.mapper.StatsMapper;
import backend.modules.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsMapper statsMapper;

    @Override
    public List<LeaderboardVO> getLeaderboard() {
        return statsMapper.selectLeaderboard();
    }

    @Override
    public List<ActivityHeatVO> getActivityHeat() {
        return statsMapper.selectActivityHeat();
    }

    @Override
    public List<TrendVO> getTrend(Integer year) {
        int targetYear = (year != null && year > 0) ? year : LocalDate.now().getYear();
        return statsMapper.selectTrend(targetYear);
    }
}
