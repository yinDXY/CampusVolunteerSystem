package backend.modules.dispatch.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 智能派岗推荐结果（岗位信息 + 候选人列表）
 */
@Data
public class DispatchRecommendVO {

    private Long positionId;
    private String positionName;
    /** 岗位总名额 */
    private Integer quota;
    /** 已派岗人数（触发器维护） */
    private Integer assignedCount;
    /** 剩余名额 = quota - assignedCount */
    private Integer remainingQuota;
    /** 岗位要求最低志愿时长（小时） */
    private BigDecimal requiredHours;
    /** 岗位需求标签总数 */
    private Integer requiredTagCount;
    /** 岗位需求标签名称（逗号分隔） */
    private String requiredTags;

    /** 完全合格候选人数（matchedCount == requiredTagCount） */
    private Integer qualifiedCount;
    /** 候选人列表（按 matchScore DESC, totalHours DESC 排序） */
    private List<DispatchCandidateVO> candidates;
}
