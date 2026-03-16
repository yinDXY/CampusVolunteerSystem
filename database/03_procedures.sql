USE volunteer_db;

DELIMITER $$

-- ============================================================
-- 存储过程1：活动结算
--   proc_settle_activity(p_activity_id, OUT p_result_code, OUT p_result_msg)
--
-- 功能：
--   1. 校验活动是否处于"进行中"状态
--   2. 将活动状态改为"已结束"（status=3）
--   3. 对所有已签到但未签退的志愿者，自动签退（以活动结束时间为准）
--   4. 计算并更新每条 checkin 的 duration_hours
--   5. 批量累加 sys_user.total_hours
--   6. 对所有已通过报名但未签到的志愿者，标记为缺席（无时长）
--   7. 返回结算结果
--
-- 返回码：
--   0 = 成功
--   1 = 活动不存在
--   2 = 活动状态不是"进行中"
-- ============================================================
CREATE PROCEDURE proc_settle_activity(
    IN  p_activity_id BIGINT,
    OUT p_result_code INT,
    OUT p_result_msg  VARCHAR(200)
)
BEGIN
    DECLARE v_status   TINYINT;
    DECLARE v_end_time DATETIME;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result_code = -1;
        SET p_result_msg  = '活动结算时发生数据库异常，已回滚';
    END;

    -- 1. 查询活动状态
    SELECT status, end_time
    INTO v_status, v_end_time
    FROM activity
    WHERE id = p_activity_id AND deleted = 0
    LIMIT 1;

    IF v_status IS NULL THEN
        SET p_result_code = 1;
        SET p_result_msg  = '活动不存在';
        LEAVE proc_settle_activity;  -- MySQL 存储过程标签跳出，替代 RETURN
    END IF;

    IF v_status != 2 THEN
        SET p_result_code = 2;
        SET p_result_msg  = CONCAT('活动当前状态为 ', v_status, '，无法结算（需为进行中=2）');
        LEAVE proc_settle_activity;
    END IF;

    START TRANSACTION;

        -- 2. 将活动状态改为已结束
        UPDATE activity
        SET status = 3, updated_at = NOW()
        WHERE id = p_activity_id;

        -- 3. 对未签退的签到记录，以活动结束时间补充签退
        UPDATE checkin
        SET checkout_time  = v_end_time,
            duration_hours = ROUND(TIMESTAMPDIFF(SECOND, checkin_time, v_end_time) / 3600.0, 2)
        WHERE activity_id  = p_activity_id
          AND checkout_time IS NULL;

        -- 4. 批量累加时长到用户总时长
        --    只累加本次结算中刚补签退的记录（checkout_time 刚被设为 v_end_time 的）
        UPDATE sys_user u
        INNER JOIN checkin c ON c.user_id = u.id
        SET u.total_hours = u.total_hours + c.duration_hours
        WHERE c.activity_id    = p_activity_id
          AND c.checkout_time  = v_end_time  -- 刚补充的签退
          AND c.duration_hours IS NOT NULL;

        -- 5. 成就解锁由 trg_user_achievement_unlock 触发器自动处理

    COMMIT;

    SET p_result_code = 0;
    SET p_result_msg  = '活动结算成功';

END$$


-- ============================================================
-- 存储过程2：刷新排行榜统计视图（全量刷新到缓存表）
--   proc_refresh_leaderboard()
--
-- 功能：
--   将志愿时长 TOP 50 的志愿者数据刷新到排行榜缓存表
--   （配合 Spring Task 定时调用，避免每次实时聚合给主库带来压力）
-- ============================================================

