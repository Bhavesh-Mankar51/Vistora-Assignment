package com.vistora.service;

import com.vistora.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SchemaCrawlerService {

    private final JdbcTemplate jdbcTemplate;
    private final Map<String, Table> tableCache = new ConcurrentHashMap<>();

    public SchemaCrawlerService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "schemaCache", key = "'allTables'")
    public List<Table> crawlSchema() {
        log.info("Starting schema crawl");
        try {
            List<Table> tables = new ArrayList<>();
            List<String> tableNames = getTableNames();
            
            for (String tableName : tableNames) {
                Table table = crawlTable(tableName);
                tables.add(table);
                tableCache.put(tableName, table);
            }
            
            log.info("Schema crawl completed successfully. Found {} tables.", tables.size());
            return tables;
        } catch (Exception e) {
            log.error("Error during schema crawl", e);
            throw new SchemaCrawlerException("Failed to crawl database schema", e);
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "schemaCache", key = "#tableName")
    public Table crawlTable(String tableName) {
        log.debug("Crawling table: {}", tableName);
        try {
            Table table = new Table();
            table.setName(tableName);
            table.setComment(getTableComment(tableName));
            table.setColumns(getColumns(tableName));
            table.setPrimaryKeys(getPrimaryKeys(tableName));
            table.setForeignKeys(getForeignKeys(tableName));
            table.setIndexes(getIndexes(tableName));
            table.setRowCount(getRowCount(tableName));
            return table;
        } catch (Exception e) {
            log.error("Error crawling table: {}", tableName, e);
            throw new SchemaCrawlerException("Failed to crawl table: " + tableName, e);
        }
    }

    private String getTableComment(String tableName) {
        String sql = """
            SELECT TABLE_COMMENT
            FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
            """;
        return jdbcTemplate.queryForObject(sql, String.class, tableName);
    }

    private List<String> getTableNames() {
        String sql = """
            SELECT TABLE_NAME 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_TYPE = 'BASE TABLE'
            ORDER BY TABLE_NAME
            """;
        return jdbcTemplate.queryForList(sql, String.class);
    }

    private List<Column> getColumns(String tableName) {
        String sql = """
            SELECT 
                COLUMN_NAME, 
                DATA_TYPE, 
                CHARACTER_MAXIMUM_LENGTH, 
                NUMERIC_PRECISION,
                NUMERIC_SCALE,
                IS_NULLABLE, 
                COLUMN_DEFAULT, 
                COLUMN_COMMENT,
                EXTRA,
                COLUMN_KEY
            FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = ?
            ORDER BY ORDINAL_POSITION
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Column column = new Column();
            column.setName(rs.getString("COLUMN_NAME"));
            column.setType(rs.getString("DATA_TYPE"));
            column.setSize(rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
            column.setPrecision(rs.getInt("NUMERIC_PRECISION"));
            column.setScale(rs.getInt("NUMERIC_SCALE"));
            column.setNullable("YES".equals(rs.getString("IS_NULLABLE")));
            column.setDefaultValue(rs.getString("COLUMN_DEFAULT"));
            column.setComment(rs.getString("COLUMN_COMMENT"));
            column.setAutoIncrement("auto_increment".equals(rs.getString("EXTRA")));
            column.setPrimaryKey("PRI".equals(rs.getString("COLUMN_KEY")));
            return column;
        }, tableName);
    }

    private List<String> getPrimaryKeys(String tableName) {
        String sql = """
            SELECT COLUMN_NAME
            FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = ? 
            AND CONSTRAINT_NAME = 'PRIMARY'
            ORDER BY ORDINAL_POSITION
            """;
        
        return jdbcTemplate.queryForList(sql, String.class, tableName);
    }

    private List<ForeignKey> getForeignKeys(String tableName) {
        String sql = """
            SELECT 
                k.CONSTRAINT_NAME,
                k.COLUMN_NAME,
                k.REFERENCED_TABLE_NAME,
                k.REFERENCED_COLUMN_NAME,
                r.UPDATE_RULE,
                r.DELETE_RULE
            FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE k
            JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS r
                ON k.CONSTRAINT_NAME = r.CONSTRAINT_NAME
                AND k.TABLE_SCHEMA = r.CONSTRAINT_SCHEMA
            WHERE k.TABLE_SCHEMA = DATABASE() 
            AND k.TABLE_NAME = ? 
            AND k.REFERENCED_TABLE_NAME IS NOT NULL
            ORDER BY k.ORDINAL_POSITION
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ForeignKey fk = new ForeignKey();
            fk.setName(rs.getString("CONSTRAINT_NAME"));
            fk.setSourceTable(tableName);
            fk.setSourceColumn(rs.getString("COLUMN_NAME"));
            fk.setTargetTable(rs.getString("REFERENCED_TABLE_NAME"));
            fk.setTargetColumn(rs.getString("REFERENCED_COLUMN_NAME"));
            fk.setUpdateRule(rs.getString("UPDATE_RULE"));
            fk.setDeleteRule(rs.getString("DELETE_RULE"));
            return fk;
        }, tableName);
    }

    private List<Index> getIndexes(String tableName) {
        String sql = """
            SELECT 
                INDEX_NAME,
                COLUMN_NAME,
                NON_UNIQUE,
                INDEX_TYPE,
                SEQ_IN_INDEX
            FROM INFORMATION_SCHEMA.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = ?
            ORDER BY INDEX_NAME, SEQ_IN_INDEX
            """;
        
        Map<String, Index> indexMap = new HashMap<>();
        
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            try {
                String indexName = rs.getString("INDEX_NAME");
                Index index = indexMap.computeIfAbsent(indexName, k -> {
                    try {
                        Index idx = new Index();
                        idx.setName(indexName);
                        idx.setTableName(tableName);
                        idx.setUnique(!rs.getBoolean("NON_UNIQUE"));
                        idx.setType(rs.getString("INDEX_TYPE"));
                        idx.setColumnNames(new ArrayList<>());
                        return idx;
                    } catch (SQLException e) {
                        throw new SchemaCrawlerException("Error processing index metadata", e);
                    }
                });
                index.getColumnNames().add(rs.getString("COLUMN_NAME"));
                return index;
            } catch (SQLException e) {
                throw new SchemaCrawlerException("Error processing index metadata", e);
            }
        }, tableName);
        
        return new ArrayList<>(indexMap.values());
    }

    private long getRowCount(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public static class SchemaCrawlerException extends RuntimeException {
        public SchemaCrawlerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 