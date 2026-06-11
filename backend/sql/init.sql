-- 创建数据库
CREATE DATABASE IF NOT EXISTS `smart_resume` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `smart_resume`;

-- 1. 用户表
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(64)  NOT NULL COMMENT '用户名',
    `userAccount` VARCHAR(64)  NOT NULL COMMENT '账号（登录用）',
    `userPassword` VARCHAR(128) NOT NULL COMMENT '密码（加密存储）',
    `avatarUrl`   VARCHAR(512) DEFAULT NULL COMMENT '头像 URL',
    `gender`      TINYINT      DEFAULT 0 COMMENT '性别：0=女 1=男',
    `email`       VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `userRole`    INT          DEFAULT 0 COMMENT '角色：0=普通用户 1=管理员',
    `status`      INT          DEFAULT 0 COMMENT '状态：0=正常 1=禁用',
    `createTime`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_userAccount` (`userAccount`)
) ENGINE=InnoDB COMMENT='用户表';

-- 1.1 remember-me 长期登录 token 表
CREATE TABLE `remember_login_token` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId`        BIGINT       NOT NULL COMMENT '所属用户 ID',
    `selector`      VARCHAR(64)  NOT NULL COMMENT '公开索引标识',
    `validatorHash` VARCHAR(255) NOT NULL COMMENT '长期登录校验凭证哈希',
    `expiresAt`     DATETIME     NOT NULL COMMENT '过期时间',
    `lastUsedAt`    DATETIME     DEFAULT NULL COMMENT '最近一次使用时间',
    `createdAt`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updatedAt`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `revoked`       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已撤销：0=否 1=是',
    `userAgent`     VARCHAR(512) DEFAULT NULL COMMENT '客户端 User-Agent',
    `clientIp`      VARCHAR(64)  DEFAULT NULL COMMENT '客户端 IP',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_selector` (`selector`),
    KEY `idx_userId` (`userId`)
) ENGINE=InnoDB COMMENT='remember-me 长期登录凭证表';

-- 2. 简历主表
CREATE TABLE `resume` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId`          BIGINT       NOT NULL COMMENT '所属用户 ID',
    `title`           VARCHAR(256) NOT NULL COMMENT '简历标题',
    `status`          INT          NOT NULL DEFAULT 0 COMMENT '状态：0=草稿 1=已完成',
    `currentTemplate` VARCHAR(64)  NOT NULL DEFAULT 'classic' COMMENT '当前模板标识',
    `createTime`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_userId` (`userId`)
) ENGINE=InnoDB COMMENT='简历主表';

# 把模板配置相关信息，从basic模块里面剥离出来，这是最开始设计的缺陷，现在要来大改
ALTER TABLE resume
    ADD COLUMN styleConfig TEXT NULL COMMENT '简历全局样式配置 JSON';

-- 3. 简历内容表
CREATE TABLE `resume_content` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `resumeId`    BIGINT      NOT NULL COMMENT '关联简历 ID',
    `moduleType`  VARCHAR(32) NOT NULL COMMENT '模块类型：basic/education/experience/project/skill/personalStrengths',
    `contentJson` TEXT        COMMENT '该模块的 JSON 数据',
    `sortOrder`   INT         NOT NULL DEFAULT 0 COMMENT '排序',
    `createTime`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_resumeId` (`resumeId`)
) ENGINE=InnoDB COMMENT='简历内容表';

# 增量同步逻辑
ALTER TABLE resume_content
    ADD UNIQUE KEY uk_resume_module (resumeId, moduleType);

-- 4. 简历版本表
CREATE TABLE `resume_version` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `resumeId`     BIGINT       NOT NULL COMMENT '关联简历 ID',
    `versionNum`   INT          NOT NULL COMMENT '版本号',
    `snapshotJson` LONGTEXT     NOT NULL COMMENT '简历完整快照 JSON',
    `remark`       VARCHAR(256) DEFAULT NULL COMMENT '版本备注',
    `createTime`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_resumeId` (`resumeId`)
) ENGINE=InnoDB COMMENT='简历版本表';

-- 5. 简历分享表
CREATE TABLE `resume_share` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `resumeId`   BIGINT      NOT NULL COMMENT '关联简历 ID',
    `userId`     BIGINT      NOT NULL COMMENT '分享者用户 ID',
    `shareKey`   VARCHAR(64) NOT NULL COMMENT '唯一分享标识（UUID）',
    `password`   VARCHAR(64) DEFAULT NULL COMMENT '访问密码（MD5）',
    `expireTime` DATETIME    DEFAULT NULL COMMENT '过期时间',
    `viewCount`  INT         NOT NULL DEFAULT 0 COMMENT '访问次数',
    `createTime` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `isDelete`   TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shareKey` (`shareKey`),
    KEY `idx_userId` (`userId`)
) ENGINE=InnoDB COMMENT='简历分享表';

ALTER TABLE resume_share
    ADD COLUMN sourceType VARCHAR(32) DEFAULT 'current' COMMENT '来源类型：current=当前简历 version=历史版本',
    ADD COLUMN sourceVersionId BIGINT DEFAULT NULL COMMENT '来源历史版本 ID',
    ADD COLUMN sourceVersionNum INT DEFAULT NULL COMMENT '来源版本号',
    ADD COLUMN snapshotJson LONGTEXT NOT NULL COMMENT '分享快照内容';

ALTER TABLE `resume_share`
    ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=启用 0=关闭' AFTER `viewCount`,
    ADD COLUMN `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER
        `createTime`,
    ADD KEY `idx_resumeId` (`resumeId`);

