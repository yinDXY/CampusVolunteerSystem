-- ============================================================
-- 校园志愿服务管理系统 - 初始化测试数据
-- 执行顺序：在 01_create_tables ~ 05_indexes 之后执行
-- 密码统一为 123456（BCrypt 加密）
-- ============================================================

USE volunteer_db;

-- ============================================================
-- 0. 清空旧数据（按外键依赖顺序删除）
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE checkin;
TRUNCATE TABLE user_achievement;
TRUNCATE TABLE registration;
TRUNCATE TABLE position_tag;
TRUNCATE TABLE position;
TRUNCATE TABLE activity_tag;
TRUNCATE TABLE activity;
TRUNCATE TABLE user_tag;
TRUNCATE TABLE qr_token;
TRUNCATE TABLE operation_log;
TRUNCATE TABLE hash_proof;
TRUNCATE TABLE sys_user;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. 用户数据（12人：1超管 + 2活动管理员 + 9志愿者）
--    密码：123456 → BCrypt hash
-- ============================================================
INSERT INTO sys_user (username, password, real_name, student_id, phone, email, role, total_hours, status) VALUES
-- 超级管理员
('admin',    '$2b$12$a5rhS6mWh5p5wjUwJiVXOOr96mO5IIdy3iddpSLWaRwy7uxbymuNq', '系统管理员',   NULL,         '13800000001', 'admin@volunteer.com',     2, 0,      1),
-- 活动管理员
('manager',  '$2b$12$a5rhS6mWh5p5wjUwJiVXOOr96mO5IIdy3iddpSLWaRwy7uxbymuNq', '李管理',       NULL,         '13800000002', 'manager@volunteer.com',   1, 0,      1),
('manager2', '$2b$12$a5rhS6mWh5p5wjUwJiVXOOr96mO5IIdy3iddpSLWaRwy7uxbymuNq', '王管理',       NULL,         '13800000003', 'manager2@volunteer.com',  1, 0,      1),
-- 志愿者（total_hours 手动设置，因为直接INSERT签到记录不走签退触发器）
('user',     '$2b$12$a5rhS6mWh5p5wjUwJiVXOOr96mO5IIdy3iddpSLWaRwy7uxbymuNq', '张志愿',       '2024001001', '13800138002', 'user@volunteer.com',      0, 120.00, 1),
('lihua',    '$2b$12$a5rhS6mWh5p5wjUwJiVXOOr96mO5IIdy3iddpSLWaRwy7uxbymuNq', '李华',         '2024001002', '13800138003', 'lihua@example.com',       0, 85.50,  1),
('wangwei',  '$2b$12$a5rhS6mWh5p5wjUwJiVXOOr96mO5IIdy3iddpSLWaRwy7uxbymuNq', '王伟',         '2024001003', '13800138004', 'wangwei@example.com',     0, 52.00,  1),
('zhaoli',   '$2b$12$a5rhS6mWh5p5wjUwJiVXOOr96mO5IIdy3iddpSLWaRwy7uxbymuNq', '赵丽',         '2024001004', '13800138005', 'zhaoli@example.com',      0, 38.75,  1),
('sunqiang', '$2b$12$a5rhS6mWh5p5wjUwJiVXOOr96mO5IIdy3iddpSLWaRwy7uxbymuNq', '孙强',         '2024001005', '13800138006', 'sunqiang@example.com',    0, 25.00,  1),
('zhoujie',  '$2b$12$a5rhS6mWh5p5wjUwJiVXOOr96mO5IIdy3iddpSLWaRwy7uxbymuNq', '周洁',         '2024001006', '13800138007', 'zhoujie@example.com',     0, 15.00,  1),
('wuming',   '$2b$12$a5rhS6mWh5p5wjUwJiVXOOr96mO5IIdy3iddpSLWaRwy7uxbymuNq', '吴明',         '2024001007', '13800138008', 'wuming@example.com',      0, 8.00,   1),
('chenxin',  '$2b$12$a5rhS6mWh5p5wjUwJiVXOOr96mO5IIdy3iddpSLWaRwy7uxbymuNq', '陈欣',         '2024001008', '13800138009', 'chenxin@example.com',     0, 3.50,   1),
('yangfan',  '$2b$12$a5rhS6mWh5p5wjUwJiVXOOr96mO5IIdy3iddpSLWaRwy7uxbymuNq', '杨帆',         '2024001009', '13800138010', 'yangfan@example.com',     0, 0.00,   1);


