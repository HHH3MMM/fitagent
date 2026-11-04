-- ============================================================
-- Fitness Agent 建表脚本（MyBatis-Plus 不自动建表，启动时由 spring.sql.init 执行）
-- CREATE TABLE IF NOT EXISTS：重复启动不报错；users 表已存在，不重建
-- 关键设计：UNIQUE(user_id, record_date) 从数据库层兜底"同一天只允许一条记录"，
-- 即使并发请求同时打进来，数据库唯一索引也会拒绝第二条——这就是数据库约束的价值。
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(100) NOT NULL,
    nickname    VARCHAR(50),
    goal        VARCHAR(20),
    height      DOUBLE,
    created_at  DATETIME,
    updated_at  DATETIME,
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '用户表';

CREATE TABLE IF NOT EXISTS checkins (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT      NOT NULL,
    checkin_date DATE        NOT NULL COMMENT '打卡日期',
    note         VARCHAR(200) COMMENT '打卡备注，如：练了腿 1 小时',
    created_at   DATETIME,
    updated_at   DATETIME,
    UNIQUE KEY uk_user_date (user_id, checkin_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '每日打卡表';

CREATE TABLE IF NOT EXISTS body_metrics (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT   NOT NULL,
    record_date DATE     NOT NULL COMMENT '记录日期',
    weight      DOUBLE COMMENT '体重 kg',
    chest       DOUBLE COMMENT '胸围 cm',
    waist       DOUBLE COMMENT '腰围 cm',
    hip         DOUBLE COMMENT '臀围 cm',
    body_fat    DOUBLE COMMENT '体脂率 %',
    note        VARCHAR(200),
    created_at  DATETIME,
    updated_at  DATETIME,
    UNIQUE KEY uk_user_date (user_id, record_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '身体三维记录表';

CREATE TABLE IF NOT EXISTS plans (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    name       VARCHAR(100) NOT NULL,
    goal       VARCHAR(20) COMMENT '训练目标：减脂/增肌/塑形/保持',
    start_date DATE,
    end_date   DATE,
    status     VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE 进行中 / DONE 已完成',
    note       VARCHAR(500),
    created_at DATETIME,
    updated_at DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '训练计划表';

CREATE TABLE IF NOT EXISTS plan_items (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id     BIGINT       NOT NULL,
    day_of_week INT          NOT NULL COMMENT '周几训练：1=周一 ... 7=周日',
    exercise    VARCHAR(100) NOT NULL COMMENT '动作名称',
    sets        INT,
    reps        INT,
    weight      DOUBLE COMMENT '负重 kg',
    note        VARCHAR(200),
    created_at  DATETIME,
    updated_at  DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '训练计划明细表';
