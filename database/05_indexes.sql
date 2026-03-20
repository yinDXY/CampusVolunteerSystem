USE volunteer_db;

-- ============================================================
-- 一、补充复合索引
-- ============================================================

-- 1. activity 表：按状态+开始时间查询（活动大厅列表、状态筛选）
--    -> 已在建表时定义 KEY idx_status_start (status, start_time)

-- 2. activity 表：按报名截止时间查询（后端定时任务关闭报名）
--    -> 已在建表时定义 KEY idx_sign_end_time (sign_end_time)

-- 3. registration 表：按用户+状态查询（个人中心报名记录）
ALTER TABLE registration
    ADD INDEX idx_user_status (user_id, status);

-- 4. registration 表：按活动+状态查询（管理员查看某活动的报名列表）
ALTER TABLE registration
    ADD INDEX idx_activity_status (activity_id, status);

-- 5. checkin 表：按用户+时间查询（个人签到历史）
ALTER TABLE checkin
    ADD INDEX idx_user_checkin_time (user_id, checkin_time);

-- 6. checkin 表：按活动+未签退查询（活动管理员实时监控）
ALTER TABLE checkin
    ADD INDEX idx_activity_checkout (activity_id, checkout_time);

-- 7. sys_user 表：按时长降序查询（排行榜）
ALTER TABLE sys_user
    ADD INDEX idx_total_hours (total_hours DESC);

-- 8. user_tag / position_tag：已有 UNIQUE KEY，覆盖正向查询
--    补充反向查询（通过 tag_id 找用户 / 岗位，用于智能派岗）
--    -> 已在建表时定义 KEY idx_tag_id (tag_id)

-- 9. hash_proof 表：按 curr_hash 查询（存证校验接口）
ALTER TABLE hash_proof
    ADD INDEX idx_curr_hash (curr_hash(32));

-- ============================================================
-- 二、索引设计说明（答辩重点）
-- ============================================================
/*
┌──────────────────────────────────────────────────────────────────────────┐
│ 表名           │ 索引字段                    │ 设计原因                   │
├──────────────────────────────────────────────────────────────────────────┤
│ sys_user       │ username (UNIQUE)           │ 登录查询，高频等值查询      │
│ sys_user       │ role                        │ 按角色筛选用户              │
│ sys_user       │ student_id                  │ 以学号检索用户              │
│ sys_user       │ total_hours DESC            │ 排行榜 ORDER BY 优化        │
├──────────────────────────────────────────────────────────────────────────┤
│ activity       │ status + start_time         │ 活动大厅主查询（复合索引）  │
│ activity       │ creator_id                  │ 管理员查自己的活动          │
│ activity       │ sign_end_time               │ 定时任务关闭报名扫描        │
│ activity       │ FULLTEXT(title, description)│ 全文搜索活动关键词          │
├──────────────────────────────────────────────────────────────────────────┤
│ registration   │ activity_id + user_id(UK)   │ 防重复报名、精确查询        │
│ registration   │ user_id + status            │ 个人中心筛选报名状态        │
│ registration   │ activity_id + status        │ 管理员查活动报名列表        │
├──────────────────────────────────────────────────────────────────────────┤
│ checkin        │ activity_id + user_id(UK)   │ 防重复签到                  │
│ checkin        │ user_id + checkin_time      │ 个人签到历史时间范围查询    │
│ checkin        │ activity_id + checkout_time │ 活动实时签到监控            │
├──────────────────────────────────────────────────────────────────────────┤
│ user_tag       │ user_id + tag_id (UK)       │ 用户技能标签正向查询        │
│ user_tag       │ tag_id                      │ 智能派岗：标签反向匹配      │
│ position_tag   │ position_id + tag_id (UK)   │ 岗位需求标签正向查询        │
│ position_tag   │ tag_id                      │ 智能派岗：标签反向匹配      │
├──────────────────────────────────────────────────────────────────────────┤
│ qr_token       │ activity_id (UNIQUE)        │ 每个活动只有一个令牌，快速查│
│ qr_token       │ expire_time                 │ 定时清理过期令牌扫描        │
├──────────────────────────────────────────────────────────────────────────┤
│ hash_proof     │ checkin_id (UNIQUE)         │ 精确查找某签到的存证        │
│ hash_proof     │ user_id                     │ 查询某用户全部存证记录      │
│ hash_proof     │ curr_hash (前缀索引)         │ 哈希校验接口查询            │
└──────────────────────────────────────────────────────────────────────────┘

【索引选型策略】
1. 高频等值查询（username、student_id）→ UNIQUE / 普通索引
2. 范围+排序组合（status+start_time、user_id+checkin_time）→ 复合索引（左前缀原则）
3. 全文检索（活动搜索）→ FULLTEXT ngram（支持中文分词）
4. 防重复约束（activity+user）→ UNIQUE KEY 兼作索引
5. 大字段哈希值只建前缀索引（curr_hash(32)），避免索引过大

【注意事项】
- total_hours 为 DECIMAL 类型，DESC 索引在排行榜场景下避免 filesort
- 逻辑删除字段 deleted 不单独建索引；在高频查询字段上的复合索引中，
  deleted 作为过滤条件由覆盖索引间接处理
- FULLTEXT 索引需 MySQL 配置 innodb_ft_min_token_size=2（中文短词）
*/