-- ============================================================
-- 2. 用户技能标签关联（志愿者绑定技能）
--    tag 表已在 01_create_tables.sql 中初始化：
--    1=摄影 2=英语翻译 3=急救知识 4=礼仪服务 5=IT技术
--    6=文字编辑 7=组织协调 8=驾驶技能
-- ============================================================
INSERT INTO user_tag (user_id, tag_id) VALUES
-- user(4): 摄影、急救知识、组织协调
(4,  1), (4,  3), (4,  7),
-- lihua(5): 英语翻译、文字编辑、IT技术
(5,  2), (5,  6), (5,  5),
-- wangwei(6): 摄影、驾驶技能
(6,  1), (6,  8),
-- zhaoli(7): 礼仪服务、英语翻译、文字编辑
(7,  4), (7,  2), (7,  6),
-- sunqiang(8): 急救知识、驾驶技能、组织协调
(8,  3), (8,  8), (8,  7),
-- zhoujie(9): IT技术、摄影
(9,  5), (9,  1),
-- wuming(10): 礼仪服务
(10, 4),
-- chenxin(11): 文字编辑、组织协调
(11, 6), (11, 7),
-- yangfan(12): 急救知识、英语翻译
(12, 3), (12, 2);


-- ============================================================
-- 3. 活动数据（8个活动，覆盖各种状态）
--    signed_count = 0，后续 INSERT registration 时触发器自动累加
-- ============================================================
INSERT INTO activity (id, title, description, location, start_time, end_time, sign_start_time, sign_end_time, total_quota, signed_count, status, creator_id) VALUES
-- 活动1：报名中（环保类）
(1,  '社区环境清洁志愿活动',
     '组织志愿者对社区公共区域进行清洁，美化社区环境，提升居民生活质量。活动将分组对小区花园、步道和公共设施进行全面清扫和维护。',
     '阳光社区', '2026-04-25 08:00:00', '2026-04-25 17:00:00',
     '2026-04-10 00:00:00', '2026-04-24 23:59:59', 30, 0, 1, 2),

-- 活动2：报名中（敬老助残类）
(2,  '关爱孤寡老人送温暖',
     '探访社区孤寡老人，提供生活帮助和心理慰藉，传递社会温暖。志愿者将分组入户，为老人整理家务、陪伴聊天、测量血压等。',
     '幸福社区', '2026-04-28 09:00:00', '2026-04-28 16:00:00',
     '2026-04-12 00:00:00', '2026-04-27 23:59:59', 15, 0, 1, 2),

-- 活动3：进行中（教育辅导类）
(3,  '儿童图书馆义务辅导',
     '为社区儿童提供课业辅导和阅读指导，培养良好的学习习惯。主要面向小学1-6年级学生，辅导语文、数学、英语等科目。',
     '市图书馆', '2026-04-20 09:00:00', '2026-05-20 17:00:00',
     '2026-04-01 00:00:00', '2026-04-19 23:59:59', 20, 0, 2, 2),

-- 活动4：报名中（应急救援类）
(4,  '校园交通安全宣传',
     '在学校周边开展交通安全宣传，提高学生交通安全意识。志愿者将在校门口和主要路口发放宣传资料、协助维护交通秩序。',
     '育才小学校', '2026-05-05 07:30:00', '2026-05-05 12:00:00',
     '2026-04-15 00:00:00', '2026-05-04 23:59:59', 25, 0, 1, 3),

