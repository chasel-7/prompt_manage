-- 提示词分组表
CREATE TABLE IF NOT EXISTS prompt_group (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL COMMENT '分组名称',
    description VARCHAR(500)  DEFAULT NULL COMMENT '分组描述',
    sort_order  INT           DEFAULT 0 COMMENT '排序序号',
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 标签表
CREATE TABLE IF NOT EXISTS tag (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- 提示词表
CREATE TABLE IF NOT EXISTS prompt (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200)  NOT NULL COMMENT '标题',
    content     TEXT          NOT NULL COMMENT '提示词内容',
    description VARCHAR(500)  DEFAULT NULL COMMENT '描述',
    group_id    BIGINT        DEFAULT NULL COMMENT '所属分组ID',
    is_favorite TINYINT       DEFAULT 0 COMMENT '是否收藏 0-否 1-是',
    is_deleted  TINYINT       DEFAULT 0 COMMENT '逻辑删除 0-否 1-是',
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 提示词-标签关联表
CREATE TABLE IF NOT EXISTS prompt_tag (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    prompt_id BIGINT NOT NULL COMMENT '提示词ID',
    tag_id    BIGINT NOT NULL COMMENT '标签ID',
    UNIQUE (prompt_id, tag_id)
);

-- 润色历史记录表
CREATE TABLE IF NOT EXISTS polishing_history (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_text   TEXT          NOT NULL COMMENT '原始文本',
    polished_text   TEXT          NOT NULL COMMENT '润色后文本',
    strategy        VARCHAR(50)   NOT NULL COMMENT '润色策略',
    strategy_name   VARCHAR(100)  NOT NULL COMMENT '策略名称',
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- 助手配置表
CREATE TABLE IF NOT EXISTS assistant (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)  NOT NULL COMMENT '助手名称',
    description     VARCHAR(500)  DEFAULT NULL COMMENT '助手描述',
    system_prompt   TEXT          NOT NULL COMMENT '系统提示词',
    icon            VARCHAR(50)   DEFAULT '🤖' COMMENT '图标emoji',
    is_deleted      TINYINT       DEFAULT 0 COMMENT '逻辑删除',
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 对话表
CREATE TABLE IF NOT EXISTS conversation (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    assistant_id    BIGINT        NOT NULL COMMENT '助手ID',
    title           VARCHAR(200)  DEFAULT '新对话' COMMENT '对话标题',
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 对话消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT        NOT NULL COMMENT '对话ID',
    role            VARCHAR(20)   NOT NULL COMMENT 'user/assistant/system',
    content         TEXT          NOT NULL COMMENT '消息内容',
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);
