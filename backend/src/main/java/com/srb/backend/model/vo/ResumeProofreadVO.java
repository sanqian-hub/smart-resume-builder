package com.srb.backend.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResumeProofreadVO {

    private String summary;

    private List<Item> items = new ArrayList<>();

    @Data
    public static class Item {
        private String id;
        private String moduleType;
        private Integer itemIndex;
        private String fieldPath;
        private Integer occurrenceIndex;
        private String fieldLabel;
        private String type;
        private String typeLabel;
        private String original;
        private String suggestion;
        private String reason;
    }
}