-- 活动5：已结束（环保类）
(5,  '春季植树造林行动',
     '在城市绿化带进行义务植树，为城市增添一份绿色。活动由市园林局指导，提供树苗和工具，志愿者负责种植和浇水。',
     '城东绿化公园', '2026-03-12 08:00:00', '2026-03-12 16:00:00',
     '2026-02-25 00:00:00', '2026-03-11 23:59:59', 40, 0, 3, 2),

-- 活动6：已结束（医疗健康类）
(6,  '社区义诊健康服务',
     '联合医院志愿者团队为社区居民提供免费健康检查和医疗咨询，包括血压测量、血糖检测、健康知识宣教等。',
     '和谐社区活动中心', '2026-03-20 09:00:00', '2026-03-20 17:00:00',
     '2026-03-05 00:00:00', '2026-03-19 23:59:59', 15, 0, 3, 3),

-- 活动7：已结束（文化宣传类）
(7,  '校园文化艺术节志愿服务',
     '为学校文化艺术节提供全方位志愿服务，包括会场布置、观众引导、摄影记录、后勤保障等。',
     '大学生活动中心', '2026-03-28 08:00:00', '2026-03-30 21:00:00',
     '2026-03-10 00:00:00', '2026-03-27 23:59:59', 50, 0, 3, 2),

-- 活动8：草稿（尚未发布）
(8,  '暑期支教志愿行动（筹备中）',
     '计划暑假期间前往山区小学开展为期两周的支教活动，教授语文、数学、英语、美术、音乐等课程。',
     '云南省大理州某村小学', '2026-07-15 08:00:00', '2026-07-29 17:00:00',
     NULL, NULL, 30, 0, 0, 2);


-- ============================================================
-- 4. 活动标签关联（给活动贴类型标签）
--    ACTIVITY_TYPE tags: 9=支教 10=环保公益 11=敬老助残 12=大型赛事 13=校园文化 14=医疗健康
-- ============================================================
INSERT INTO activity_tag (activity_id, tag_id) VALUES
(1, 10),  -- 社区清洁 → 环保公益
(2, 11),  -- 关爱老人 → 敬老助残
(3,  9),  -- 儿童辅导 → 支教
(4, 10),  -- 交通安全 → 环保公益（广义社会公益）
(5, 10),  -- 植树造林 → 环保公益
(6, 14),  -- 社区义诊 → 医疗健康
(7, 13),  -- 文化艺术节 → 校园文化
(8,  9);  -- 暑期支教 → 支教


-- ============================================================
-- 5. 岗位数据（每个活动 2-3 个岗位）
--    assigned_count 手动设置（因 INSERT registration 不触发派岗触发器）
-- ============================================================
INSERT INTO `position` (id, activity_id, name, description, quota, assigned_count, required_hours) VALUES
-- 活动1：社区清洁
(1,  1, '清洁组志愿者', '负责清理社区道路、绿化带等公共区域',              20, 2, 0),
(2,  1, '宣传组志愿者', '负责环保宣传和活动拍摄记录',                      10, 0, 5),

-- 活动2：关爱老人
(3,  2, '陪护志愿者',   '陪伴老人聊天，帮助打扫卫生',                      10, 1, 0),
(4,  2, '医护志愿者',   '提供简单的健康检查和咨询',                         5, 0, 10),

-- 活动3：儿童辅导
(5,  3, '学科辅导员',   '辅导小学生各科作业',                              15, 5, 0),
(6,  3, '阅读指导员',   '指导儿童阅读，开展读书活动',                       5, 2, 0),

-- 活动4：交通安全
(7,  4, '宣传员',       '发放宣传资料，讲解交通规则',                      20, 0, 0),
(8,  4, '协管员',       '协助维护学校周边交通秩序',                         5, 0, 5),

