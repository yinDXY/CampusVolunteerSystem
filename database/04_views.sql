USE volunteer_db;

-- ============================================================
-- 视图1：活动报名详情视图 v_activity_registration_detail
--
-- 聚合内容：
--   活动基本信息 + 志愿者姓名/学号 + 报名状态 + 派岗岗位名称
--   + 签到时间/时长 + 评分
--
-- 使用场景：
--   - 管理员活动详情页查看报名/签到列表
--   - 管理员派岗页
--   - 数据导出报表
-- ============================================================
CREATE OR REPLACE VIEW v_activity_registration_detail AS
SELECT
    r.id                          AS registration_id,
    r.activity_id,
    a.title                       AS activity_title,
    a.start_time                  AS activity_start_time,
    a.end_time                    AS activity_end_time,
    a.location                    AS activity_location,
    a.status                      AS activity_status,
    r.user_id,
    u.real_name                   AS volunteer_name,
    u.student_id,
    u.phone,
    u.email,
    u.total_hours                 AS user_total_hours,
    r.status                      AS registration_status,
    r.remark                      AS registration_remark,
    r.score                       AS volunteer_score,
    r.activity_score              AS activity_score,
    r.activity_comment            AS activity_comment,
    r.created_at                  AS apply_time,
    p.id                          AS position_id,
    p.name                        AS position_name,
    p.description                 AS position_desc,
    c.id                          AS checkin_id,
    c.checkin_time,
    c.checkout_time,
    c.duration_hours,
    c.is_late,
    c.hash_proof
FROM registration r
INNER JOIN activity a  ON a.id = r.activity_id AND a.deleted = 0
INNER JOIN sys_user u  ON u.id = r.user_id     AND u.deleted = 0
LEFT  JOIN `position` p  ON p.id = r.position_id AND p.deleted = 0
LEFT  JOIN checkin  c  ON c.registration_id = r.id;


-- ============================================================
-- 视图2：志愿者画像聚合视图 v_volunteer_profile
--
-- 聚合内容：
--   用户基本信息 + 五维画像数据 + 已解锁勋章数 + 技能标签列表
--
-- 使用场景：
--   - 个人中心展示
--   - Flask 微服务雷达图数据来源
--   - 超级管理员志愿者列表
-- ============================================================
CREATE OR REPLACE VIEW v_volunteer_profile AS
SELECT
    u.id                                                                   AS user_id,
    u.real_name,
    u.student_id,
    u.avatar_url,
    u.total_hours,
    u.created_at                                                           AS register_time,
    COUNT(DISTINCT c.activity_id)                                          AS activity_count,
    COUNT(DISTINCT at_tag.tag_id)                                          AS type_diversity,
    ROUND(IFNULL(AVG(r.score), 0), 2)                                      AS avg_score,
    ROUND(
        IFNULL(
            SUM(CASE WHEN c.is_late = 0 AND c.duration_hours IS NOT NULL THEN 1 ELSE 0 END) * 100.0
            / NULLIF(COUNT(CASE WHEN c.duration_hours IS NOT NULL THEN 1 END), 0),
        0)
    , 2)                                                                    AS punctuality_rate,
    COUNT(DISTINCT ua.achievement_id)                                       AS badge_count,
    -- 技能标签（逗号拼接，供展示用）
    GROUP_CONCAT(DISTINCT skill_tag.name ORDER BY skill_tag.name SEPARATOR ',')
                                                                            AS skill_tags
FROM sys_user u
LEFT JOIN checkin c
       ON c.user_id = u.id
      AND c.duration_hours IS NOT NULL
LEFT JOIN activity_tag at_tag
       ON at_tag.activity_id = c.activity_id
LEFT JOIN registration r
       ON r.user_id = u.id
      AND r.activity_id = c.activity_id
      AND r.score IS NOT NULL
LEFT JOIN user_achievement ua
       ON ua.user_id = u.id
LEFT JOIN user_tag ut
       ON ut.user_id = u.id
LEFT JOIN tag skill_tag
       ON skill_tag.id = ut.tag_id
      AND skill_tag.category = 'SKILL'
WHERE u.role    = 0   -- 仅志愿者
  AND u.deleted = 0
  AND u.status  = 1
GROUP BY
    u.id, u.real_name, u.student_id, u.avatar_url,
    u.total_hours, u.created_at;


