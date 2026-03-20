package backend.modules.dispatch.mapper;

import backend.modules.dispatch.dto.DispatchCandidateVO;
import backend.modules.dispatch.dto.DispatchRecommendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DispatchMapper {

    /**
     * 查询岗位摘要信息（名称、名额、需求标签列表）。
     * 返回的 DispatchRecommendVO 中 candidates / qualifiedCount 由 Service 层填充。
     */
    DispatchRecommendVO selectPositionInfo(@Param("positionId") Long positionId);

    /**
     * 智能匹配：查询该岗位下所有候选志愿者并计算标签交集匹配分。
     * 候选人范围：同活动下 status IN (0=待审核, 1=已通过) 的报名记录。
     * 结果按 match_score DESC, total_hours DESC 排序。
     */
    List<DispatchCandidateVO> selectCandidates(@Param("positionId") Long positionId);
}
