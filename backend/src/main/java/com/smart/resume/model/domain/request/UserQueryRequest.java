package com.smart.resume.model.domain.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class UserQueryRequest {

    @NotNull(message = "当前页不能为空")
    @Min(value = 1, message = "当前页必须≥1")
    private long current = 1;

    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数必须≥1")
    private long pageSize = 5;

    // 👇 新增模糊查询字段
    private String username;

    private String userAccount;

    private Integer userStatus;

    private Integer userRole;

    private LocalDate startTime;

    private LocalDate endTime;
}
