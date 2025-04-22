package com.vistora.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class Table {
    private String name;
    private String comment;
    private long rowCount;
    private List<Column> columns = new ArrayList<>();
    private List<ForeignKey> foreignKeys = new ArrayList<>();
    private List<String> primaryKeys = new ArrayList<>();
    private List<Index> indexes = new ArrayList<>();
} 