-- 活动5：植树造林（已结束）
(9,  5, '种植组',       '负责挖坑、种植、浇水',                            30, 8, 0),
(10, 5, '后勤组',       '负责工具分发和午餐供应',                          10, 3, 0),

-- 活动6：社区义诊（已结束）
(11, 6, '导诊志愿者',   '引导居民有序就诊，维护现场秩序',                  10, 4, 0),
(12, 6, '健康宣教员',   '发放健康知识手册，讲解常见病预防',                  5, 2, 0),

-- 活动7：文化艺术节（已结束）
(13, 7, '会场布置组',   '负责舞台搭建和会场装饰',                          15, 5, 0),
(14, 7, '摄影记者组',   '负责活动全程摄影和视频拍摄',                       5, 3, 5),
(15, 7, '观众引导组',   '引导观众入座，维持现场秩序',                      20, 6, 0),
(16, 7, '后勤保障组',   '负责饮用水供应和应急处理',                        10, 3, 0),

-- 活动8：暑期支教（草稿）
(17, 8, '语文教师',     '教授语文课程和写作指导',                          10, 0, 20),
(18, 8, '数学教师',     '教授数学课程',                                    10, 0, 20),
(19, 8, '英语教师',     '教授英语口语和基础课程',                          10, 0, 20);


-- ============================================================
-- 6. 岗位技能标签需求
--    SKILL tags: 1=摄影 2=英语翻译 3=急救知识 4=礼仪服务
--                5=IT技术 6=文字编辑 7=组织协调 8=驾驶技能
-- ============================================================
INSERT INTO position_tag (position_id, tag_id) VALUES
-- 宣传组志愿者(2) 需要：摄影、文字编辑
(2, 1), (2, 6),
-- 医护志愿者(4) 需要：急救知识
(4, 3),
-- 协管员(8) 需要：组织协调
(8, 7),
-- 后勤组(10) 需要：驾驶技能
(10, 8),
-- 导诊志愿者(11) 需要：礼仪服务
(11, 4),
-- 摄影记者组(14) 需要：摄影
(14, 1),
-- 后勤保障组(16) 需要：急救知识、驾驶技能
(16, 3), (16, 8),
-- 英语教师(19) 需要：英语翻译
(19, 2);


-- ============================================================
-- 7. 报名记录
--    注意：INSERT 触发器会自动累加 activity.signed_count
--    状态：0=待审核 1=已通过 2=已拒绝 3=已派岗 4=已取消
-- ============================================================

-- ── 活动1：社区清洁（报名中）──
INSERT INTO registration (activity_id, user_id, position_id, status, remark, score, created_at) VALUES
(1, 4,  1, 3, '我有社区服务经验',   NULL, '2026-04-15 10:30:00'),  -- user → 清洁组，已派岗
(1, 5,  1, 3, '希望为社区出一份力', NULL, '2026-04-15 14:20:00'),  -- lihua → 清洁组，已派岗
(1, 6,  NULL, 1, '周末有空',         NULL, '2026-04-16 09:00:00'),  -- wangwei → 已通过
(1, 7,  NULL, 0, '想参加环保活动',   NULL, '2026-04-17 11:30:00'),  -- zhaoli → 待审核
(1, 8,  NULL, 0, NULL,               NULL, '2026-04-18 08:45:00');  -- sunqiang → 待审核

-- ── 活动2：关爱老人（报名中）──
INSERT INTO registration (activity_id, user_id, position_id, status, remark, score, created_at) VALUES
(2, 4,  3, 3, '有敬老院志愿经验',   NULL, '2026-04-16 14:20:00'),  -- user → 陪护，已派岗
(2, 7,  NULL, 1, '喜欢和老人交流',   NULL, '2026-04-17 10:00:00'),  -- zhaoli → 已通过
(2, 9,  NULL, 0, NULL,               NULL, '2026-04-18 15:30:00'),  -- zhoujie → 待审核
(2, 11, NULL, 0, '希望帮助老人',     NULL, '2026-04-18 16:00:00');  -- chenxin → 待审核

