package com.vistora.model;

import lombok.Data;
import java.util.List;

@Data
public class Index {
    private String name;
    private String tableName;
    private List<String> columnNames;
    private boolean unique;
    private String type;
} 