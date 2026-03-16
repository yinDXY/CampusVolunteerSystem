USE volunteer_db;

DELIMITER $$

-- ============================================================
-- 触发器1：签退后自动累加用户总时长
--
-- 时机：checkin 表的 checkout_time 从 NULL 变为非 NULL 时
--       即 UPDATE AFTER，检测 checkout_time 从空到有值
-- 逻辑：
--   1. 计算 duration_hours = ROUND((checkout_time - checkin_time) / 3600, 2)
--   2. 更新 checkin.duration_hours
--   3. 累加 sys_user.total_hours
--   4. 调用成就检测（通过触发器2）
-- ============================================================
CREATE TRIGGER trg_checkin_checkout_update
AFTER UPDATE ON checkin
FOR EACH ROW
BEGIN
    -- 只在签退时（checkout_time 从 NULL → 非 NULL）触发
    IF OLD.checkout_time IS NULL AND NEW.checkout_time IS NOT NULL THEN

        -- 累加志愿者总时长（duration_hours 由业务层计算后写入，或此处计算）
        UPDATE sys_user
        SET total_hours = total_hours + NEW.duration_hours
        WHERE id = NEW.user_id;

        -- 同步更新报名记录中的状态（可选，标记"已完成"）
        -- UPDATE registration SET status = 5 WHERE id = NEW.registration_id;

    END IF;
END$$


-- ============================================================
-- 触发器2：用户总时长更新后，检测并解锁成就勋章
--
-- 时机：sys_user.total_hours 被更新后
-- 逻辑：遍历 achievement 表，找到 required_hours <= new_total_hours
--        且尚未解锁的成就，全部插入 user_achievement
-- ============================================================
CREATE TRIGGER trg_user_achievement_unlock
AFTER UPDATE ON sys_user
FOR EACH ROW
BEGIN
    -- 仅在 total_hours 增加时检测
    IF NEW.total_hours > OLD.total_hours THEN
        -- 将符合条件且未解锁的成就批量插入
        INSERT IGNORE INTO user_achievement (user_id, achievement_id, unlocked_at)
        SELECT NEW.id, a.id, NOW()
        FROM achievement a
        WHERE a.required_hours <= NEW.total_hours
          AND NOT EXISTS (
              SELECT 1 FROM user_achievement ua
              WHERE ua.user_id = NEW.id
                AND ua.achievement_id = a.id
          );
    END IF;
END$$


-- ============================================================
-- 触发器3：报名成功后自动递增活动的已报名人数
--
-- 时机：registration 表 INSERT AFTER
-- 逻辑：若报名状态不是"已取消"(4)或"已拒绝"(2)，则 signed_count+1
-- ============================================================
CREATE TRIGGER trg_registration_insert
AFTER INSERT ON registration
FOR EACH ROW
BEGIN
    IF NEW.status NOT IN (2, 4) THEN
        UPDATE activity
        SET signed_count = signed_count + 1
        WHERE id = NEW.activity_id;
    END IF;
END$$


-- ============================================================
-- 触发器4：报名状态变更时同步维护 activity.signed_count
--
-- 时机：registration 表 UPDATE AFTER
-- 逻辑：
--   - 旧状态有效 → 新状态无效（取消/拒绝）：signed_count-1
--   - 旧状态无效 → 新状态有效（重新激活）：signed_count+1
-- ============================================================
CREATE TRIGGER trg_registration_status_update
AFTER UPDATE ON registration
FOR EACH ROW
BEGIN
    -- 有效状态：0=待审核 1=已通过 3=已派岗
    -- 无效状态：2=已拒绝 4=已取消
    DECLARE old_valid TINYINT DEFAULT 0;
    DECLARE new_valid TINYINT DEFAULT 0;

    IF OLD.status NOT IN (2, 4) THEN SET old_valid = 1; END IF;
    IF NEW.status NOT IN (2, 4) THEN SET new_valid = 1; END IF;

    IF old_valid = 1 AND new_valid = 0 THEN
        -- 从有效变为无效：减少计数
        UPDATE activity SET signed_count = signed_count - 1 WHERE id = NEW.activity_id;
    ELSEIF old_valid = 0 AND new_valid = 1 THEN
        -- 从无效变为有效：增加计数
        UPDATE activity SET signed_count = signed_count + 1 WHERE id = NEW.activity_id;
    END IF;
END$$


-- ============================================================
-- 触发器5：派岗完成后自动更新岗位的已派岗人数
--
-- 时机：registration 表 UPDATE AFTER，检测 position_id 从 NULL → 非 NULL
--       且状态变为 3（已派岗）
-- ============================================================
CREATE TRIGGER trg_registration_assign_position
AFTER UPDATE ON registration
FOR EACH ROW
BEGIN
    -- 新增派岗：position_id 从空到有、且状态变为已派岗
    IF OLD.position_id IS NULL AND NEW.position_id IS NOT NULL
       AND NEW.status = 3 THEN
        UPDATE `position`
        SET assigned_count = assigned_count + 1
        WHERE id = NEW.position_id;
    END IF;

    -- 取消派岗：position_id 从有到空
    IF OLD.position_id IS NOT NULL AND NEW.position_id IS NULL
       AND OLD.status = 3 THEN
        UPDATE `position`
        SET assigned_count = GREATEST(assigned_count - 1, 0)
        WHERE id = OLD.position_id;
    END IF;
END$$


DELIMITER ;
