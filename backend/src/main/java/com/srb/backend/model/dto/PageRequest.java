package com.srb.backend.model.dto;

import lombok.Data;

@Data
public class PageRequest {
    private int current = 1;
    private int pageSize = 10;
}
