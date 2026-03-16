CREATE DATABASE IF NOT EXISTS volunteer_db
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE volunteer_db;

-- ============================================================
-- 1. 用户表 sys_user
--    角色：VOLUNTEER(志愿者) / ACTIVITY_ADMIN(活动管理员) / SUPER_ADMIN(超级管理员)
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username      VARCHAR(50)  NOT NULL                COMMENT '用户名（登录账号）',
    password      VARCHAR(255) NOT NULL                COMMENT '密码（BCrypt加密）',
    real_name     VARCHAR(50)  NOT NULL                COMMENT '真实姓名',
    student_id    VARCHAR(30)  DEFAULT NULL            COMMENT '学号（管理员可为空）',
    phone         VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    email         VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    avatar_url    VARCHAR(500) DEFAULT NULL            COMMENT '头像URL',
    role          TINYINT      NOT NULL DEFAULT 0      COMMENT '角色：0=志愿者 1=活动管理员 2=超级管理员',
    total_hours   DECIMAL(8,2) NOT NULL DEFAULT 0.00   COMMENT '累计志愿时长（小时）',
    status        TINYINT      NOT NULL DEFAULT 1      COMMENT '账号状态：1=正常 0=禁用',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=正常 1=已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role (role),
    KEY idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';


-- ============================================================
-- 2. 标签表 tag
--    用于描述技能、活动类型等
-- ============================================================
CREATE TABLE IF NOT EXISTS tag (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    name        VARCHAR(50)  NOT NULL                COMMENT '标签名称',
    category    VARCHAR(50)  DEFAULT NULL            COMMENT '标签分类：SKILL(技能)/ACTIVITY_TYPE(活动类型)',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name_category (name, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';


-- ============================================================
-- 3. 用户标签关联表 user_tag
--    志愿者拥有的技能标签（多对多）
-- ============================================================
CREATE TABLE IF NOT EXISTS user_tag (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT   NOT NULL                COMMENT '用户ID',
    tag_id      BIGINT   NOT NULL                COMMENT '标签ID',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_tag (user_id, tag_id),
    KEY idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户技能标签关联表';


-- ============================================================
-- 4. 活动表 activity
-- ============================================================
CREATE TABLE IF NOT EXISTS activity (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    title           VARCHAR(200)  NOT NULL                COMMENT '活动标题',
    description     TEXT          DEFAULT NULL            COMMENT '活动描述',
    cover_url       VARCHAR(500)  DEFAULT NULL            COMMENT '封面图URL',
    location        VARCHAR(200)  DEFAULT NULL            COMMENT '活动地点',
    start_time      DATETIME      NOT NULL                COMMENT '活动开始时间',
    end_time        DATETIME      NOT NULL                COMMENT '活动结束时间',
    sign_start_time DATETIME      DEFAULT NULL            COMMENT '报名开始时间',
    sign_end_time   DATETIME      DEFAULT NULL            COMMENT '报名截止时间',
    total_quota     INT           NOT NULL DEFAULT 0      COMMENT '总招募人数',
    signed_count    INT           NOT NULL DEFAULT 0      COMMENT '已报名人数（冗余字段，提高查询效率）',
    status          TINYINT       NOT NULL DEFAULT 0      COMMENT '活动状态：0=草稿 1=报名中 2=进行中 3=已结束 4=已取消',
    creator_id      BIGINT        NOT NULL                COMMENT '创建人（活动管理员）ID',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_status_start (status, start_time),
    KEY idx_creator_id (creator_id),
    KEY idx_sign_end_time (sign_end_time),
    FULLTEXT KEY ft_title_desc (title, description) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';


-- ============================================================
-- 5. 活动标签关联表 activity_tag
--    活动所属类型标签（多对多）
-- ============================================================
CREATE TABLE IF NOT EXISTS activity_tag (
    id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    activity_id  BIGINT   NOT NULL                COMMENT '活动ID',
    tag_id       BIGINT   NOT NULL                COMMENT '标签ID',
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_tag (activity_id, tag_id),
    KEY idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动类型标签关联表';


-- ============================================================
-- 6. 岗位表 position
--    每个活动可以设置多个岗位
-- ============================================================
CREATE TABLE IF NOT EXISTS `position` (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
    activity_id     BIGINT       NOT NULL                COMMENT '所属活动ID',
    name            VARCHAR(100) NOT NULL                COMMENT '岗位名称',
    description     VARCHAR(500) DEFAULT NULL            COMMENT '岗位描述',
    quota           INT          NOT NULL DEFAULT 1      COMMENT '岗位招募人数',
    assigned_count  INT          NOT NULL DEFAULT 0      COMMENT '已派岗人数',
    required_hours  DECIMAL(5,2) DEFAULT 0.00           COMMENT '要求的最低志愿时长（小时）',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_activity_id (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动岗位表';


-- ============================================================
-- 7. 岗位技能标签关联表 position_tag
--    岗位需要的技能标签（多对多）
-- ============================================================
CREATE TABLE IF NOT EXISTS position_tag (
    id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    position_id  BIGINT   NOT NULL                COMMENT '岗位ID',
    tag_id       BIGINT   NOT NULL                COMMENT '标签ID',
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_position_tag (position_id, tag_id),
    KEY idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位技能标签关联表';


-- ============================================================
-- 8. 报名表 registration
-- ============================================================
CREATE TABLE IF NOT EXISTS registration (
    id           BIGINT    NOT NULL AUTO_INCREMENT COMMENT '报名ID',
    activity_id  BIGINT    NOT NULL                COMMENT '活动ID',
    user_id      BIGINT    NOT NULL                COMMENT '志愿者用户ID',
    position_id  BIGINT    DEFAULT NULL            COMMENT '派岗后的岗位ID（报名时为空，派岗后填入）',
    status       TINYINT   NOT NULL DEFAULT 0      COMMENT '报名状态：0=待审核 1=已通过 2=已拒绝 3=已派岗 4=已取消',
    remark           VARCHAR(500) DEFAULT NULL     COMMENT '志愿者报名留言',
    score            TINYINT   DEFAULT NULL         COMMENT '活动结束后活动方对志愿者的评分（1-5分）',
    activity_score   TINYINT   DEFAULT NULL         COMMENT '志愿者对活动的评分（1-5分）',
    activity_comment VARCHAR(500) DEFAULT NULL      COMMENT '志愿者对活动的文字评价',
    created_at   DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    updated_at   DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    KEY idx_user_id (user_id),
    KEY idx_position_id (position_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名表';


-- ============================================================
-- 9. 签到记录表 checkin
-- ============================================================
CREATE TABLE IF NOT EXISTS checkin (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '签到记录ID',
    activity_id     BIGINT        NOT NULL                COMMENT '活动ID',
    user_id         BIGINT        NOT NULL                COMMENT '志愿者用户ID',
    registration_id BIGINT        NOT NULL                COMMENT '关联的报名ID',
    checkin_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
    checkout_time   DATETIME      DEFAULT NULL            COMMENT '签退时间',
    duration_hours  DECIMAL(6,2)  DEFAULT NULL            COMMENT '本次参与时长（小时，签退后计算）',
    qr_token        VARCHAR(100)  NOT NULL                COMMENT '扫码时使用的二维码令牌（防重放）',
    is_late         TINYINT       NOT NULL DEFAULT 0      COMMENT '是否迟到：0=准时 1=迟到',
    hash_proof      VARCHAR(128)  DEFAULT NULL            COMMENT '时长哈希存证',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    KEY idx_user_id (user_id),
    KEY idx_registration_id (registration_id),
    KEY idx_checkin_time (checkin_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到记录表';


-- ============================================================
-- 10. 动态二维码令牌表 qr_token
--     存储当前有效的二维码令牌（Spring Task 每分钟刷新）
-- ============================================================
CREATE TABLE IF NOT EXISTS qr_token (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    activity_id  BIGINT       NOT NULL                COMMENT '活动ID',
    token        VARCHAR(100) NOT NULL                COMMENT '二维码令牌（UUID）',
    expire_time  DATETIME     NOT NULL                COMMENT '过期时间',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_token (activity_id),
    KEY idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态二维码令牌表';


-- ============================================================
-- 11. 成就表 achievement
--     预设成就里程碑（系统初始化时插入）
-- ============================================================
CREATE TABLE IF NOT EXISTS achievement (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '成就ID',
    name          VARCHAR(100) NOT NULL                COMMENT '成就名称',
    description   VARCHAR(500) DEFAULT NULL            COMMENT '成就描述',
    icon_url      VARCHAR(500) DEFAULT NULL            COMMENT '勋章图标URL',
    required_hours DECIMAL(8,2) NOT NULL DEFAULT 0.00  COMMENT '解锁所需时长（小时）',
    badge_level   TINYINT      NOT NULL DEFAULT 1      COMMENT '勋章等级：1=铜 2=银 3=金',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成就/勋章定义表';


-- ============================================================
-- 12. 用户成就关联表 user_achievement
--     志愿者已解锁的成就（触发器自动写入）
-- ============================================================
CREATE TABLE IF NOT EXISTS user_achievement (
    id             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id        BIGINT   NOT NULL                COMMENT '用户ID',
    achievement_id BIGINT   NOT NULL                COMMENT '成就ID',
    unlocked_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_achievement (user_id, achievement_id),
    KEY idx_achievement_id (achievement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户已解锁成就表';


-- ============================================================
-- 13. 哈希存证表 hash_proof
--     每条时长记录的区块链存证（防篡改）
-- ============================================================
CREATE TABLE IF NOT EXISTS hash_proof (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    checkin_id     BIGINT       NOT NULL                COMMENT '关联签到记录ID',
    user_id        BIGINT       NOT NULL                COMMENT '用户ID',
    activity_id    BIGINT       NOT NULL                COMMENT '活动ID',
    duration_hours DECIMAL(6,2) NOT NULL                COMMENT '时长（冗余，用于校验）',
    prev_hash      VARCHAR(128) DEFAULT NULL            COMMENT '前一条记录的哈希（链式结构）',
    curr_hash      VARCHAR(128) NOT NULL                COMMENT '当前记录哈希（SHA-256）',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '存证时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_checkin_id (checkin_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='哈希存证表';


-- ============================================================
-- 14. 活动操作日志表 operation_log（审计）
-- ============================================================
CREATE TABLE IF NOT EXISTS operation_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    operator_id  BIGINT       NOT NULL                COMMENT '操作人ID',
    module       VARCHAR(50)  NOT NULL                COMMENT '模块：USER/ACTIVITY/REGISTRATION/CHECKIN',
    operation    VARCHAR(100) NOT NULL                COMMENT '操作描述',
    target_id    BIGINT       DEFAULT NULL            COMMENT '操作对象ID',
    ip_address   VARCHAR(50)  DEFAULT NULL            COMMENT '操作IP',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_operator_id (operator_id),
    KEY idx_module (module),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';


-- ============================================================
-- 初始化数据
-- ============================================================

-- 初始化成就/勋章数据
INSERT INTO achievement (name, description, icon_url, required_hours, badge_level)
VALUES
    ('志愿新星',   '累计志愿时长达到 10 小时',  '/icons/badge_bronze.png', 10,  1),
    ('志愿先锋',   '累计志愿时长达到 50 小时',  '/icons/badge_silver.png', 50,  2),
    ('志愿之光',   '累计志愿时长达到 100 小时', '/icons/badge_gold.png',   100, 3);

-- 初始化常用技能标签
INSERT INTO tag (name, category)
VALUES
    ('摄影',       'SKILL'),
    ('英语翻译',   'SKILL'),
    ('急救知识',   'SKILL'),
    ('礼仪服务',   'SKILL'),
    ('IT技术',     'SKILL'),
    ('文字编辑',   'SKILL'),
    ('组织协调',   'SKILL'),
    ('驾驶技能',   'SKILL'),
    ('支教',       'ACTIVITY_TYPE'),
    ('环保公益',   'ACTIVITY_TYPE'),
    ('敬老助残',   'ACTIVITY_TYPE'),
    ('大型赛事',   'ACTIVITY_TYPE'),
    ('校园文化',   'ACTIVITY_TYPE'),
    ('医疗健康',   'ACTIVITY_TYPE');
