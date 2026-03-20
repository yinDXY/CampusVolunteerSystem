package backend.modules.dispatch.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 智能派岗候选人（单个志愿者的匹配结果）
 */
@Data
public class DispatchCandidateVO {

    private Long registrationId;
    private Long userId;
    private String realName;
    private String studentId;
    private BigDecimal totalHours;

    /** 志愿者拥有的全部技能标签（逗号分隔，供预览） */
    private String volunteerTags;

    /** 岗位需求标签总数 */
    private Integer requiredCount;

    /** 该志愿者命中的需求标签数 */
    private Integer matchedCount;

    /**
     * 综合匹配分 = matchedCount + (totalHours >= position.requiredHours ? 1 : 0)
     * 用于排序，分数越高越优先
     */
    private Integer matchScore;

    /**
     * 是否完全合格：matchedCount == requiredCount（或岗位无标签需求时全员合格）
     * 由 Service 层计算赋值，不来自 SQL
     */
    private Boolean qualified;
}