-- 排行榜缓存表（单独建，供存储过程写入）
CREATE TABLE IF NOT EXISTS leaderboard_cache (
    rank_no       INT          NOT NULL COMMENT '排名',
    user_id       BIGINT       NOT NULL COMMENT '用户ID',
    real_name     VARCHAR(50)  NOT NULL COMMENT '真实姓名',
    avatar_url    VARCHAR(500) DEFAULT NULL COMMENT '头像',
    total_hours   DECIMAL(8,2) NOT NULL    COMMENT '累计时长',
    activity_count INT         NOT NULL    COMMENT '参与活动次数',
    refreshed_at  DATETIME     NOT NULL    COMMENT '刷新时间',
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时长排行榜缓存表';

CREATE PROCEDURE proc_refresh_leaderboard()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
    END;

    START TRANSACTION;

        -- 先清空旧数据
        TRUNCATE TABLE leaderboard_cache;

        -- 重新计算 TOP 50 并写入
        INSERT INTO leaderboard_cache (rank_no, user_id, real_name, avatar_url, total_hours, activity_count, refreshed_at)
        SELECT
            ROW_NUMBER() OVER (ORDER BY u.total_hours DESC, u.created_at ASC) AS rank_no,
            u.id                    AS user_id,
            u.real_name,
            u.avatar_url,
            u.total_hours,
            COUNT(DISTINCT c.activity_id)                                    AS activity_count,
            NOW()                   AS refreshed_at
        FROM sys_user u
        LEFT JOIN checkin c ON c.user_id = u.id AND c.duration_hours IS NOT NULL
        WHERE u.role    = 0
          AND u.deleted = 0
          AND u.status  = 1
        GROUP BY u.id, u.real_name, u.avatar_url, u.total_hours
        ORDER BY u.total_hours DESC
        LIMIT 50;

    COMMIT;
END$$


-- ============================================================
-- 存储过程3：志愿者报名原子操作
--   proc_apply_activity(p_user_id, p_activity_id, p_remark,
--                        OUT p_result_code, OUT p_result_msg)
--
-- 功能（事务保护）：
--   1. 校验活动是否存在且状态为"报名中"(1)
--   2. 校验志愿者是否已报名（防重复）
--   3. 校验名额是否已满
--   4. 插入报名记录
--   5. signed_count+1 由触发器 trg_registration_insert 完成
--
-- 返回码：
--   0 = 报名成功
--   1 = 活动不存在或已不在报名期
--   2 = 已报名，请勿重复提交
--   3 = 活动名额已满
-- ============================================================
CREATE PROCEDURE proc_apply_activity(
    IN  p_user_id     BIGINT,
    IN  p_activity_id BIGINT,
    IN  p_remark      VARCHAR(500),
    OUT p_result_code INT,
    OUT p_result_msg  VARCHAR(200)
)
BEGIN
    DECLARE v_status      TINYINT;
    DECLARE v_quota       INT;
    DECLARE v_signed      INT;
    DECLARE v_exist_count INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result_code = -1;
        SET p_result_msg  = '报名时发生数据库异常，已回滚';
    END;

    START TRANSACTION;

        -- 1. 查询活动（加行锁，防并发超额报名）
        SELECT status, total_quota, signed_count
        INTO v_status, v_quota, v_signed
        FROM activity
        WHERE id = p_activity_id AND deleted = 0
        FOR UPDATE;

        IF v_status IS NULL OR v_status != 1 THEN
            SET p_result_code = 1;
            SET p_result_msg  = '活动不存在或当前不在报名期';
            ROLLBACK;
            LEAVE proc_apply_activity;
        END IF;

        -- 2. 检查是否已报名
        SELECT COUNT(*) INTO v_exist_count
        FROM registration
        WHERE activity_id = p_activity_id
          AND user_id     = p_user_id
          AND status NOT IN (4);  -- 已取消的可以重新报名

        IF v_exist_count > 0 THEN
            SET p_result_code = 2;
            SET p_result_msg  = '您已报名该活动，请勿重复提交';
            ROLLBACK;
            LEAVE proc_apply_activity;
        END IF;

        -- 3. 检查名额
        IF v_signed >= v_quota THEN
            SET p_result_code = 3;
            SET p_result_msg  = '活动名额已满';
            ROLLBACK;
            LEAVE proc_apply_activity;
        END IF;

        -- 4. 插入报名记录（triggered: trg_registration_insert 会递增 signed_count）
        INSERT INTO registration (activity_id, user_id, status, remark)
        VALUES (p_activity_id, p_user_id, 0, p_remark);

    COMMIT;

    SET p_result_code = 0;
    SET p_result_msg  = '报名成功，等待审核';

END$$


-- ============================================================
-- 存储过程4：生成志愿者画像雷达图数据
--   proc_get_user_radar(p_user_id)
--
-- 功能：聚合计算志愿者五维画像指标，供 Flask 微服务调用
--   - 维度1 参与次数（activity_count）：已完成的活动数
--   - 维度2 总时长（total_hours）：sys_user.total_hours
--   - 维度3 活动类型多样性（type_diversity）：参与的不同活动类型标签数
--   - 维度4 平均评分（avg_score）：被管理员打分的平均值（1-5）
--   - 维度5 准时率（punctuality_rate）：非迟到 / 总签到 × 100
--
-- 返回：单行结果集，供 Java 层映射为 RadarDataDTO
-- ============================================================
CREATE PROCEDURE proc_get_user_radar(
    IN p_user_id BIGINT
)
BEGIN
    SELECT
        u.id                                                                  AS user_id,
        u.real_name,
        u.total_hours,
        COUNT(DISTINCT c.activity_id)                                         AS activity_count,
        COUNT(DISTINCT at_tag.tag_id)                                         AS type_diversity,
        ROUND(IFNULL(AVG(r.score), 0), 2)                                     AS avg_score,
        ROUND(
            IFNULL(
                SUM(CASE WHEN c.is_late = 0 THEN 1 ELSE 0 END) * 100.0
                / NULLIF(COUNT(c.id), 0),
            0)
        , 2)                                                                   AS punctuality_rate
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
    WHERE u.id      = p_user_id
      AND u.deleted = 0
    GROUP BY u.id, u.real_name, u.total_hours;
END$$


DELIMITER ;
