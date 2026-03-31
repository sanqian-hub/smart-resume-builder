package com.smart.resume.model.domain.request;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class DeleteRequest {

    @NotNull(message = "id不能为空")
    @Positive(message = "删除用户id必须大于0")
    private Long id;

}
