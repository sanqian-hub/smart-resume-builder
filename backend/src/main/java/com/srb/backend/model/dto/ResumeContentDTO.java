package com.srb.backend.model.dto;

import lombok.Data;

@Data
public class ResumeContentDTO {
    private Long id;
    private String moduleType;
    private String contentJson;
    private Integer sortOrder;
}
