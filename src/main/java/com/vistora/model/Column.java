package com.vistora.model;

import lombok.Data;

@Data
public class Column {
    private String name;
    private String type;
    private int size;
    private int precision;
    private int scale;
    private boolean nullable;
    private boolean primaryKey;
    private boolean autoIncrement;
    private String defaultValue;
    private String comment;
} 