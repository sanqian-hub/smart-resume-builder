package com.srb.backend.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResumeScoreVO {

    private Integer totalScore;

    private String summary;

    private List<String> highlights = new ArrayList<>();

    private List<String> suggestions = new ArrayList<>();

    private List<Dimension> dimensions = new ArrayList<>();

    @Data
    public static class Dimension {
        private String name;
        private Integer score;
        private String analysis;
        private String suggestion;
    }
}