-- ── 活动3：儿童辅导（进行中，已满）──
INSERT INTO registration (activity_id, user_id, position_id, status, remark, score, created_at) VALUES
(3, 4,  5,  3, '有家教经验',                 NULL, '2026-04-05 09:00:00'),
(3, 5,  5,  3, '英语专业，可辅导英语',       NULL, '2026-04-05 10:00:00'),
(3, 6,  5,  3, NULL,                          NULL, '2026-04-06 08:30:00'),
(3, 7,  5,  3, '喜欢和小朋友相处',           NULL, '2026-04-06 11:00:00'),
(3, 8,  5,  3, NULL,                          NULL, '2026-04-07 09:15:00'),
(3, 9,  6,  3, '热爱阅读推广',               NULL, '2026-04-07 14:00:00'),
(3, 10, 6,  3, NULL,                          NULL, '2026-04-08 10:00:00');

-- ── 活动4：交通安全（报名中）──
INSERT INTO registration (activity_id, user_id, position_id, status, remark, score, created_at) VALUES
(4, 5,  NULL, 0, '想为学生安全贡献力量',     NULL, '2026-04-18 10:00:00'),
(4, 8,  NULL, 0, NULL,                        NULL, '2026-04-18 11:00:00'),
(4, 12, NULL, 0, '有交通志愿经验',           NULL, '2026-04-19 08:00:00');

-- ── 活动5：植树造林（已结束）──
INSERT INTO registration (activity_id, user_id, position_id, status, remark, score, created_at) VALUES
(5, 4,  9,  3, NULL, 5, '2026-02-28 10:00:00'),
(5, 5,  9,  3, NULL, 4, '2026-02-28 11:00:00'),
(5, 6,  9,  3, NULL, 5, '2026-02-28 14:00:00'),
(5, 7,  9,  3, NULL, 4, '2026-03-01 09:00:00'),
(5, 8,  9,  3, NULL, 5, '2026-03-01 10:00:00'),
(5, 9,  9,  3, NULL, 3, '2026-03-02 08:00:00'),
(5, 10, 9,  3, NULL, 4, '2026-03-02 09:00:00'),
(5, 11, 9,  3, NULL, 4, '2026-03-03 10:00:00'),
(5, 12, 10, 3, NULL, 3, '2026-03-03 11:00:00');

-- ── 活动6：社区义诊（已结束）──
INSERT INTO registration (activity_id, user_id, position_id, status, remark, score, created_at) VALUES
(6, 4,  11, 3, '有志愿服务经验',     5, '2026-03-08 09:00:00'),
(6, 7,  11, 3, '礼仪服务专业',       4, '2026-03-08 10:00:00'),
(6, 8,  11, 3, '急救知识丰富',       5, '2026-03-09 08:00:00'),
(6, 9,  11, 3, NULL,                  4, '2026-03-09 09:00:00'),
(6, 10, 12, 3, NULL,                  4, '2026-03-10 10:00:00'),
(6, 11, 12, 3, '文字功底好',         3, '2026-03-10 11:00:00');

-- ── 活动7：文化艺术节（已结束）──
INSERT INTO registration (activity_id, user_id, position_id, status, remark, score, created_at) VALUES
(7, 4,  14, 3, '有摄影经验',         5, '2026-03-15 09:00:00'),
(7, 5,  13, 3, NULL,                  4, '2026-03-15 10:00:00'),
(7, 6,  13, 3, NULL,                  5, '2026-03-15 11:00:00'),
(7, 7,  15, 3, '有礼仪服务经验',     4, '2026-03-16 08:00:00'),
(7, 8,  15, 3, NULL,                  5, '2026-03-16 09:00:00'),
(7, 9,  14, 3, '会使用专业相机',     4, '2026-03-16 10:00:00'),
(7, 10, 15, 3, NULL,                  3, '2026-03-17 08:00:00'),
(7, 11, 13, 3, NULL,                  4, '2026-03-17 09:00:00'),
(7, 12, 15, 3, NULL,                  3, '2026-03-17 10:00:00');


