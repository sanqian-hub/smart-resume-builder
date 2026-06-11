package com.srb.backend.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResumeUpdateRequest {
    private Long id;
    private String title;
    private Integer status;
    private String currentTemplate;
    private String styleConfig;
    private List<ResumeContentDTO> contents;
}
