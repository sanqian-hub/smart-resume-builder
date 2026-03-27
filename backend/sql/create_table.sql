-- auto-generated definition
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    username     varchar(256)                       null comment '用户名',
    userAccount  varchar(256)                       null comment '登录账号',
    avatarUrl    varchar(1024)                      null comment '用户头像URL',
    userPassword varchar(512)                       not null comment '登录密码',
    email        varchar(512)                       null comment '用户邮箱',
    userStatus   int      default 0                 not null comment '用户状态 0 - 正常',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '逻辑删除 0 - 没删除',
    userRole     int      default 0                 not null comment '0 - 普通用户  1 - 管理员'
)
    comment '用户';