-- ============================================================
-- 8. 签到记录（已结束活动的签到/签退数据）
--    直接INSERT，不会触发签退触发器（触发器监听UPDATE，不监听INSERT）
--    registration_id 通过子查询获取，确保正确关联
-- ============================================================

-- ── 活动5：植树造林 ──
INSERT INTO checkin (activity_id, user_id, registration_id, checkin_time, checkout_time, duration_hours, qr_token, is_late)
SELECT 5, 4,  id, '2026-03-12 08:05:00', '2026-03-12 16:00:00', 7.92, 'token-5-init-1', 0 FROM registration WHERE activity_id=5 AND user_id=4
UNION ALL
SELECT 5, 5,  id, '2026-03-12 08:10:00', '2026-03-12 15:50:00', 7.67, 'token-5-init-2', 0 FROM registration WHERE activity_id=5 AND user_id=5
UNION ALL
SELECT 5, 6,  id, '2026-03-12 08:15:00', '2026-03-12 16:10:00', 7.92, 'token-5-init-3', 0 FROM registration WHERE activity_id=5 AND user_id=6
UNION ALL
SELECT 5, 7,  id, '2026-03-12 08:30:00', '2026-03-12 15:30:00', 7.00, 'token-5-init-4', 1 FROM registration WHERE activity_id=5 AND user_id=7
UNION ALL
SELECT 5, 8,  id, '2026-03-12 08:00:00', '2026-03-12 16:00:00', 8.00, 'token-5-init-5', 0 FROM registration WHERE activity_id=5 AND user_id=8
UNION ALL
SELECT 5, 9,  id, '2026-03-12 08:20:00', '2026-03-12 14:00:00', 5.67, 'token-5-init-6', 0 FROM registration WHERE activity_id=5 AND user_id=9
UNION ALL
SELECT 5, 10, id, '2026-03-12 08:45:00', '2026-03-12 15:00:00', 6.25, 'token-5-init-7', 1 FROM registration WHERE activity_id=5 AND user_id=10
UNION ALL
SELECT 5, 11, id, '2026-03-12 08:00:00', '2026-03-12 12:00:00', 4.00, 'token-5-init-8', 0 FROM registration WHERE activity_id=5 AND user_id=11;

-- ── 活动6：社区义诊 ──
INSERT INTO checkin (activity_id, user_id, registration_id, checkin_time, checkout_time, duration_hours, qr_token, is_late)
SELECT 6, 4,  id, '2026-03-20 08:55:00', '2026-03-20 17:00:00', 8.08, 'token-6-init-1', 0 FROM registration WHERE activity_id=6 AND user_id=4
UNION ALL
SELECT 6, 7,  id, '2026-03-20 09:10:00', '2026-03-20 16:30:00', 7.33, 'token-6-init-2', 1 FROM registration WHERE activity_id=6 AND user_id=7
UNION ALL
SELECT 6, 8,  id, '2026-03-20 09:00:00', '2026-03-20 17:05:00', 8.08, 'token-6-init-3', 0 FROM registration WHERE activity_id=6 AND user_id=8
UNION ALL
SELECT 6, 9,  id, '2026-03-20 09:05:00', '2026-03-20 16:00:00', 6.92, 'token-6-init-4', 0 FROM registration WHERE activity_id=6 AND user_id=9
UNION ALL
SELECT 6, 10, id, '2026-03-20 09:00:00', '2026-03-20 15:00:00', 6.00, 'token-6-init-5', 0 FROM registration WHERE activity_id=6 AND user_id=10
UNION ALL
SELECT 6, 11, id, '2026-03-20 09:30:00', '2026-03-20 14:00:00', 4.50, 'token-6-init-6', 1 FROM registration WHERE activity_id=6 AND user_id=11;

