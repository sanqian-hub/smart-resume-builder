package com.smart.resume.constant;

public interface Const {

    // 分页参数常量（建议抽离到常量类）
    long MIN_CURRENT = 1L;
    long MIN_PAGE_SIZE = 1L;
    long MAX_PAGE_SIZE = 100L; // 限制最大每页条数，防止查太多数据
}
