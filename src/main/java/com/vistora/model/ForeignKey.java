package com.vistora.model;

import lombok.Data;

@Data
public class ForeignKey {
    private String name;
    private String sourceTable;
    private String sourceColumn;
    private String targetTable;
    private String targetColumn;
    private String updateRule;
    private String deleteRule;
} 