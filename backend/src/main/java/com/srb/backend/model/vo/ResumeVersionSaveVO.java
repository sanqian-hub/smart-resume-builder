package com.srb.backend.model.vo;

import lombok.Data;

@Data
public class ResumeVersionSaveVO {
    private Long id;
    private Integer versionNum;
    private Boolean created;
}
