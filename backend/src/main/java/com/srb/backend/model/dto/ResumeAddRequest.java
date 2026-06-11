package com.srb.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResumeAddRequest {
    @Size(max = 256)
    private String title;
    private String currentTemplate;
    private String styleConfig;
}
