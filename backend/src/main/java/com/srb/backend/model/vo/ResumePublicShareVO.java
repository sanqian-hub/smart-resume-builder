package com.srb.backend.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResumePublicShareVO implements Serializable {
    private Boolean needPassword;
    private Boolean expired;
    private ResumeVO resume;
}