ALTER TABLE resume_share
    ADD COLUMN expireDays INT NULL COMMENT '分享有效期天数，0 表示永久有效';


-- 6. AI 对话记录表
CREATE TABLE `resume_chat` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `resumeId`   BIGINT      NOT NULL COMMENT '关联简历 ID',
    `userId`     BIGINT      NOT NULL COMMENT '用户 ID',
    `role`       VARCHAR(16) NOT NULL COMMENT '角色：user/assistant',
    `content`    TEXT        COMMENT '对话内容',
    `createTime` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_resumeId` (`resumeId`)
) ENGINE=InnoDB COMMENT='AI 对话记录表';

-- 7. 主动提醒消息表
CREATE TABLE `resume_notice` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId`     BIGINT      NOT NULL COMMENT '用户 ID',
    `resumeId`   BIGINT      DEFAULT NULL COMMENT '关联简历 ID',
    `type`       VARCHAR(32) NOT NULL COMMENT '类型：update_remind/optimize_suggest/completeness_check',
    `title`      VARCHAR(256) NOT NULL COMMENT '提醒标题',
    `content`    TEXT        COMMENT '提醒内容（AI 生成的建议）',
    `isRead`     TINYINT     NOT NULL DEFAULT 0 COMMENT '是否已读：0=未读 1=已读',
    `createTime` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_userId` (`userId`)
) ENGINE=InnoDB COMMENT='主动提醒消息表';

ALTER TABLE resume_version ADD COLUMN userId BIGINT NOT NULL COMMENT '所属用户 ID' AFTER resumeId;
ALTER TABLE resume_version ADD INDEX idx_userId (userId);

ALTER TABLE resume_notice
    ADD COLUMN resumeVersionNum INT NULL COMMENT '生成该通知时对应的简历版本号';

-- 8. 用户记忆表
CREATE TABLE user_memory (
                             id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                             userId      BIGINT NOT NULL,       -- 关联 user.id
                             category    VARCHAR(64) NOT NULL,  -- 记忆分类：preference（用户偏好）/ skill（技能画像）/ career（职业倾向）/ habit（修改习惯）
                             content     TEXT NOT NULL,         -- 记忆内容（自然语言描述）
                             source      VARCHAR(64),           -- 来源：auto（AI 自动提取）/ manual（用户手动）
                             updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             createTime  DATETIME DEFAULT CURRENT_TIMESTAMP,
                             INDEX idx_user_id (userId),
                             INDEX idx_category (userId, category)
);

-- 8. 记录用户消息通知偏好
ALTER TABLE user ADD COLUMN noticeEnabled TINYINT DEFAULT 0;

-- 8. 简历邮件发送日志表
CREATE TABLE `resume_email_log` (
                                    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                                    `userId`      BIGINT       NOT NULL COMMENT '用户 ID',
                                    `resumeId`    BIGINT       NOT NULL COMMENT '触发邮件的简历 ID',
                                    `type`        VARCHAR(64)  NOT NULL COMMENT '邮件类型：resume_recall',
                                    `subject`     VARCHAR(255) DEFAULT NULL COMMENT '邮件主题',
                                    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '发送状态：1成功 0失败',
                                    `errorMsg`    VARCHAR(512) DEFAULT NULL COMMENT '失败原因',
                                    `createTime`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_user_type_time` (`userId`, `type`, `createTime`),
                                    KEY `idx_resume_time` (`resumeId`, `createTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历邮件发送日志表';


-- 改了数据库存储的一轮（大改），所以需要清表
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE resume_notice;
TRUNCATE TABLE resume_share;
TRUNCATE TABLE resume_version;
TRUNCATE TABLE resume_content;
TRUNCATE TABLE resume_chat;
TRUNCATE TABLE resume_email_log;
TRUNCATE TABLE resume;
TRUNCATE TABLE user_memory;
TRUNCATE TABLE remember_login_token;

SET FOREIGN_KEY_CHECKS = 1;

-- 帮用户加一下头像
UPDATE `user`
  SET `avatarUrl` =
          'https://api.dicebear.com/9.x/lorelei/svg?eyebrows=variant01,variant05,variant06,variant07,variant08,variant09,variant12&seed=Riley'
  WHERE `avatarUrl` IS NULL OR `avatarUrl` = '';