-- ── 活动7：文化艺术节 ──
INSERT INTO checkin (activity_id, user_id, registration_id, checkin_time, checkout_time, duration_hours, qr_token, is_late)
SELECT 7, 4,  id, '2026-03-28 07:50:00', '2026-03-30 21:00:00', 8.00, 'token-7-init-1', 0 FROM registration WHERE activity_id=7 AND user_id=4
UNION ALL
SELECT 7, 5,  id, '2026-03-28 08:00:00', '2026-03-30 20:30:00', 8.00, 'token-7-init-2', 0 FROM registration WHERE activity_id=7 AND user_id=5
UNION ALL
SELECT 7, 6,  id, '2026-03-28 08:10:00', '2026-03-30 21:00:00', 8.00, 'token-7-init-3', 0 FROM registration WHERE activity_id=7 AND user_id=6
UNION ALL
SELECT 7, 7,  id, '2026-03-28 08:00:00', '2026-03-30 20:00:00', 7.50, 'token-7-init-4', 0 FROM registration WHERE activity_id=7 AND user_id=7
UNION ALL
SELECT 7, 8,  id, '2026-03-28 08:15:00', '2026-03-30 20:30:00', 8.00, 'token-7-init-5', 0 FROM registration WHERE activity_id=7 AND user_id=8
UNION ALL
SELECT 7, 9,  id, '2026-03-28 08:30:00', '2026-03-30 19:00:00', 7.00, 'token-7-init-6', 1 FROM registration WHERE activity_id=7 AND user_id=9
UNION ALL
SELECT 7, 10, id, '2026-03-28 09:00:00', '2026-03-30 18:00:00', 6.00, 'token-7-init-7', 1 FROM registration WHERE activity_id=7 AND user_id=10
UNION ALL
SELECT 7, 11, id, '2026-03-28 08:00:00', '2026-03-30 20:00:00', 7.50, 'token-7-init-8', 0 FROM registration WHERE activity_id=7 AND user_id=11
UNION ALL
SELECT 7, 12, id, '2026-03-28 08:20:00', '2026-03-30 17:00:00', 5.50, 'token-7-init-9', 0 FROM registration WHERE activity_id=7 AND user_id=12;


-- ============================================================
-- 9. 手动插入成就解锁（直接INSERT不走触发器）
--    achievement: 1=志愿新星(10h) 2=志愿先锋(50h) 3=志愿之光(100h)
-- ============================================================
INSERT INTO user_achievement (user_id, achievement_id, unlocked_at) VALUES
-- user(4): 120h → 三个全解锁
(4, 1, '2026-02-15 10:00:00'),
(4, 2, '2026-03-10 15:00:00'),
(4, 3, '2026-03-28 20:00:00'),
-- lihua(5): 85.5h → 新星 + 先锋
(5, 1, '2026-02-20 12:00:00'),
(5, 2, '2026-03-15 16:00:00'),
-- wangwei(6): 52h → 新星 + 先锋
(6, 1, '2026-03-01 09:00:00'),
(6, 2, '2026-03-20 14:00:00'),
-- zhaoli(7): 38.75h → 新星
(7, 1, '2026-03-05 11:00:00'),
-- sunqiang(8): 25h → 新星
(8, 1, '2026-03-10 09:00:00'),
-- zhoujie(9): 15h → 新星
(9, 1, '2026-03-15 10:00:00');
-- wuming(10): 8h → 未解锁
-- chenxin(11): 3.5h → 未解锁
-- yangfan(12): 0h → 未解锁

-- ============================================================
-- 10. 修正计数字段（若触发器未加载则手动同步）
-- ============================================================
UPDATE activity a SET signed_count = (SELECT COUNT(*) FROM registration r WHERE r.activity_id = a.id AND r.status NOT IN (2, 4));
UPDATE `position` p SET assigned_count = (SELECT COUNT(*) FROM registration r WHERE r.position_id = p.id AND r.status = 3);