-- ============================================================
-- 视图3：活动热度统计视图 v_activity_heat
--
-- 聚合内容：
--   活动基本信息 + 已报名人数 + 签到人数 + 岗位总配额 + 岗位数
--   + 活动类型标签
--
-- 使用场景：
--   - 管理员数据统计看板（ECharts）
--   - 活动大厅热度排序
-- ============================================================
CREATE OR REPLACE VIEW v_activity_heat AS
SELECT
    a.id                                                                AS activity_id,
    a.title,
    a.location,
    a.start_time,
    a.end_time,
    a.status                                                            AS activity_status,
    a.total_quota,
    a.signed_count,
    COUNT(DISTINCT c.id)                                                AS checkin_count,
    SUM(DISTINCT p.quota)                                               AS position_total_quota,
    COUNT(DISTINCT p.id)                                                AS position_count,
    GROUP_CONCAT(DISTINCT t.name ORDER BY t.name SEPARATOR ',')        AS activity_tags,
    ROUND(IFNULL(AVG(r.activity_score), 0), 2)                         AS avg_activity_score,
    COUNT(DISTINCT CASE WHEN r.activity_comment IS NOT NULL THEN r.id END) AS comment_count,
    a.creator_id,
    creator.real_name                                                   AS creator_name
FROM activity a
LEFT JOIN registration r    ON r.activity_id = a.id AND r.status NOT IN (2, 4)
LEFT JOIN checkin c         ON c.activity_id = a.id AND c.duration_hours IS NOT NULL
LEFT JOIN `position` p        ON p.activity_id = a.id AND p.deleted = 0
LEFT JOIN activity_tag at2  ON at2.activity_id = a.id
LEFT JOIN tag t             ON t.id = at2.tag_id
LEFT JOIN sys_user creator  ON creator.id = a.creator_id AND creator.deleted = 0
WHERE a.deleted = 0
GROUP BY
    a.id, a.title, a.location, a.start_time, a.end_time,
    a.status, a.total_quota, a.signed_count, a.creator_id, creator.real_name;


-- ============================================================
-- 视图4：智能派岗匹配视图 v_smart_assign_match
--
-- 说明：
--   这是系统创新点"智能派岗"的核心 SQL。
--   对于每个活动下的每个岗位，计算每位已通过报名的志愿者
--   与该岗位在技能标签上的匹配分数（matched_tag_count）。
--   管理员可据此按匹配度排序，选择最合适的志愿者派岗。
--
-- 匹配算法：
--   匹配分 = 志愿者技能标签 ∩ 岗位需求标签 的数量
--   + 时长资格加成（total_hours >= required_hours ? 1 : 0）
-- ============================================================
CREATE OR REPLACE VIEW v_smart_assign_match AS
SELECT
    p.activity_id,
    a.title                                                             AS activity_title,
    p.id                                                                AS position_id,
    p.name                                                              AS position_name,
    p.quota,
    p.assigned_count,
    p.required_hours                                                    AS position_required_hours,
    r.id                                                                AS registration_id,
    r.user_id,
    u.real_name                                                         AS volunteer_name,
    u.student_id,
    u.total_hours                                                       AS volunteer_total_hours,
    COUNT(DISTINCT pt.tag_id)                                           AS required_tag_count,
    COUNT(DISTINCT CASE WHEN ut.tag_id IS NOT NULL THEN pt.tag_id END)  AS matched_tag_count,
    -- 综合匹配分：标签匹配数 + 时长达标加成
    (COUNT(DISTINCT CASE WHEN ut.tag_id IS NOT NULL THEN pt.tag_id END)
        + IF(u.total_hours >= p.required_hours, 1, 0))                 AS match_score,
    GROUP_CONCAT(DISTINCT t_req.name ORDER BY t_req.name SEPARATOR ',') AS required_tags,
    GROUP_CONCAT(DISTINCT t_vol.name ORDER BY t_vol.name SEPARATOR ',') AS volunteer_tags
FROM `position` p
INNER JOIN activity a                ON a.id = p.activity_id AND a.deleted = 0
INNER JOIN registration r            ON r.activity_id = p.activity_id AND r.status = 1  -- 已通过报名
INNER JOIN sys_user u                ON u.id = r.user_id AND u.deleted = 0
LEFT  JOIN position_tag pt           ON pt.position_id = p.id
LEFT  JOIN tag  t_req                ON t_req.id = pt.tag_id
LEFT  JOIN user_tag ut               ON ut.user_id = r.user_id AND ut.tag_id = pt.tag_id
LEFT  JOIN user_tag ut_all           ON ut_all.user_id = r.user_id
LEFT  JOIN tag  t_vol                ON t_vol.id = ut_all.tag_id AND t_vol.category = 'SKILL'
WHERE p.deleted = 0
  AND r.position_id IS NULL  -- 尚未被派岗
GROUP BY
    p.activity_id, a.title,
    p.id, p.name, p.quota, p.assigned_count, p.required_hours,
    r.id, r.user_id, u.real_name, u.student_id, u.total_hours;
