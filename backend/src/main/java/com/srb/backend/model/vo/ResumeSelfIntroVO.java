package com.srb.backend.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class ResumeSelfIntroVO {
    private String title;
    private String content;
    private List<String> highlights;
}
